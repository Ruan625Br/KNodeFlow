package io.jn.knodeflow.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GraphBackground(
    modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit
) {
    val graphBackgroundState = LocalGraphBackgroundState.current
    val state = graphBackgroundState.value
    val minZoom = 0.3f
    val maxZoom = 3f
    val zoomSpeed = 0.05f
    val zoom = state.zoom
    val pan = state.pan

    Box(modifier = modifier.pointerInput(Unit) {
        detectDragGestures(
            onDrag = { change, dragAmount ->
                change.consume()
                val currentState = graphBackgroundState.value
                graphBackgroundState.value = currentState.copy(
                    pan = currentState.pan + dragAmount
                )
            },

            )
    }) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF212121))

        )

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawContext.canvas.save()
            drawContext.canvas.scale(zoom, zoom)

            val step = 50f
            val width = size.width / zoom
            val height = size.height / zoom

            val gridColor = Color.Gray.copy(alpha = 0.2f)

            val startX =  pan.x / zoom % step
            val startY =  pan.y / zoom % step

            for (x in 0..(width / step).toInt()) {
                val xPos = startX + x * step
                drawLine(
                    start = Offset(xPos, 0f),
                    end = Offset(xPos, height),
                    color = gridColor
                )
            }

            for (y in 0..(height / step).toInt()) {
                val xPos = startY + y * step

                drawLine(
                    start = Offset(0f, xPos),
                    end = Offset(width, xPos),
                    color = gridColor
                )
            }

            drawContext.canvas.restore()
        }


        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    translationX = pan.x, translationY = pan.y
                )
        ) {
            content()
        }

    }
}


@Composable
fun GraphBackgroundStateProvider(
    content: @Composable () -> Unit
){
    val graphBackgroundState = remember { mutableStateOf(GraphBackgroundState()) }
    CompositionLocalProvider(LocalGraphBackgroundState provides graphBackgroundState) {
        content()
    }

}

data class GraphBackgroundState(
    val zoom: Float = 1f,
    val pan: Offset = Offset.Zero,
    val coordinates: Offset = Offset.Zero
)

val LocalGraphBackgroundState: ProvidableCompositionLocal<MutableState<GraphBackgroundState>> = compositionLocalOf {
    mutableStateOf(GraphBackgroundState())
}