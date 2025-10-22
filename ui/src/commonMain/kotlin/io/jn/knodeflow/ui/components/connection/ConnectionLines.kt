package io.jn.knodeflow.ui.components.connection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toOffset
import io.jn.knodeflow.port.Port
import io.jn.knodeflow.port.PortDirection


@Composable
fun ConnectionLines(
    ports: List<Port>,
    getPortPosition: (String, String, PortDirection) -> IntOffset?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        ports.forEach { port ->
            port.connection?.let { connection ->

                val from = getPortPosition(
                    connection.fromNodeId,
                    connection.fromPortId,
                    PortDirection.Output
                )
                val to =
                    getPortPosition(connection.toNodeId, connection.toPortId, PortDirection.Input)

                if (from != null && to != null) {
                    ConnectionLine(
                        from = from.toOffset(), to = to.toOffset()
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionLine(from: Offset, to: Offset, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val path = calculateBezier(from, to)
        drawPath(
            path = path,
            color = Color.Gray,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

fun calculateBezier(from: Offset, to: Offset): Path {
    val path = Path()

    path.moveTo(from.x, from.y)

    val dx = 110f // quanto mais alto, mais aberta a curva
    val dy = (to.y - from.y) / 2f

    val ctrl1: Offset
    val ctrl2: Offset

    if (to.x >= from.x - 6f) {
        // Conexão normal para direita (horizontal)
        ctrl1 = Offset(from.x + dx, from.y)
        ctrl2 = Offset(to.x - dx, to.y)
    } else {
        // Conexão "para trás" (curva redonda para a esquerda)
        ctrl1 = Offset(from.x + dx, from.y + dy)
        ctrl2 = Offset(to.x - dx, to.y - dy)
    }

    path.cubicTo(
        ctrl1.x, ctrl1.y,
        ctrl2.x, ctrl2.y,
        to.x, to.y
    )

    return path
}
