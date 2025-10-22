package io.jn.knodeflow.ui.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class EditorConfig(
    val showGrid: Boolean,
    val gridSize: Float,
    val backgroundColor: Color,
    val enableZoom: Boolean,
    val enablePan: Boolean,
    val minZoom: Float,
    val maxZoom: Float,
    val showMinimap: Boolean,
    val showToolbar: Boolean,
    val enableUndo: Boolean,
    val enableRedo: Boolean
)

object KNodeFlowEditorDefaults {
    @Composable
    fun config(
        showGrid: Boolean = true,
        gridSize: Float = 50f,
        backgroundColor: Color = Color(0xFF212121),
        enableZoom: Boolean = true,
        enablePan: Boolean = true,
        minZoom: Float = 0.3f,
        maxZoom: Float = 3.0f,
        showMinimap: Boolean = false,
        showToolbar: Boolean = true,
        enableUndo: Boolean = true,
        enableRedo: Boolean = true
    ): EditorConfig {
        return EditorConfig(
            showGrid = showGrid,
            gridSize = gridSize,
            backgroundColor = backgroundColor,
            enableZoom = enableZoom,
            enablePan = enablePan,
            minZoom = minZoom,
            maxZoom = maxZoom,
            showMinimap = showMinimap,
            showToolbar = showToolbar,
            enableUndo = enableUndo,
            enableRedo = enableRedo
        )
    }
}