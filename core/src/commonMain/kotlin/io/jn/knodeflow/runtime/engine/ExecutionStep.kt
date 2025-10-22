package io.jn.knodeflow.runtime.engine

sealed class ExecutionStep {
    data class ExecutionStarted(val startNodeId: String) : ExecutionStep()

    object ExecutionCompleted : ExecutionStep()

    data class NodeStarted(val nodeId: String, val nodeName: String) : ExecutionStep()

    data class NodeCompleted(
        val nodeId: String, val nodeName: String, val outputs: Map<String, Any?>
    ) : ExecutionStep()

    data class NodeError(
        val nodeId: String, val error: String
    ) : ExecutionStep()

    data class OutputSet(
        val nodeId: String, val portId: String, val value: Any?
    ) : ExecutionStep()

    data class VariableSet(
        val name: String, val value: Any?
    ) : ExecutionStep()
}