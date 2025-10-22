package io.jn.knodeflow.port

class PortBuilder(
    private val id: String, private val direction: PortDirection
) {
    private var name: String = id
    private var type: String = "any"
    private var portType: PortType = PortType.DATA
    private var defaultValue: Any? = null
    private var isExecutionPort: Boolean = false
    private var isRequired: Boolean = false
    private var tooltip: String = ""
    private var allowMultiple: Boolean = false
    private var validation: PortValidation? = null

    fun name(value: String): PortBuilder = apply { name = value }
    fun type(value: String): PortBuilder = apply { type = value }
    fun portType(value: PortType): PortBuilder = apply { portType = value }
    fun defaultValue(value: Any?): PortBuilder = apply { defaultValue = value }
    fun execution(): PortBuilder = apply { isExecutionPort = true }
    fun required(): PortBuilder = apply { isRequired = true }
    fun tooltip(value: String): PortBuilder = apply { tooltip = value }
    fun allowMultiple(): PortBuilder = apply { allowMultiple = true }
    fun validate(
        validator: (Any?) -> Boolean,
        errorMsg: String = "Invalid value"
    ): PortBuilder = apply {
        validation = PortValidation(validator, errorMsg)
    }

    fun build(): Port = Port(
        id = id,
        name = name,
        type = portType,
        direction = direction,
        defaultValue = defaultValue,
        isExecutionPort = isExecutionPort,
        isRequired = isRequired,
        tooltip = tooltip,
        allowMultipleConnections = allowMultiple,
        validation = validation
    )
}