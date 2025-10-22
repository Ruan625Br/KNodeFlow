package io.jn.knodeflow.node

import io.jn.knodeflow.port.Port

data class Node(
    val id: String,
    val name: String,
    val type: String,
    val inputs: List<Port> = emptyList(),
    val outputs: List<Port> = emptyList(),
    val style: NodeStyle = NodeStyle(),

    val description: String = "",
    val isCollapsible: Boolean = false,
    val isCollapsed: Boolean = false,

    val internalState: Map<String, Any?> = emptyMap(),

    val canExecute: Boolean = true,
    val isAsync: Boolean = false,

    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)