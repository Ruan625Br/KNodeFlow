package io.jn.knodeflow.sample.nodes

import io.jn.knodeflow.node.Node
import io.jn.knodeflow.node.NodeTemplate
import io.jn.knodeflow.port.PortDefinition
import io.jn.knodeflow.runtime.engine.ExecutionContext
import io.jn.knodeflow.registry.ExecutableNode

object ForLoopNode : ExecutableNode {
    override val template = NodeTemplate(
        type = "control.for",
        name = "For Loop",
        category = "control.for",
        description = "Executes loop body for a range of values",
        inputDefinitions = listOf(
            PortDefinition("exec", "Execute", "exec", isExecutionPort = true),
            PortDefinition("start", "Start", "int", defaultValue = 0),
            PortDefinition("end", "End", "int", defaultValue = 10),
            PortDefinition("step", "Step", "int", defaultValue = 1)
        ),
        outputDefinitions = listOf(
            PortDefinition("loopBody", "Loop Body", "exec", isExecutionPort = true),
            PortDefinition("completed", "Completed", "exec", isExecutionPort = true),
            PortDefinition("index", "Index", "int")
        ),
        factory = { id ->
            Node(
                id = id,
                name = "For Loop",
                type = "control.for",
                inputs = listOf(
                    Ports.executionInput("exec"),
                    Ports.intInput("start", "Start", 0),
                    Ports.intInput("end", "End", 10),
                    Ports.intInput("step", "Step", 1)
                ),
                outputs = listOf(
                    Ports.executionOutput("loopBody", "Loop Body"),
                    Ports.executionOutput("completed", "Completed"),
                    Ports.output("index", "Index")
                )
            )
        }
    )

    override suspend fun execute(node: Node, context: ExecutionContext): Map<String, Any?> {
        val start = context.getInputValue(node, "start") as? Int ?: 0
        val end = context.getInputValue(node, "end") as? Int ?: 10
        val step = context.getInputValue(node, "step") as? Int ?: 1

        for (i in start until end step step) {
            context.setOutputValue(node, "index", i)

            context.continueExecution(node, "loopBody")
        }

        context.continueExecution(node, "completed")
        return emptyMap()
    }
}
