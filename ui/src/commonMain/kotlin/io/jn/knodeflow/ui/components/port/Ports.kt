package io.jn.knodeflow.ui.components.port

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.jn.knodeflow.port.Port
import io.jn.knodeflow.port.PortDirection
import io.jn.knodeflow.graph.LocalGraphBackgroundState

@Composable
fun PortList(
    inputs: List<Port>,
    outputs: List<Port>,
    onPortPositionChanged: (Port, Offset) -> Unit,
    onPortDragStarted: (Port, Offset) -> Unit,
    onPortDragEnded: (Port, Offset) -> Unit,
    onValueChange: (Port, String) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)) {
        inputs.forEach { port ->
            PortItem(
                port = port,
                onPositionCalculated = {
                    onPortPositionChanged(port, it)
                },
                onStartDrag = {
                    onPortDragStarted(port, it)
                },
                onEndDrag = {
                    onPortDragEnded(port, it)
                },
                onValueChange = {
                    onValueChange(port, it)
                },
                modifier = modifier
            )
        }

        outputs.forEach { port ->
            PortItem(
                port = port,
                onPositionCalculated = {
                    onPortPositionChanged(port, it)
                },
                onStartDrag = {
                    onPortDragStarted(port, it)
                },
                onEndDrag = {
                    onPortDragEnded(port, it)
                },
                onValueChange = {
                    onValueChange(port, it)
                },
                modifier = modifier
            )
        }
    }
}

@Composable
private fun PortItem(
    port: Port,
    onPositionCalculated: (Offset) -> Unit,
    onStartDrag: (Offset) -> Unit,
    onEndDrag: (Offset) -> Unit,
    onValueChange: (String) -> Unit,
    modifier: Modifier
) {
    val isInput = port.direction == PortDirection.Input

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = if (isInput) Arrangement.spacedBy(5.dp, Alignment.Start) else Arrangement.spacedBy(5.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isInput){
            PortIndicator(
                modifier = Modifier
                    .background(Color(0xFF31dd9f), CircleShape),
                port = port,
                onPositionCalculated = onPositionCalculated,
                onStartDrag = onStartDrag,
                onEndDrag = onEndDrag,
            )
        }

        if (port.value is String) {
            CustomTextField(
                value = port.value as String,
                label = port.name,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
            )
        } else {
            Text(
                text = port.id.toString(),
                color = Color.White

            )
        }

        if (!isInput){

            PortIndicator(
                modifier = Modifier
                    .background(Color(0xFF31dd9f), CircleShape),
                port = port,
                onPositionCalculated = onPositionCalculated,
                onStartDrag = onStartDrag,
                onEndDrag = onEndDrag,
            )
        }

    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PortIndicator(
    port: Port,
    onPositionCalculated: (Offset) -> Unit,
    onStartDrag: (Offset) -> Unit,
    onEndDrag: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val dragState = LocalDragConnectionState.current
    val density = LocalDensity.current

    val portSizeDp = 12.dp
    val portSizePx = with(density) { portSizeDp.toPx() }

    var centerPosition by remember { mutableStateOf(Offset.Zero) }
    val graphBackgroundState = LocalGraphBackgroundState.current
    val zoom = graphBackgroundState.value.zoom
    val pan = graphBackgroundState.value.pan
    val graphPositionInWindow = graphBackgroundState.value.coordinates

    Box(
        modifier = modifier
            .size(portSizeDp)
            .onGloballyPositioned { coordinates ->
                val posInWindow = coordinates.localToWindow(Offset.Zero)
                val center = posInWindow + Offset(portSizePx / 2, portSizePx / 2)
                val correctedCenter = (center - pan)
                centerPosition = correctedCenter
                onPositionCalculated(correctedCenter)
            }

            .pointerInput(port.id) {
                if (port.direction == PortDirection.Output) {
                    detectDragGestures(
                        onDragStart = {
                            onStartDrag(centerPosition)
                        },
                        onDrag = { change, dragAmount ->
                            val newPosition = (dragState.value.current ?: Offset.Zero) + dragAmount
                            dragState.value = dragState.value.copy(current = newPosition)
                        },
                        onDragEnd = {
                            dragState.value = dragState.value.copy(isDragging = false)
                            onEndDrag(dragState.value.current ?: Offset.Zero)

                        },
                        onDragCancel = { dragState.value = dragState.value.copy(isDragging = false) }
                    )
                }
            }

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor =   Color(0xFF00215E),
        unfocusedBorderColor = Color.Gray,
        disabledBorderColor = Color.White,
        errorBorderColor = Color.White,
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White
    )


    BasicTextField(
        modifier = modifier
            .size(200.dp, 25.dp),
        value = value,
        onValueChange = onValueChange,
        interactionSource = interactionSource,
        singleLine = true,
        cursorBrush = SolidColor(Color.White),
        textStyle = TextStyle(color = Color.White),
    ) {
        OutlinedTextFieldDefaults.DecorationBox(
            value = value,
            visualTransformation = VisualTransformation.None,
            innerTextField = it,
            label = {
                Text(
                    text = label
                )
            },
            leadingIcon = null,
            trailingIcon = null,
            singleLine = true,
            enabled = true,
            isError = false,
            interactionSource = interactionSource,
            colors = colors,
            contentPadding = OutlinedTextFieldDefaults.contentPadding(
                top = 0.dp,
                bottom = 0.dp,
                end = 0.dp,
            ),
            container = {
                OutlinedTextFieldDefaults.Container(
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    colors = colors,
                    shape = MaterialTheme.shapes.extraSmall,
                )
            }
        )
    }
}

data class DragConnectionState(
    val isDragging: Boolean = false,
    val from: Offset? = null,
    val current: Offset? = null,
    val sourcePortId: String? = null
)

val LocalDragConnectionState = compositionLocalOf {
    mutableStateOf(DragConnectionState())
}

