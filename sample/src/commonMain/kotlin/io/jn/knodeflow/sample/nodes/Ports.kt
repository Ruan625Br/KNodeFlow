package io.jn.knodeflow.sample.nodes

import io.jn.knodeflow.port.PortBuilder
import io.jn.knodeflow.port.PortDirection
import io.jn.knodeflow.port.PortType

object Ports {
    fun executionInput(id: String, name: String = "Execute") =
        PortBuilder(id, PortDirection.Input)
            .name(name)
            .portType(PortType.EXEC)
            .execution()
            .build()

    fun executionOutput(id: String, name: String = "Then") =
        PortBuilder(id, PortDirection.Output)
            .name(name)
            .portType(PortType.EXEC)
            .execution()
            .build()

    fun intInput(id: String, name: String, defaultValue: Int? = null, required: Boolean = false) =
        PortBuilder(id, PortDirection.Input)
            .name(name)
            .type("int")
            .defaultValue(defaultValue)
            .apply { if (required) required() }
            .build()

    fun stringInput(id: String, name: String, defaultValue: String? = null, required: Boolean = false) =
        PortBuilder(id, PortDirection.Input)
            .name(name)
            .type("string")
            .defaultValue(defaultValue)
            .apply { if (required) required() }
            .build()

    fun boolInput(id: String, name: String, defaultValue: Boolean? = null) =
        PortBuilder(id, PortDirection.Input)
            .name(name)
            .type("bool")
            .defaultValue(defaultValue)
            .build()

    fun output(id: String, name: String, portType: PortType = PortType.EXEC) =
        PortBuilder(id, PortDirection.Output)
            .name(name)
            .portType(portType)
            .build()
}