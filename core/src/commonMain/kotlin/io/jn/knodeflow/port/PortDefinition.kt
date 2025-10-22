package io.jn.knodeflow.port

data class PortDefinition(
    val id: String,
    val name: String,
    val type: String,
    val isRequired: Boolean = false,
    val defaultValue: Any? = null,
    val isExecutionPort: Boolean = false,
    val allowMultiple: Boolean = false
)