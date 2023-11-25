package com.idesade.compose.pdf

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun PdfReader(
    file: File?,
    modifier: Modifier = Modifier,
    loader: PdfLoader = rememberPdfLoader(file),
    pdfTransformState: PdfTransformState = rememberPdfTransformState(file),
    initContent: @Composable () -> Unit = {},
    loadingContent: @Composable () -> Unit = {
        CircularProgressIndicator()
    },
    errorContent: @Composable (PdfLoader.State.Error) -> Unit = { state ->
        Text(text = state.throwable.message.orEmpty())
    },
    contentPadding: PaddingValues = PaddingValues(8.dp),
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.LightGray),
        contentAlignment = Alignment.Center,
    ) {
        when (val state = loader.state) {
            is PdfLoader.State.Init -> initContent()
            is PdfLoader.State.Loading -> loadingContent()
            is PdfLoader.State.Error -> errorContent(state)
            is PdfLoader.State.Success ->
                BoxWithConstraints {
                    val maxPageWidth = remember { loader.pdfPages.maxOf { it.width } }

                    LazyColumn(
                        modifier = Modifier
                            .graphicsLayerWithPdfTransform(pdfTransformState)
                            .background(Color.Gray),
                        contentPadding = contentPadding,
                        state = pdfTransformState.verticalScrollState,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(loader.pdfPages) { pageInfo ->
                            val width = maxWidth * (pageInfo.width.toFloat() / maxPageWidth)
                            val height = width * (pageInfo.height.toFloat() / pageInfo.width)

                            val widthPx = width.roundToPx()
                            val heightPx = height.roundToPx()

                            Box(
                                modifier = Modifier.size(maxWidth, height),
                                contentAlignment = Alignment.Center,
                            ) {
                                when (val pageContent = pageInfo.state.collectAsState().value) {
                                    is PdfLoader.PageContent.Empty -> {
                                        Box(
                                            modifier = Modifier
                                                .size(width, height)
                                                .background(Color.White)
                                        )
                                    }

                                    is PdfLoader.PageContent.Content -> {
                                        Image(
                                            modifier = Modifier
                                                .size(width, height)
                                                .background(Color.White),
                                            painter = BitmapPainter(pageContent.bitmap),
                                            contentDescription = pageContent.contentDescription
                                        )
                                    }
                                }

                                LaunchedEffect(pdfTransformState.scale) {
                                    pageInfo.render(widthPx, heightPx, pdfTransformState.scale)
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .pdfTransformable(pdfTransformState, constraints)
                            .fillMaxSize()
                    )
                }
        }
    }
}

@Composable
private fun Dp.roundToPx() = with(LocalDensity.current) { roundToPx() }