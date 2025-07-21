package com.bleelblep.glyphsharge.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import java.lang.ref.WeakReference

/**
 * WatermarkHelper - A utility class for adding configurable watermark overlays to Android activities.
 * 
 * Features:
 * - 45° repeating text overlay
 * - Configurable text, color, rotation, and spacing
 * - BuildConfig integration for conditional enabling
 * - Performance optimized with view recycling
 * - Easy enable/disable functionality
 * 
 * Usage:
 * ```kotlin
 * // Configure and enable
 * WatermarkHelper.configure { 
 *     text = "PREVIEW"
 *     spacing = 150 
 * }.enable()
 * 
 * // BuildConfig approach
 * if (BuildConfig.WATERMARK_ENABLED) {
 *     WatermarkHelper.enable()
 * }
 * 
 * // Disable watermark
 * WatermarkHelper.disable()
 * ```
 */
object WatermarkHelper {
    
    /**
     * Configuration data class for watermark properties
     */
    data class WatermarkConfig(
        var text: String = "kaushik",
        var textColor: Int = Color.GRAY,
        var rotationAngle: Float = 315f,
        var spacing: Int = 50, // spacing in dp
        var alpha: Float = 0.2f,
        var textSize: Float = 24f, // text size in sp
        var useThemeBasedColor: Boolean = true // automatically adapt to light/dark theme
    )
    
    private var config = WatermarkConfig()
    private var isEnabled = false
    private val activeOverlays = mutableMapOf<String, WeakReference<WatermarkData>>()
    
    /**
     * Data class to hold watermark overlay reference
     */
    private data class WatermarkData(
        val overlay: WatermarkOverlay
    )
    
    /**
     * Configure watermark properties using DSL syntax
     */
    fun configure(block: WatermarkConfig.() -> Unit): WatermarkHelper {
        config.apply(block)
        return this
    }
    
    /**
     * Enable watermark with current configuration
     */
    fun enable(): WatermarkHelper {
        isEnabled = true
        return this
    }
    
    /**
     * Disable watermark and remove from all activities
     */
    fun disable(): WatermarkHelper {
        isEnabled = false
        removeAllOverlays()
        return this
    }
    
    /**
     * Check if watermark is currently enabled
     */
    fun isEnabled(): Boolean = isEnabled
    
    /**
     * Add watermark overlay to the specified activity above dialogs using decor view
     */
    fun addToActivity(activity: Activity) {
        if (!isEnabled) return
        
        val activityKey = activity.javaClass.simpleName + "@" + activity.hashCode()
        
        // Remove existing overlay if present
        removeOverlay(activityKey, activity)
        
        val overlay = WatermarkOverlay(activity, config)
        
        // Add to window's decor view (root of window hierarchy - above dialogs)
        val decorView = activity.window.decorView as ViewGroup
        val layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        
        // Set maximum elevation to appear above dialogs
        overlay.elevation = Float.MAX_VALUE
        overlay.translationZ = Float.MAX_VALUE
        
        decorView.addView(overlay, layoutParams)
        
        // Store references for cleanup
        activeOverlays[activityKey] = WeakReference(WatermarkData(overlay))
    }
    
    /**
     * Remove watermark overlay from the specified activity
     */
    fun removeFromActivity(activity: Activity) {
        val activityKey = activity.javaClass.simpleName + "@" + activity.hashCode()
        removeOverlay(activityKey, activity)
    }
    
    /**
     * Remove all watermark overlays
     */
    private fun removeAllOverlays() {
        activeOverlays.keys.toList().forEach { key ->
            removeOverlay(key, null)
        }
    }
    
    /**
     * Remove specific overlay by key
     */
    private fun removeOverlay(key: String, activity: Activity?) {
        activeOverlays[key]?.get()?.let { watermarkData ->
            try {
                // Remove from parent view (either decor view or content view)
                (watermarkData.overlay.parent as? ViewGroup)?.removeView(watermarkData.overlay)
            } catch (e: Exception) {
                // Ignore removal errors (view might already be removed)
            }
        }
        activeOverlays.remove(key)
    }
    
    /**
     * Initialize watermark based on debug mode
     */
    fun initializeFromBuildConfig() {
        // Note: Replace with BuildConfig.WATERMARK_ENABLED when available
        // For now, this can be manually controlled
        // enable()
    }
}

