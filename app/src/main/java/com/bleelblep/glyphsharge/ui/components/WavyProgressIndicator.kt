package com.bleelblep.glyphsharge.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * Default values for WavyProgressIndicator components
 */
object WavyProgressIndicatorDefaults {
    val indicatorColor: Color @Composable get() = MaterialTheme.colorScheme.primary
    val trackColor: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant

    val LinearIndicatorStrokeWidth: Dp = 4.dp
    val LinearTrackStrokeWidth: Dp = 4.dp

    val LinearIndicatorTrackGapSize: Dp = 2.dp
    val LinearIndeterminateWavelength: Dp = 24.dp
}

/**
 * Linear wavy progress indicator with expressive animations
 * Following Material 3 Expressive design principles
 */
@Composable
fun LinearWavyProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = WavyProgressIndicatorDefaults.indicatorColor,
    trackColor: Color = WavyProgressIndicatorDefaults.trackColor,
    strokeWidth: Dp = WavyProgressIndicatorDefaults.LinearIndicatorStrokeWidth,
    trackStrokeWidth: Dp = WavyProgressIndicatorDefaults.LinearTrackStrokeWidth,
    gapSize: Dp = WavyProgressIndicatorDefaults.LinearIndicatorTrackGapSize,
    amplitude: Float = 1.0f,
    wavelength: Dp = WavyProgressIndicatorDefaults.LinearIndeterminateWavelength,
    waveSpeed: Dp = wavelength
) {
    val density = LocalDensity.current

    // Memoize expensive calculations that don't change frequently
    val wavelengthPx = remember(wavelength) { with(density) { wavelength.toPx() } }
    val strokeWidthPx = remember(strokeWidth) { with(density) { strokeWidth.toPx() } }
    val trackStrokeWidthPx = remember(trackStrokeWidth) { with(density) { trackStrokeWidth.toPx() } }
    val amplitudePx = remember(amplitude, strokeWidthPx) { amplitude * (strokeWidthPx / 2) * 0.8f }
    val animationDuration = remember(wavelength, waveSpeed) { 
        (1000 * wavelength.value / waveSpeed.value).toInt() 
    }

    // Animate the wave movement
    val infiniteTransition = rememberInfiniteTransition(label = "wavyProgress")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = wavelengthPx,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDuration,
                easing = LinearEasing
            )
        ),
        label = "waveOffset"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(strokeWidth.coerceAtLeast(4.dp) + gapSize * 2)
            .clipToBounds()
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2

        // Draw track (background wavy line)
        drawWavyLine(
            centerY = centerY,
            width = canvasWidth,
            wavelength = wavelengthPx,
            amplitude = amplitudePx * 0.5f, // Less amplitude for track
            offset = waveOffset * 0.3f, // Slower animation for track
            strokeWidth = trackStrokeWidthPx,
            color = trackColor
        )

        // Draw progress indicator (foreground wavy line)
        drawWavyLine(
            centerY = centerY,
            width = canvasWidth,
            wavelength = wavelengthPx,
            amplitude = amplitudePx,
            offset = waveOffset,
            strokeWidth = strokeWidthPx,
            color = color
        )
    }
}

/**
 * Linear wavy progress indicator with determinate progress
 * Following Material 3 Expressive design principles
 */
