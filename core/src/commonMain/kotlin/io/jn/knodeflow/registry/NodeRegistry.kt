package io.jn.knodeflow.registry

import io.jn.knodeflow.node.Node
import io.jn.knodeflow.node.NodeTemplate
import io.jn.knodeflow.runtime.engine.ExecutionContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class NodeRegistry {
    private val templates = mutableMapOf<String, NodeTemplate>()
    private val executableNodes = mutableMapOf<String, ExecutableNode>()
    private val categories = mutableMapOf<Any, MutableList<String>>()

    fun register(executableNode: ExecutableNode) {
        val template = executableNode.template
        templates[template.type] = template
        executableNodes[template.type] = executableNode

        categories.getOrPut(template.category) { mutableListOf() }.add(template.type)
    }

    fun registerAll(nodes: List<ExecutableNode>) {
        nodes.forEach { register(it) }
    }

    @OptIn(ExperimentalUuidApi::class)
    fun createNode(type: String, id: String = Uuid.random().toString()): Node? {
        return templates[type]?.createInstance(id)
    }

    fun getExecutable(type: String): ExecutableNode? = executableNodes[type]

    fun getTemplate(type: String): NodeTemplate? = templates[type]

    fun getAllTypes(): List<String> = templates.keys.toList()

    fun getTemplatesByCategory(category: Any): List<NodeTemplate> {
        return categories[category]?.mapNotNull { templates[it] } ?: emptyList()
    }

    fun getTemplates(): List<NodeTemplate> {
        return templates.values.toList()
    }


    fun searchTemplate(query: String): List<NodeTemplate> {
        val lowerQuery = query.lowercase()
        return templates.values.filter {
            it.name.lowercase().contains(lowerQuery) || it.description.lowercase()
                .contains(lowerQuery) || it.type.lowercase().contains(lowerQuery)
        }
    }

    fun getCategories(): List<Any> = categories.keys.toList()

    fun hasType(type: String): Boolean = templates.containsKey(type)

    fun unregister(type: String) {
        val template = templates.remove(type)
        executableNodes.remove(type)
        template?.let {
            categories[it.category]?.remove(type)
        }
    }

}

interface ExecutableNode {
    val template: NodeTemplate

    suspend fun execute(node: Node, context: ExecutionContext): Map<String, Any?>
    fun validate(node: Node): List<String> = emptyList()
}
