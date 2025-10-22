package io.jn.knodeflow.ui.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import io.jn.knodeflow.graph.GraphBackground
import io.jn.knodeflow.graph.GraphBackgroundStateProvider
import io.jn.knodeflow.graph.GraphState
import io.jn.knodeflow.graph.LocalGraphBackgroundState
import io.jn.knodeflow.graph.rememberGraphState
import io.jn.knodeflow.node.Node
import io.jn.knodeflow.port.Connection
import io.jn.knodeflow.registry.NodeRegistry
import io.jn.knodeflow.ui.components.connection.ConnectionLine
import io.jn.knodeflow.ui.components.connection.ConnectionLines
import io.jn.knodeflow.ui.components.dialog.AddNodeDialog
import io.jn.knodeflow.ui.components.node.Nodes
import io.jn.knodeflow.ui.components.port.DragConnectionState
import io.jn.knodeflow.ui.components.port.LocalDragConnectionState
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun KNodeFlowEditor(
    registry: NodeRegistry,
    modifier: Modifier = Modifier,
    graph: GraphState = rememberGraphState(),
    config: EditorConfig = KNodeFlowEditorDefaults.config(),
    onNodeSelected: ((Node) -> Unit)? = null,
    onNodeAdded: ((Node) -> Unit)? = null,
    onNodeRemoved: ((Node) -> Unit)? = null,
    onConnectionCreated: ((Connection) -> Unit)? = null,
    onExecutionStarted: (() -> Unit)? = null,
    onExecutionComplete: ((Map<String, Any?>) -> Unit)? = null,
    onError: ((String) -> Unit)? = null
) {
    rememberCoroutineScope()
    val dragConnectionState = remember { mutableStateOf(DragConnectionState()) }

    var showAddNodeDialog by remember { mutableStateOf(false) }
    var addNodeDialogPosition by remember { mutableStateOf(IntOffset.Zero) }
    var mousePosition by remember { mutableStateOf(IntOffset.Zero) }

    CompositionLocalProvider(LocalDragConnectionState provides dragConnectionState) {

        GraphBackgroundStateProvider {
            val graphBackgroundState by LocalGraphBackgroundState.current
            val pan = graphBackgroundState.pan

            GraphBackground(
                modifier = modifier.clickable(
                    interactionSource = null, indication = null
                ) {
                    showAddNodeDialog = false
                }.pointerInput(graphBackgroundState) {
                    awaitEachGesture {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull() ?: continue
                            val position = change.position
                            mousePosition = IntOffset(
                                ((position.x - pan.x) / graphBackgroundState.zoom).toInt(),
                                ((position.y - pan.y) / graphBackgroundState.zoom).toInt()
                            )


                            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                                addNodeDialogPosition = mousePosition
                                showAddNodeDialog = true
                                change.consume()
                            }

                        }
                    }
                }) {

                ConnectionLines(
                    ports = graph.ports, getPortPosition = { nodeId, portId, direction ->
                        graph.getPortPosition(nodeId, portId)
                    })

                val drag = dragConnectionState.value
                if (drag.isDragging && drag.from != null && drag.current != null) {
                    ConnectionLine(from = drag.from, to = drag.current)
                }


                Nodes(
                    nodes = graph.nodes,
                    positionedNodes = graph.positionedNodes,
                    onNodeDrag = { node, offset ->
                        graph.setNodePosition(node.id, offset)
                    },
                    onPortValueChange = { node, port, value ->
                        graph.setPortValue(value, port.id, node.id, port.direction)
                    },
                    onPortPositionChanged = { node, port, offset ->
                        graph.setPortPosition(
                            node.id, port.id, IntOffset(offset.x.toInt(), offset.y.toInt())
                        )
                    },
                    onPortDragStarted = { node, port, offset ->
                        val connection = graph.getConnection(node.id, port.id, port.direction)
                        connection?.let {
                            graph.removeConnection(it)
                        }

                        dragConnectionState.value = DragConnectionState(
                            isDragging = true,
                            from = offset,
                            current = offset,
                            sourcePortId = port.id
                        )
                    },
                    onPortDragEnded = { node, port, offset ->
                        val pair = graph.getNodeAndPortInPosition(
                            excludeNodeId = node.id, position = IntOffset(
                                offset.x.toInt(), offset.y.toInt()
                            )
                        )

                        pair?.let { (toNodeId, toPort) ->
                            val connection =
                                graph.getConnection(toNodeId.id, toPort.id, toPort.direction)

                            connection?.let {
                                graph.removeConnection(it)
                            }
                            graph.setConnection(
                                Connection(
                                    fromNodeId = node.id,
                                    toNodeId = toNodeId.id,
                                    fromPortId = port.id,
                                    toPortId = toPort.id
                                )
                            )

                        }
                    }

                )

                if (showAddNodeDialog) {
                    AddNodeDialog(
                        nodes = registry.getTemplates(),
                        position = addNodeDialogPosition,
                        onSelected = { template ->
                            val node = template.factory(Uuid.random().toString())
                            graph.apply {
                                addNode(node)
                                setNodePosition(node.id, mousePosition)
                            }
                            showAddNodeDialog = false
                        },
                    )
                }

            }

        }
    }
}