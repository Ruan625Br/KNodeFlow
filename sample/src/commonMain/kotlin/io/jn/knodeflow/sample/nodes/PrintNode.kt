package io.jn.knodeflow.sample.nodes

import io.jn.knodeflow.node.Node
import io.jn.knodeflow.node.NodeTemplate
import io.jn.knodeflow.port.PortBuilder
import io.jn.knodeflow.port.PortDefinition
import io.jn.knodeflow.port.PortDirection
import io.jn.knodeflow.runtime.engine.ExecutionContext
import io.jn.knodeflow.registry.ExecutableNode

object PrintNode : ExecutableNode {
    override val template = NodeTemplate(
        type = "io.print",
        name = "Print",
        category = "io",
        description = "Prints a value to console",
        inputDefinitions = listOf(
            PortDefinition("exec", "Execute", "exec", isExecutionPort = true),
            PortDefinition("value", "Value", "any")
        ),
        outputDefinitions = listOf(
            PortDefinition("then", "Then", "exec", isExecutionPort = true)
        ),
        factory = { id ->
            Node(
                id = id,
                name = "Print",
                type = "io.print",
                inputs = listOf(
                    Ports.executionInput("exec"),
                    PortBuilder("value", PortDirection.Input)
                        .name("Value")
                        .build()
                ),
                outputs = listOf(
                    Ports.executionOutput("then")
                )
            )
        }
    )

    override suspend fun execute(node: Node, context: ExecutionContext): Map<String, Any?> {
        val value = context.getInputValue(node, "value")
        println(value)

        context.continueExecution(node, "then")
        return emptyMap()
    }
}
