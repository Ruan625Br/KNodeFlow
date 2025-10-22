package io.jn.knodeflow.sample.nodes

import io.jn.knodeflow.node.Node
import io.jn.knodeflow.node.NodeTemplate
import io.jn.knodeflow.port.Port
import io.jn.knodeflow.port.PortDefinition
import io.jn.knodeflow.port.PortDirection
import io.jn.knodeflow.port.PortType
import io.jn.knodeflow.runtime.engine.ExecutionContext
import io.jn.knodeflow.registry.ExecutableNode

object OnStartNode : ExecutableNode {

    const val ID = "events.OnStart"

    override val template = NodeTemplate(
        type = "events.onStart",
        name = "On Start",
        category = "events",
        description = "Triggers when the graph starts executing",
        inputDefinitions = emptyList(),
        outputDefinitions = listOf(
            PortDefinition(
                "then", "Then", "exec", isExecutionPort = true
            )
        ),
        factory = {
            Node(
                id = ID,
                name = "On Start",
                type = "events.onStart",
                outputs = listOf(
                    Port(
                        id = "then",
                        name = "Then",
                        type = PortType.EXEC,
                        direction = PortDirection.Output
                    )
                )
            )
        }
    )

    override suspend fun execute(
        node: Node,
        context: ExecutionContext
    ): Map<String, Any?> {
        context.continueExecution(node, "then")
        return emptyMap()
    }

}