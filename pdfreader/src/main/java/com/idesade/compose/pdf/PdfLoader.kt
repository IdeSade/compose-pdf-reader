package com.idesade.compose.pdf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
import android.os.ParcelFileDescriptor
import android.os.ParcelFileDescriptor.MODE_READ_ONLY
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import kotlin.math.roundToInt

@Composable
fun rememberPdfLoader(file: File?): PdfLoader =
    remember(file) { PdfLoader(file) }

class PdfLoader(private val file: File?) : RememberObserver {

    sealed interface State {
        data object Init : State
        data object Loading : State
        data object Success : State
        data class Error(val throwable: Throwable) : State
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        state = State.Error(throwable)
    }

    private var rememberScope: CoroutineScope? = null
    private val mutex = Mutex()

    private var pdfRenderer: PdfRenderer? = null

    private val bitmapCache: MutableMap<PageInfo, ImageBitmap?> = mutableMapOf()
    private val scaleCache: MutableMap<PageInfo, Float> = mutableMapOf()
    private val jobList: MutableMap<PageInfo, Job?> = mutableMapOf()

    var pdfPages: List<PageInfo> = emptyList()
        private set

    var state: State by mutableStateOf(State.Init)
        private set

    override fun onRemembered() {
        if (rememberScope != null || file == null) return

        rememberScope = CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)

        rememberScope?.launch {
            mutex.withLock {
                state = State.Loading
                val pfd = ParcelFileDescriptor.open(file, MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(pfd)
                pdfPages = (0 until pdfRenderer.pageCount).map { index ->
                    pdfRenderer.openPage(index).use { page ->
                        PageInfo(
                            index = index,
                            width = page.width,
                            height = page.height,
                            pageCount = pdfRenderer.pageCount
                        )
                    }
                }
                this@PdfLoader.pdfRenderer = pdfRenderer
                state = State.Success
            }
        }
    }

    override fun onForgotten() {
        clear()
    }

    override fun onAbandoned() {
        clear()
    }

    private fun clear() {
        rememberScope?.launch {
            mutex.withLock {
                pdfRenderer?.close()
            }

            pdfRenderer = null
            pdfPages = emptyList()

            bitmapCache.clear()
            scaleCache.clear()
            jobList.values.onEach { it?.cancel() }
            jobList.clear()

            state = State.Init

            rememberScope = null
        }
    }

    private suspend fun renderPage(
        pageInfo: PageInfo,
        width: Int,
        height: Int,
        scale: Float,
        onComplete: (ImageBitmap) -> Unit
    ) {
        rememberScope?.launch {
            if (scaleCache[pageInfo]?.equals(scale) == true) {
                bitmapCache[pageInfo]?.let(onComplete)
            } else {
                jobList[pageInfo]?.cancel()
                jobList[pageInfo] = rememberScope?.launch {
                    var imageBitmap: ImageBitmap? = null
                    ensureActive()
                    mutex.withLock {
                        ensureActive()
                        pdfRenderer?.openPage(pageInfo.index)?.use { page ->
                            val bitmap = Bitmap.createBitmap(
                                (width * scale).roundToInt(),
                                (height * scale).roundToInt(),
                                Bitmap.Config.ARGB_8888
                            )
                            page.render(bitmap, null, null, RENDER_MODE_FOR_DISPLAY)
                            imageBitmap = bitmap.asImageBitmap()
                        }
                    }
                    bitmapCache[pageInfo] = imageBitmap
                    scaleCache[pageInfo] = scale
                    ensureActive()
                    imageBitmap?.let(onComplete)
                }
            }
        }
    }

    private fun getBitmap(pageInfo: PageInfo): ImageBitmap? = bitmapCache[pageInfo]

    sealed interface PageContent {
        data class Empty(val width: Int, val height: Int) : PageContent
        data class Content(val bitmap: ImageBitmap, val contentDescription: String) : PageContent
    }

    inner class PageInfo(val index: Int, val width: Int, val height: Int, val pageCount: Int) {

        private var _state = MutableStateFlow<PageContent>(PageContent.Empty(width, height))
        val state = _state.asStateFlow()

        suspend fun render(width: Int, height: Int, scale: Float) {
            getBitmap(this)?.let(::setState)
            renderPage(
                pageInfo = this,
                width = width,
                height = height,
                scale = scale,
                onComplete = ::setState
            )
        }

        private fun setState(imageBitmap: ImageBitmap) {
            _state.value = PageContent.Content(
                bitmap = imageBitmap,
                contentDescription = "Page ${index + 1} of $pageCount"
            )
        }

    }

}