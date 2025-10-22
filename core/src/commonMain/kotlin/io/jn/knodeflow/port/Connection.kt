package io.jn.knodeflow.port

data class Connection(
    val fromNodeId: String, val toNodeId: String, val fromPortId: String, val toPortId: String
)
