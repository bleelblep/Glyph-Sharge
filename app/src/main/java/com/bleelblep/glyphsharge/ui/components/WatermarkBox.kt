package com.bleelblep.glyphsharge.ui.components

import android.graphics.Paint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.hypot
import kotlin.math.max

/**
 * WatermarkBox – light-weight Compose implementation of the diagonal repeating watermark.
 *
 * Usage:
 * ```kotlin
 * WatermarkBox(enabled = true, text = "") {
 *     // your UI here
 * }
 * ```
 *
 * All parameters have sensible defaults so the watermark can be toggled with a single flag.
 */
@Composable
fun WatermarkBox(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String = "TESTING",
    color: Color = Color.Gray,
    alpha: Float = 0.12f,
    rotation: Float = 315f,
    fontSize: TextUnit = 14.sp,
    spacing: Dp = 48.dp,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val spacingPx = with(density) { spacing.toPx() }
    val paint = rememberPaint(color, alpha, fontSize, density)

    // Use a top-level Box so our canvas sits above children.
    Box(modifier = modifier.fillMaxSize()) {
        content()

        if (enabled) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val textWidth = paint.measureText(text)
                val step = textWidth + spacingPx
                val diag = hypot(size.width, size.height)
                val reps = (diag / step).toInt() + 2 // a bit extra for safety

                drawIntoCanvas { canvas ->
                    val nativeCanvas = canvas.nativeCanvas
                    nativeCanvas.save()
                    // Rotate around centre so we can iterate in regular grid coordinates.
                    nativeCanvas.rotate(rotation, size.width / 2, size.height / 2)

                    val startX = -reps * step
                    val startY = -reps * step

                    for (i in 0..reps * 2) {
                        for (j in 0..reps * 2) {
                            val x = startX + i * step
                            val y = startY + j * step
                            nativeCanvas.drawText(text, x, y, paint)
                        }
                    }
                    nativeCanvas.restore()
                }
            }
        }
    }
}

@Composable
private fun rememberPaint(color: Color, alpha: Float, fontSize: TextUnit, density: androidx.compose.ui.unit.Density): Paint {
    // Recreate only when parameters change.
    return androidx.compose.runtime.remember(color, alpha, fontSize) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color.copy(alpha = alpha).toArgb()
            textSize = with(density) { fontSize.toPx() }
            isFakeBoldText = true
        }
    }
} 