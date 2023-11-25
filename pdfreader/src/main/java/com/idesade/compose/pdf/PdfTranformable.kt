package com.idesade.compose.pdf

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.panBy
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

fun Modifier.graphicsLayerWithPdfTransform(
    state: PdfTransformState,
): Modifier = this
    .clipToBounds()
    .graphicsLayer {
        with(state) {
            setGraphicsLayer()
        }
    }

fun Modifier.pdfTransformable(
    state: PdfTransformState,
    constraints: Constraints,
    flingBehavior: FlingBehavior? = null,
): Modifier = composed {
    state.size = IntSize(constraints.maxWidth, constraints.maxHeight)

    val scope = rememberCoroutineScope()

    val velocityTracker = remember { VelocityTracker() }
    val fling = flingBehavior ?: ScrollableDefaults.flingBehavior()

    val horizontalScrollState = remember {
        ScrollableState { change ->
            scope.launch {
                state.panBy(Offset(-change, 0f))
            }
            change
        }
    }
    val verticalScrollState = remember {
        ScrollableState { change ->
            scope.launch {
                state.panBy(Offset(0f, -change))
            }
            change
        }
    }

    val center = remember { Offset(state.size.width / 2f, state.size.height / 2f) }

    this
        .pointerInput(Unit) {
            coroutineScope {
                detectTapGestures(onDoubleTap = { tapCenter ->
                    launch {
                        val newScale = when {
                            state.scale < state.midScale -> state.midScale
                            state.scale < state.maxScale -> state.maxScale
                            else -> state.minScale
                        }

                        val diffScale = newScale / state.scale
                        val position = (tapCenter - center) * diffScale
                        state.animateTransformBy(diffScale, -position)
                    }
                })
            }
        }
        .pointerInput(Unit) {
            coroutineScope {
                detectPdfTransformGestures(
                    onStart = {
                        velocityTracker.resetTracking()

                        if (horizontalScrollState.isScrollInProgress) {
                            launch {
                                horizontalScrollState.stopScroll()
                            }
                        }
                        if (verticalScrollState.isScrollInProgress) {
                            launch {
                                verticalScrollState.stopScroll()
                            }
                        }
                    },
                    onEnd = {
                        val velocity = velocityTracker.calculateVelocity()

                        launch {
                            horizontalScrollState.scroll {
                                with(fling) {
                                    performFling(-velocity.x)
                                }
                            }
                        }
                        launch {
                            verticalScrollState.scroll {
                                with(fling) {
                                    performFling(-velocity.y)
                                }
                            }
                        }
                    },
                    onEvent = { event ->
                        event.changes.forEach(velocityTracker::addPointerInputChange)
                    },
                    onGesture = { zoomChange, panChange ->
                        launch {
                            state.transformBy(zoomChange, panChange)
                        }
                    },
                )
            }
        }
}