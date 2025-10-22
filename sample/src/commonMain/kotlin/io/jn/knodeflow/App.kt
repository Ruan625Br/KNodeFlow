package io.jn.knodeflow

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.jn.knodeflow.graph.rememberGraphState
import io.jn.knodeflow.registry.buildNodeRegistry
import io.jn.knodeflow.runtime.engine.GraphExecutor
import io.jn.knodeflow.sample.nodes.ForLoopNode
import io.jn.knodeflow.sample.nodes.OnStartNode
import io.jn.knodeflow.sample.nodes.PrintNode
import io.jn.knodeflow.ui.editor.KNodeFlowEditor
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.uuid.ExperimentalUuidApi

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class, ExperimentalUuidApi::class
)
@Composable
@Preview
fun App() {
    var result by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val registry = remember {
        buildNodeRegistry {
            nodes(
                OnStartNode, PrintNode, ForLoopNode
            )
        }
    }

    val graph = rememberGraphState()


    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        KNodeFlowEditor(
            registry = registry, graph = graph
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {

            Text(
                text = result,
                color = Color.White,

                )

            Button(
                {
                    coroutineScope.launch {
                        val executor = GraphExecutor(graph.nodes, registry)
                        val resultMap =
                            executor.execute(graph.nodes.find { it.id == OnStartNode.ID }!!)
                        println("Execution result: $resultMap")
                        result = flattenAndJoinResult(resultMap)
                    }
                }) {
                Text("Execute")
            }
        }
    }
}

fun flattenAndJoinResult(resultMap: Map<String, Any?>, key: String = "result"): String {
    val raw = resultMap[key] ?: return ""

    val flattened = when (raw) {
        is List<*> -> raw.flatMap {
            when (it) {
                is List<*> -> it
                else -> listOf(it)
            }
        }

        else -> listOf(raw)
    }

    return flattened.joinToString(separator = ", ") { it.toString() }
}