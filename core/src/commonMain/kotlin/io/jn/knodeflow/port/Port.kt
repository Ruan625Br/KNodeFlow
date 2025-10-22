package io.jn.knodeflow.port

data class Port(
    val id: String,
    val name: String,
    val type: PortType = PortType.DATA,
    val direction: PortDirection,
    val typeName: String = "",

    val value: Any? = null,
    val defaultValue: Any? = null,

    val connection: Connection? = null,
    val allowMultipleConnections: Boolean = false,
    val connections: List<Connection> = emptyList(),

    val isExecutionPort: Boolean = false,
    val isRequired: Boolean = false,
    val isVisible: Boolean = true,
    val isEditable: Boolean = true,

    val tooltip: String = "",
    val placeholder: String = "",

    val validation: PortValidation? = null,

    val metadata: Map<String, Any?> = emptyMap()
) {

    fun isConnected(): Boolean = connection != null || connections.isNotEmpty()

    fun canAcceptConnection(): Boolean = !isConnected() || allowMultipleConnections

    fun validateValue(value: Any?): Boolean {
        if (!isRequired && value == null) return true
        if (isRequired && value == null) return false

        return validation?.validator?.invoke(value) ?: true
    }

    fun getEffectiveValue(): Any? = value ?: defaultValue


    fun addConnection(conn: Connection): Port {
        return if (allowMultipleConnections) {
            copy(connections = connections + conn)
        } else {
            copy(connection = conn)
        }
    }

    fun removeConnection(connToRemove: Connection): Port {
        return copy(
            connection = if (connection == connToRemove) null else connection,
            connections = connections.filter { it != connToRemove })
    }
}

enum class PortType {
    DATA, EXEC
}

