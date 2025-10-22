package io.jn.knodeflow.graph

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.unit.IntOffset
import io.jn.knodeflow.node.Node
import io.jn.knodeflow.port.Connection
import io.jn.knodeflow.port.Port
import io.jn.knodeflow.port.PortDirection
import kotlin.collections.set
import kotlin.math.hypot

class GraphState {
    private val _nodes = mutableStateListOf<Node>()
    val nodes: SnapshotStateList<Node> get() = _nodes

    val ports: List<Port> by derivedStateOf {
        _nodes.flatMap { it.inputs + it.outputs }
    }

    private val _positionedNodes = mutableStateListOf<PositionedNode>()
    val positionedNodes: List<PositionedNode> get() = _positionedNodes

    private val nodeIndex = mutableStateMapOf<String, Int>()
    private val positionedNodeIndex = mutableStateMapOf<String, Int>()

    private val _connections = mutableStateMapOf<String, Connection>()
    val connections: Map<String, Connection> get() = _connections


    fun addNode(node: Node) {
        val index = _nodes.size
        _nodes.add(node)
        nodeIndex[node.id] = index

        val positioned = PositionedNode(node)
        _positionedNodes.add(positioned)
        positionedNodeIndex[node.id] = index
    }

    fun addAllNode(nodes: List<Node>) {
        nodes.forEach { addNode(it) }
    }

    fun removeNode(nodeId: String) {
        getNode(nodeId) ?: return

        removeNodeConnections(nodeId)

        val index = nodeIndex[nodeId] ?: return
        _nodes.removeAt(index)
        nodeIndex.remove(nodeId)

        rebuildNodeIndex()

        val posIndex = positionedNodeIndex[nodeId] ?: return
        _positionedNodes.removeAt(posIndex)
        positionedNodeIndex.remove(nodeId)
        rebuildPositionedNodeIndex()
    }

    fun getNode(nodeId: String): Node? {
        val index = nodeIndex[nodeId] ?: return null
        return _nodes.getOrNull(index)
    }

    fun updateNode(nodeId: String, updater: (Node) -> Node) {
        val index = nodeIndex[nodeId] ?: return
        val oldNode = _nodes[index]
        val newNode = updater(oldNode)
        _nodes[index] = newNode

        val posIndex = positionedNodeIndex[nodeId]
        if (posIndex != null) {
            _positionedNodes[posIndex] = _positionedNodes[posIndex].copy(node = newNode)
        }
    }

    fun setPortValue(
        nodeId: String, portId: String, value: Any?, direction: PortDirection
    ) {
        updateNode(nodeId) { node ->
            val ports = if (direction == PortDirection.Input) node.inputs else node.outputs
            val portIndex = ports.indexOfFirst { it.id == portId }

            if (portIndex == -1) return@updateNode node

            val updatedPort = ports[portIndex].copy(value = value)
            val newPorts = ports.toMutableList().apply { this[portIndex] = updatedPort }

            if (direction == PortDirection.Input) {
                node.copy(inputs = newPorts)
            } else {
                node.copy(outputs = newPorts)
            }
        }
    }

    fun getPort(nodeId: String, portId: String, direction: PortDirection): Port? {
        val node = getNode(nodeId) ?: return null
        val ports = if (direction == PortDirection.Input) node.inputs else node.outputs
        return ports.find { it.id == portId }
    }


    fun canConnect(
        fromNodeId: String, fromPortId: String, toNodeId: String, toPortId: String
    ): Boolean {
        if (fromNodeId == toNodeId) return false

        getPort(fromNodeId, fromPortId, PortDirection.Output) ?: return false
        val toPort = getPort(toNodeId, toPortId, PortDirection.Input) ?: return false

        return toPort.canAcceptConnection()
    }


    fun canConnect(
        connection: Connection
    ): Boolean {
        return canConnect(
            connection.fromNodeId, connection.fromPortId, connection.toNodeId, connection.toPortId
        )
    }

    fun setConnection(connection: Connection): Boolean {
        if (!canConnect(connection)) return false

        val connectionKey = getConnectionKey(
            connection.fromNodeId, connection.fromPortId, PortDirection.Output
        )

        val targetNodeConnectionKey = getConnectionKey(
            connection.toNodeId, connection.toPortId, PortDirection.Input
        )

        val toPort = getPort(connection.toNodeId, connection.toPortId, PortDirection.Input)
        if (toPort?.isConnected() == true && !toPort.allowMultipleConnections) {
            toPort.connection?.let { removeConnection(it) }
        }

        _connections[connectionKey] = connection
        _connections[targetNodeConnectionKey] = connection

        updateNode(connection.fromNodeId) { node ->
            val portIndex = node.outputs.indexOfFirst { it.id == connection.fromPortId }
            if (portIndex == -1) return@updateNode node

            val updatedPort = node.outputs[portIndex].addConnection(connection)
            val newOutputs = node.outputs.toMutableList().apply { this[portIndex] = updatedPort }
            node.copy(outputs = newOutputs)
        }

        updateNode(connection.toNodeId) { node ->
            val portIndex = node.inputs.indexOfFirst { it.id == connection.toPortId }
            if (portIndex == -1) return@updateNode node

            val updatedPort = node.inputs[portIndex].addConnection(connection)
            val newInputs = node.inputs.toMutableList().apply { this[portIndex] = updatedPort }
            node.copy(inputs = newInputs)
        }

        return true
    }


