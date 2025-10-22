package io.jn.knodeflow.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.jn.knodeflow.node.NodeTemplate

@Composable
fun AddNodeDialog(
    nodes: List<NodeTemplate>,
    position: IntOffset,
    onSelected: (NodeTemplate) -> Unit,
    modifier: Modifier = Modifier
){
    val widthDp = 200.dp
    val heightDp = 300.dp
    val density = LocalDensity.current


    val offsetDp = with(density) {
        IntOffset(
            x = position.x - (widthDp.toPx() / 2).toInt(),
            y = position.y// - (heightDp.toPx() / 2).toInt()
        )
    }

    Column(
        modifier = modifier
            .width(200.dp)
            .offset { offsetDp }
            .background(Color(0xFF151515), MaterialTheme.shapes.small)

    ) {
        nodes.forEach { node ->
            NodeItem(
                node = node,
                onClick = { onSelected(node) })
        }
    }
}

@Composable
private fun NodeItem(
    node: NodeTemplate,
    onClick: () -> Unit,
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }

    ) {
        Text(
            modifier = Modifier
                .padding(8.dp),
            text = node.name,
            color = Color.White
        )
    }
}