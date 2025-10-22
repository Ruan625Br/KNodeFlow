package io.jn.knodeflow.node

import io.jn.knodeflow.port.PortDefinition

data class NodeTemplate(
    val type: String,
    val name: String,
    val description: String,
    val category: Any,
    val style: NodeStyle = NodeStyle(),
    val inputDefinitions: List<PortDefinition>,
    val outputDefinitions: List<PortDefinition>,
    val factory: (id: String) -> Node
) {
    fun createInstance(id: String): Node = factory(id)
}