package io.jn.knodeflow.graph

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.unit.IntOffset
import io.jn.knodeflow.node.Node

data class PositionedNode(
    val node: Node,
    val position: MutableState<IntOffset> = mutableStateOf(IntOffset.Zero),
    val portPositions: SnapshotStateMap<String, IntOffset> = mutableStateMapOf()
)
