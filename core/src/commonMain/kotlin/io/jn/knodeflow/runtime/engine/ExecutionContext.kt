package io.jn.knodeflow.runtime.engine

import io.jn.knodeflow.node.Node
import io.jn.knodeflow.registry.NodeRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

class ExecutionContext(
    val nodes: List<Node>,
    val registry: NodeRegistry,
    coroutineContext: CoroutineContext = Dispatchers.Default
) {
    private val scope = CoroutineScope(coroutineContext + SupervisorJob())

    private val variables = mutableMapOf<String, Any?>()
    private val outputCache = mutableMapOf<String, MutableMap<String, Any?>>()
    private val nodeStates = mutableMapOf<String, MutableMap<String, Any?>>()
    private val executionHistory = mutableListOf<ExecutionStep>()

    private var isExecuting = false
    private var shouldStop = false


    fun getVariable(name: String): Any? = variables[name]

    fun setVariable(name: String, value: Any?) {
        variables[name] = value
        logStep(ExecutionStep.VariableSet(name, variables))
    }

    fun getInputValue(node: Node, portId: String): Any? {
        val port = node.inputs.find { it.id == portId } ?: return null

        port.connection?.let { connection ->
            val sourceNodeId = connection.fromNodeId
            val sourcePortId = connection.fromPortId

            return outputCache[sourceNodeId]?.get(sourcePortId)
        }

        return port.getEffectiveValue()
    }

    fun setOutputValue(node: Node, portId: String, value: Any?) {
        outputCache.getOrPut(node.id) { mutableMapOf() }[portId] = value
        logStep(ExecutionStep.OutputSet(node.id, portId, value))
    }

    fun updateNodeState(nodeId: String, key: String, value: Any?) {
        nodeStates.getOrPut(nodeId) { mutableMapOf() }[key] = value
    }

    fun getNodeState(nodeId: String): Map<String, Any?> {
        return nodeStates[nodeId] ?: emptyMap()
    }

    suspend fun continueExecution(node: Node, executionPortId: String) {
        if (shouldStop) return

        val port = node.outputs.find { it.id == executionPortId } ?: return

        val connections = if (port.allowMultipleConnections) {
            port.connections
        } else {
            listOfNotNull(port.connection)
        }

        connections.forEach { connection ->
            val nextNode = nodes.find { it.id == connection.toNodeId } ?: return@forEach
            executeNode(nextNode)
        }
    }

    suspend fun executeNode(node: Node) {
        if (shouldStop) return

        logStep(ExecutionStep.NodeStarted(node.id, node.name))

        try {
            val executable = registry.getExecutable(node.type)
                ?: throw IllegalStateException("Node executable found for type: ${node.type}")

            val errors = executable.validate(node)

            if (errors.isNotEmpty()) {
                logStep(
                    ExecutionStep.NodeError(
                        node.id, "Validation filed: ${errors.joinToString()}"
                    )
                )
                return
            }

            val nodeWithState = nodeStates[node.id]?.let { state ->
                node.copy(
                    internalState = node.internalState + state
                )
            } ?: node

            val result = if (node.isAsync) {
                executable.execute(nodeWithState, this@ExecutionContext)
            } else {
                executable.execute(nodeWithState, this)
            }

            result.forEach { (portId, value) ->
                setOutputValue(node, portId, value)
            }

            logStep(ExecutionStep.NodeCompleted(node.id, node.name, result))
        } catch (e: Exception) {
            logStep(ExecutionStep.NodeError(node.id, e.message ?: "Unknow errror"))
            throw e
        }
    }

    suspend fun execute(startNode: Node): Map<String, Any?> {
        if (isExecuting) {
            throw IllegalStateException("Execution already in progress")
        }

        try {
            logStep(ExecutionStep.ExecutionStarted(startNode.id))
            executeNode(startNode)
            logStep(ExecutionStep.ExecutionCompleted)

            return outputCache[startNode.id] ?: emptyMap()
        } finally {
            isExecuting = false
        }
    }

    fun stop() {
        shouldStop = true
    }

    fun reset() {
        variables.clear()
        outputCache.clear()
        nodeStates.clear()
        executionHistory.clear()
    }

    fun dispose() {
        scope.cancel()
    }

    fun getExecutionHistory(): List<ExecutionStep> = executionHistory.toList()

    private fun logStep(step: ExecutionStep) {
        executionHistory.add(step)
    }
}