/**
 * Custom overlay view that renders the watermark pattern
 */
@SuppressLint("ViewConstructor")
class WatermarkOverlay(
    context: Context,
    private val config: WatermarkHelper.WatermarkConfig
) : FrameLayout(context) {
    
    private val paint = Paint().apply {
        isAntiAlias = true
        color = getThemeBasedColor()
        alpha = (255 * config.alpha).toInt()
        textSize = dpToPx(config.textSize)
        typeface = Typeface.DEFAULT_BOLD
    }
    
    private val textBounds = Rect()
    private val matrix = Matrix()
    
    init {
        // Make view non-clickable to allow touch events to pass through
        isClickable = false
        isFocusable = false
        setWillNotDraw(false)
        
        // Measure text bounds for spacing calculations
        paint.getTextBounds(config.text, 0, config.text.length, textBounds)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (width == 0 || height == 0) return
        
        val spacingPx = dpToPx(config.spacing.toFloat())
        val textWidth = textBounds.width()
        val textHeight = textBounds.height()
        
        // Calculate diagonal spacing for rotation with optimized text spacing
        // Use an even smaller portion of text width for extremely dense layout
        val textSpacingPadding = textWidth * 0.15f // Reduced from 30% to 15% for extremely dense text
        val totalSpacing = spacingPx + textSpacingPadding
        val diagonalSpacing = totalSpacing * 1.414f // sqrt(2) for diagonal
        
        // Calculate how many repetitions we need to cover the screen (extended for corners)
        val maxDimension = maxOf(width, height)
        val diagonalDimension = kotlin.math.sqrt((width * width + height * height).toDouble()).toFloat()
        val repetitions = (diagonalDimension / diagonalSpacing * 5.0).toInt() + 25 // Increased multiplier from 3.5 to 5.0 and base from 18 to 25
        
        canvas.save()
        
        // Apply rotation around center
        canvas.rotate(config.rotationAngle, width / 2f, height / 2f)
        
        // Draw tiled watermark pattern with extended coverage
        val centerOffsetX = width / 2f
        val centerOffsetY = height / 2f
        
        for (i in -repetitions..repetitions) {
            for (j in -repetitions..repetitions) {
                val x = i * diagonalSpacing - centerOffsetX
                val y = j * diagonalSpacing - centerOffsetY
                
                // Massively extended bounds to ensure complete corner coverage
                val margin = maxOf(textWidth, textHeight) * 10 // Increased margin from 8 to 10
                val extendedWidth = width + margin * 2
                val extendedHeight = height + margin * 2
                
                if (x >= -margin && x <= extendedWidth &&
                    y >= -margin && y <= extendedHeight) {
                    canvas.drawText(config.text, x, y, paint)
                }
            }
        }
        
        canvas.restore()
    }
    
    /**
     * Get color based on current theme (dark text on light theme, light text on dark theme)
     */
    private fun getThemeBasedColor(): Int {
        return if (config.useThemeBasedColor) {
            when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    // Dark theme - use light text
                    Color.parseColor("#66FFFFFF") // Semi-transparent white
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    // Light theme - use dark text
                    Color.parseColor("#66000000") // Semi-transparent black
                }
                else -> {
                    // Default fallback
                    config.textColor
                }
            }
        } else {
            config.textColor
        }
    }
    
    /**
     * Convert dp to pixels
     */
    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }
    
    /**
     * Convert sp to pixels
     */
    private fun spToPx(sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        )
    }
}

/**
 * Note: WatermarkActivity base class removed to avoid dependency issues.
 * Use the extension functions or manual integration instead.
 * 
 * Example manual integration:
 * 
 * override fun onResume() {
 *     super.onResume()
 *     WatermarkHelper.addToActivity(this)
 * }
 * 
 * override fun onDestroy() {
 *     super.onDestroy()
 *     WatermarkHelper.removeFromActivity(this)
 * }
 */

/**
 * Extension function for any Activity to easily add watermark
 */
fun Activity.addWatermark() {
    WatermarkHelper.addToActivity(this)
}

/**
 * Extension function for any Activity to easily remove watermark
 */
fun Activity.removeWatermark() {
    WatermarkHelper.removeFromActivity(this)
} 