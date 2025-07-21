package com.bleelblep.glyphsharge.ui.components

import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.nativeCanvas

/**
 * Helper that converts an emoji (or any single-line text) to a [Painter] so it can be passed to
 * existing card components that expect a `Painter` icon.
 *
 * The emoji will be rendered with the system emoji font so full colour is preserved. Tinting is
 * disabled at call-site by using `Color.Unspecified`.
 */
@Composable
fun rememberEmojiPainter(emoji: String, fontSizeDp: Float = 32f): Painter {
    val density = LocalDensity.current
    return remember(emoji, fontSizeDp, density) {
        object : Painter() {
            override val intrinsicSize: Size = Size.Unspecified

            override fun DrawScope.onDraw() {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textAlign = Paint.Align.CENTER
                    textSize = with(density) { fontSizeDp.dp.toPx() }
                    color = android.graphics.Color.BLACK // colour ignored for emoji glyphs
                }
                drawIntoCanvas { canvas ->
                    val x = size.width / 2f
                    val y = (size.height / 2f) - (paint.ascent() + paint.descent()) / 2f
                    canvas.nativeCanvas.drawText(emoji, x, y, paint)
                }
            }
        }
    }
} 