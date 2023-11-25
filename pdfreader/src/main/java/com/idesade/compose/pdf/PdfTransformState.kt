package com.idesade.compose.pdf

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateTo
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.TransformScope
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun rememberPdfTransformState(
    file: File?,
    scope: CoroutineScope = rememberCoroutineScope(),
    maxScale: Float = 3f,
    midScale: Float = 1.75f,
    minScale: Float = 1f,
): PdfTransformState {
    return remember(file) {
        PdfTransformState(
            scope = scope,
            maxScale = maxScale,
            midScale = midScale,
            minScale = minScale,
        )
    }
}

class PdfTransformState(
    val scope: CoroutineScope,
    val maxScale: Float = 3f,
    val midScale: Float = 1.75f,
    val minScale: Float = 1f,
) : TransformableState {

    var size = IntSize.Zero

    var scale by mutableFloatStateOf(1f)
        private set

    val verticalScrollState = LazyListState()

    private var offset by mutableStateOf(Offset.Zero)

    private val transformableState = TransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(minScale, maxScale)

        val verticalOffset = if (panChange.y > 0) {
            if (verticalScrollState.canScrollBackward) {
                Pair(0f, panChange.y)
            } else {
                Pair(panChange.y, 0f)
            }
        } else {
            if (verticalScrollState.canScrollForward) {
                Pair(0f, panChange.y)
            } else {
                Pair(panChange.y, 0f)
            }
        }

        val newOffset = if (scale > 1f) {
            val maxX = (size.width * scale) - size.width
            val maxY = (size.height * scale) - size.height
            Offset(
                x = (offset.x + panChange.x).coerceIn((-maxX / 2), (maxX / 2)),
                y = (offset.y + verticalOffset.first).coerceIn((-maxY / 2), (maxY / 2))
            )
        } else {
            Offset(0f, 0f)
        }

        offset = newOffset
        scope.launch {
            verticalScrollState.scrollBy(-verticalOffset.second / scale)
        }
    }

    override suspend fun transform(
        transformPriority: MutatePriority,
        block: suspend TransformScope.() -> Unit
    ): Unit = transformableState.transform(transformPriority, block)

    override val isTransformInProgress: Boolean
        get() = transformableState.isTransformInProgress

    suspend fun transformBy(
        zoomChange: Float = 1f,
        panChange: Offset = Offset.Zero
    ) = this.transform {
        transformBy(zoomChange, panChange)
    }

    suspend fun animateTransformBy(
        zoomChange: Float,
        panChange: Offset,
        animationSpec: AnimationSpec<Float> = SpringSpec(stiffness = Spring.StiffnessLow)
    ) {
        val baseScale = scale
        var previous = 0f
        transform {
            AnimationState(initialValue = previous).animateTo(1f, animationSpec) {
                val delta = value - previous
                previous = value
                transformBy(
                    zoomChange = (baseScale * (1 + (zoomChange - 1) * value)) / scale,
                    panChange = panChange * delta,
                )
            }
        }
    }

    fun GraphicsLayerScope.setGraphicsLayer() {
        scaleX = scale
        scaleY = scale
        translationX = offset.x
        translationY = offset.y
    }

}