    fun removeConnection(connection: Connection) {
        val connectionKey = getConnectionKey(
            connection.fromNodeId, connection.fromPortId, PortDirection.Input
        )

        _connections.remove(connectionKey)

        updateNode(connection.fromNodeId) { node ->
            val portIndex = node.outputs.indexOfFirst { it.id == connection.fromPortId }
            if (portIndex == -1) return@updateNode node

            val updatedPort = node.outputs[portIndex].removeConnection(connection)
            val newOutputs = node.outputs.toMutableList().apply { this[portIndex] = updatedPort }
            node.copy(outputs = newOutputs)
        }

        updateNode(connection.toNodeId) { node ->
            val portIndex = node.inputs.indexOfFirst { it.id == connection.toPortId }
            if (portIndex == -1) return@updateNode node

            val updatedPort = node.inputs[portIndex].removeConnection(connection)
            val newInputs = node.inputs.toMutableList().apply { this[portIndex] = updatedPort }
            node.copy(inputs = newInputs)
        }
    }

    fun getConnection(
        nodeId: String, portId: String, direction: PortDirection
    ): Connection? {
        val key = getConnectionKey(nodeId, portId, direction)
        return _connections[key]
    }

    fun getNodeConnections(nodeId: String): List<Connection> {
        return _connections.values.filter {
            it.fromNodeId == nodeId || it.toNodeId == nodeId
        }
    }

    private fun removeNodeConnections(nodeId: String) {
        val connectionsToRemove = getNodeConnections(nodeId)
        connectionsToRemove.forEach { removeConnection(it) }
    }

    fun getConnectionKey(nodeId: String, portId: String, direction: PortDirection): String {
        return "$nodeId:$portId:${direction.name}"
    }

    fun setNodePosition(nodeId: String, position: IntOffset) {
        val index = positionedNodeIndex[nodeId] ?: return
        _positionedNodes.getOrNull(index)?.position?.value = position
    }

    fun getNodePosition(nodeId: String): IntOffset? {
        val index = positionedNodeIndex[nodeId] ?: return null
        return positionedNodes.getOrNull(index)?.position?.value
    }

    fun setPortPosition(nodeId: String, portId: String, position: IntOffset) {
        val index = positionedNodeIndex[nodeId] ?: return
        _positionedNodes.getOrNull(index)?.portPositions?.set(portId, position)

    }

    fun getPortPosition(nodeId: String, portId: String): IntOffset? {
        val index = positionedNodeIndex[nodeId] ?: return null
        return _positionedNodes.getOrNull(index)?.portPositions?.get(portId)
    }


    fun getNodeAndPortInPosition(
        position: IntOffset,
        excludeNodeId: String? = null,
        direction: PortDirection = PortDirection.Input
    ): Pair<Node, Port>? {
        return _positionedNodes.filter { excludeNodeId == null || it.node.id != excludeNodeId }
            .firstNotNullOfOrNull { positioned ->
                val ports = if (direction == PortDirection.Input) {
                    positioned.node.inputs
                } else {
                    positioned.node.outputs
                }

                ports.firstOrNull { port ->
                    val portOffset = positioned.portPositions[port.id] ?: return@firstOrNull false
                    portOffset.getDistance(position) < 15
                }?.let { port ->
                    positioned.node to port
                }
            }
    }

    private fun IntOffset.getDistance(other: IntOffset): Float {
        val dx = (x - other.x).toFloat()
        val dy = (y - other.y).toFloat()
        return hypot(dx, dy)
    }

    private fun rebuildNodeIndex() {
        nodeIndex.clear()
        _nodes.forEachIndexed { index, node ->
            nodeIndex[node.id] = index
        }
    }

    private fun rebuildPositionedNodeIndex() {
        positionedNodeIndex.clear()
        _positionedNodes.forEachIndexed { index, positioned ->
            positionedNodeIndex[positioned.node.id] = index
        }
    }
}

@Composable
fun rememberGraphState(): GraphState = remember {
    GraphState()
}