@Composable
fun LinearWavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = WavyProgressIndicatorDefaults.indicatorColor,
    trackColor: Color = WavyProgressIndicatorDefaults.trackColor,
    strokeWidth: Dp = WavyProgressIndicatorDefaults.LinearIndicatorStrokeWidth,
    trackStrokeWidth: Dp = WavyProgressIndicatorDefaults.LinearTrackStrokeWidth,
    gapSize: Dp = WavyProgressIndicatorDefaults.LinearIndicatorTrackGapSize,
    amplitude: Float = 1.0f,
    wavelength: Dp = WavyProgressIndicatorDefaults.LinearIndeterminateWavelength,
    waveSpeed: Dp = wavelength
) {
    val density = LocalDensity.current

    // Memoize expensive calculations that don't change frequently
    val wavelengthPx = remember(wavelength) { with(density) { wavelength.toPx() } }
    val strokeWidthPx = remember(strokeWidth) { with(density) { strokeWidth.toPx() } }
    val trackStrokeWidthPx = remember(trackStrokeWidth) { with(density) { trackStrokeWidth.toPx() } }
    val amplitudePx = remember(amplitude, strokeWidthPx) { amplitude * (strokeWidthPx / 2) * 0.8f }
    val animationDuration = remember(wavelength, waveSpeed) { 
        (1000 * wavelength.value / waveSpeed.value).toInt() 
    }

    // Animate the wave movement
    val infiniteTransition = rememberInfiniteTransition(label = "wavyProgressDeterminate")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = wavelengthPx,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDuration,
                easing = LinearEasing
            )
        ),
        label = "waveOffset"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(strokeWidth.coerceAtLeast(4.dp) + gapSize * 2)
            .clipToBounds()
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerY = canvasHeight / 2

        // Draw track (full width background)
        drawWavyLine(
            centerY = centerY,
            width = canvasWidth,
            wavelength = wavelengthPx,
            amplitude = amplitudePx * 0.5f,
            offset = waveOffset * 0.3f,
            strokeWidth = trackStrokeWidthPx,
            color = trackColor
        )

        // Draw progress indicator (clipped to progress width)
        val progressWidth = canvasWidth * progress.coerceIn(0f, 1f)
        if (progressWidth > 0) {
            drawWavyLine(
                centerY = centerY,
                width = progressWidth,
                wavelength = wavelengthPx,
                amplitude = amplitudePx,
                offset = waveOffset,
                strokeWidth = strokeWidthPx,
                color = color
            )
        }
    }
}

/**
 * Draw a wavy line using Canvas with optimized performance for animations
 * Uses cached path calculations and reduced sine computations
 */
private fun DrawScope.drawWavyLine(
    centerY: Float,
    width: Float,
    wavelength: Float,
    amplitude: Float,
    offset: Float,
    strokeWidth: Float,
    color: Color
) {
    val path = Path()
    
    // Optimize step size based on width for better performance/quality balance
    val stepSize = (width / 100f).coerceAtLeast(1f).coerceAtMost(4f)
    val twoPiOverWavelength = 2 * PI / wavelength
    
    // Pre-calculate offset factor
    val offsetFactor = offset * twoPiOverWavelength
    
    // Start from the left edge
    var x = 0f
    val startY = centerY + amplitude * sin(offsetFactor).toFloat()
    path.moveTo(x, startY)

    // Draw the wavy line with optimized calculations
    while (x <= width) {
        val y = centerY + amplitude * sin((x * twoPiOverWavelength) + offsetFactor).toFloat()
        path.lineTo(x, y)
        x += stepSize
    }
    
    // Ensure we end exactly at the width
    if (x - stepSize < width) {
        val finalY = centerY + amplitude * sin((width * twoPiOverWavelength) + offsetFactor).toFloat()
        path.lineTo(width, finalY)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * Preview component demonstrating different wavy progress indicator configurations
 * Showcasing Material 3 Expressive design patterns
 */
@Preview(showBackground = true)
@Composable
fun WavyProgressIndicatorPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Material 3 Expressive Wavy Progress Indicators",
                style = MaterialTheme.typography.headlineSmall
            )

            // Indeterminate progress indicator
            Text(
                text = "Indeterminate (flowing waves)",
                style = MaterialTheme.typography.bodyMedium
            )
            LinearWavyProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )

            // Determinate progress with different amplitudes
            Text(
                text = "30% Progress - High amplitude",
                style = MaterialTheme.typography.bodyMedium
            )
            LinearWavyProgressIndicator(
                progress = 0.3f,
                amplitude = 1.0f,
                wavelength = 32.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "60% Progress - Medium amplitude",
                style = MaterialTheme.typography.bodyMedium
            )
            LinearWavyProgressIndicator(
                progress = 0.6f,
                amplitude = 0.6f,
                wavelength = 24.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Text(text = "90% Progress - Low amplitude", style = MaterialTheme.typography.bodyMedium)
            LinearWavyProgressIndicator(
                progress = 0.9f,
                amplitude = 0.3f,
                wavelength = 16.dp,
                modifier = Modifier.fillMaxWidth()
            )

            // Fast wave speed
            Text(text = "Fast wave speed", style = MaterialTheme.typography.bodyMedium)
            LinearWavyProgressIndicator(
                progress = 0.75f,
                amplitude = 0.8f,
                wavelength = 20.dp,
                waveSpeed = 10.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
} 