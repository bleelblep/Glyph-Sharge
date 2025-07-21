package com.bleelblep.glyphsharge.ui.utils

import android.view.View
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition

/**
 * Animation utilities optimized for hardware acceleration and smooth performance
 */
object AnimationUtils {

    // Optimized timing constants for hardware-accelerated animations
    object Timing {
        const val FAST = 150
        const val NORMAL = 250
        const val SLOW = 350
    }

    // Scale factors for smooth transitions
    object Scale {
        const val SUBTLE_IN = 0.97f
        const val SUBTLE_OUT = 1.03f
        const val GENTLE_IN = 0.95f
        const val GENTLE_OUT = 1.05f
    }

    /**
     * Optimized easing curves for hardware acceleration
     */
    val SmoothEasing = FastOutSlowInEasing
    val QuickEasing = EaseOut
    val ResponsiveEasing = EaseIn

    /**
     * Create smooth fade + scale enter transition
     */
    fun createSmoothEnterTransition(
        duration: Int = Timing.NORMAL,
        scaleFrom: Float = Scale.SUBTLE_IN
    ): EnterTransition = fadeIn(
        animationSpec = tween(duration, easing = SmoothEasing)
    ) + scaleIn(
        animationSpec = tween(duration, easing = SmoothEasing),
        initialScale = scaleFrom
    )

    /**
     * Create smooth fade + scale exit transition
     */
    fun createSmoothExitTransition(
        duration: Int = Timing.FAST,
        scaleTo: Float = Scale.SUBTLE_OUT
    ): ExitTransition = fadeOut(
        animationSpec = tween(duration, easing = ResponsiveEasing)
    ) + scaleOut(
        animationSpec = tween(duration, easing = ResponsiveEasing),
        targetScale = scaleTo
    )

    /**
     * Enable hardware layer for view during animation (for View-based components)
     */
    fun enableHardwareLayerForAnimation(view: View) {
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    /**
     * Disable hardware layer after animation (for View-based components)
     */
    fun disableHardwareLayer(view: View) {
        view.setLayerType(View.LAYER_TYPE_NONE, null)
    }

    /**
     * Optimized spring animation specifications
     */
    object Springs {
        val Bouncy = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )

        val Smooth = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )

        val Quick = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        )
    }
} 