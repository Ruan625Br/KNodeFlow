package io.jn.knodeflow.runtime.engine

import io.jn.knodeflow.node.Node
import io.jn.knodeflow.registry.NodeRegistry

class GraphExecutor(
    private val nodes: List<Node>,
    private val registry: NodeRegistry,
) {

    suspend fun execute(startNode: Node): Map<String, Any?> {
        val context = ExecutionContext(nodes, registry)

        return try {
            context.execute(startNode)
        } finally {
            context.dispose()
        }
    }

    /*private val context = ExecutionContext(
        graph = nodes,
        executor = this
    )

    public fun execute(type: KClass<*>): Map<String, Any?> {
        val targetNode = nodes.find { it.type == type.qualifiedName } ?: return emptyMap()
        return execute(targetNode, mutableSetOf())
    }

    public fun execute(targetNode: Node, forceExecute: Boolean = false): Map<String, Any?> {
        return execute(targetNode, mutableSetOf(), forceExecute)
    }

    private fun execute(
        targetNode: Node,
        visited: MutableSet<String>,
        forceExecute: Boolean = false
    ): Map<String, Any?> {
        if (!forceExecute && context.hasOutput(targetNode.id)) return context.getNodeOutputs(targetNode.id)

        if (visited.contains(targetNode.id)) {
            println("Cycle detected at node: ${targetNode.id}")
        } else {
            println("Executing node: ${targetNode.id}")
        }

        visited.add(targetNode.id)

        val resolvedInputs = targetNode.inputs.associate { inputPort ->
            val conn = inputPort.connection
            val value = if (conn == null) {
                inputPort.value
            } else if (inputPort.type == PortType.EXEC) {
                null
            } else {
                val sourceNode = nodes.find { it.id == conn.fromNodeId }
                    ?: error("Source node not found: ${conn.fromNodeId}")

                if (!context.hasOutput(sourceNode.id)) {
                    execute(sourceNode)
                }

                context.getOutput(sourceNode.id, conn.fromPortId)
            }

            inputPort.id to value
        }


        val exec = executableNodes[targetNode.type]
            ?: error("Node type not found: ${targetNode.type}")

        val result = exec.execute(targetNode, context)

        context.saveOutputs(targetNode.id, result)
        visited.remove(targetNode.id)
        return result
    }*/
}
