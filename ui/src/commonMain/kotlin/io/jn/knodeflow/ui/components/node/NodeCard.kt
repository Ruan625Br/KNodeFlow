package io.jn.knodeflow.ui.components.node

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.jn.knodeflow.node.Node
import io.jn.knodeflow.port.Port
import io.jn.knodeflow.ui.components.port.PortList

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NodeCard(
    node: Node,
    position: IntOffset,
    onNodeDrag: (IntOffset) -> Unit,
    onPortPositionChanged: (Port, Offset) -> Unit,
    onPortDragStarted: (Port, Offset) -> Unit,
    onPortDragEnded: (Port, Offset) -> Unit,
    onPortValueChange: (Port, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var nodePosition by remember {
        mutableStateOf(position)
    }
    var active by remember {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier.width(200.dp).offset {
            position
        }/*.onPointerEvent(PointerEventType.Enter) { active = true }

        .onPointerEvent(PointerEventType.Exit) { active = false }*/.pointerInput(node.id) {
            detectDragGestures(
                onDrag = { change, offset ->
                    val newOffset = IntOffset(
                        nodePosition.x + offset.x.toInt(), nodePosition.y + offset.y.toInt()
                    )
                    nodePosition = newOffset
                    onNodeDrag(nodePosition)

                })
        }.clip(MaterialTheme.shapes.small).border(
            1.dp, if (active) Color(0xFF00434A) else Color.Gray, MaterialTheme.shapes.small
        ).background(Color(0xFF020814).copy(0.9f), MaterialTheme.shapes.small)

    ) {

        Column(
            modifier = Modifier.blur(0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF00215E), Color.Black
                        )
                    )
                )
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                    text = node.name,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            PortList(
                inputs = node.inputs,
                outputs = node.outputs,
                onPortPositionChanged = onPortPositionChanged,
                onPortDragStarted = onPortDragStarted,
                onPortDragEnded = onPortDragEnded,
                onValueChange = onPortValueChange,
                modifier = Modifier
            )
        }

    }
}