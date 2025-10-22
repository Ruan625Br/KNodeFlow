package io.jn.knodeflow.ui.components.node

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import io.jn.knodeflow.graph.PositionedNode
import io.jn.knodeflow.node.Node
import io.jn.knodeflow.port.Port

@Composable
fun Nodes(
    nodes: List<Node>,
    positionedNodes: List<PositionedNode>,
    onNodeDrag: (Node, IntOffset) -> Unit,
    onPortPositionChanged: (Node, Port, Offset) -> Unit,
    onPortDragStarted: (Node, Port, Offset) -> Unit,
    onPortDragEnded: (Node, Port, Offset) -> Unit,
    onPortValueChange: (Node, Port, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        nodes.forEach { node ->
            val positionedNode = positionedNodes.find { it.node.id == node.id }
            val nodePosition = positionedNode?.position?.value ?: IntOffset.Zero

            NodeCard(
                node = node,
                position = nodePosition,
                onNodeDrag = { offset ->
                    onNodeDrag(node, offset)
                },
                onPortPositionChanged = { port, offset ->
                    onPortPositionChanged(node, port, offset)
                },
                onPortDragStarted = { port, offset ->
                    onPortDragStarted(node, port, offset)
                },
                onPortDragEnded = { port, offset ->
                    onPortDragEnded(node, port, offset)
                },
                onPortValueChange = { port, value ->
                    onPortValueChange(node, port, value)
                }
            )
        }
    }
}