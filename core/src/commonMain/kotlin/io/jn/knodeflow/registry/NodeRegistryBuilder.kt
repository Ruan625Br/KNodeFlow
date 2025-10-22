package io.jn.knodeflow.registry

class NodeRegistryBuilder {
    private val nodes = mutableListOf<ExecutableNode>()

    fun node(executableNode: ExecutableNode) {
        nodes.add(executableNode)
    }

    fun nodes(vararg executableNodes: ExecutableNode) {
        nodes.addAll(executableNodes)
    }

    fun build(): NodeRegistry {
        return NodeRegistry().apply {
            registerAll(nodes)
        }
    }
}

fun buildNodeRegistry(block: NodeRegistryBuilder.() -> Unit): NodeRegistry {
    return NodeRegistryBuilder().apply(block).build()
}