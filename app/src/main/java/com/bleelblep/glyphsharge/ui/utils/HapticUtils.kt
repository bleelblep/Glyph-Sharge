package com.bleelblep.glyphsharge.ui.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository
import com.bleelblep.glyphsharge.di.AppModule
import dagger.hilt.android.EntryPointAccessors

/**
 * Utility class for managing haptic feedback across the app
 * Provides consistent haptic patterns for different interactions
 * 
 * Android Vibration Amplitude Documentation:
 * - Amplitude range: 1-255 (0 = OFF)
 * - DEFAULT_AMPLITUDE = -1 (system default)
 * - Always check hasAmplitudeControl() for device support
 */
object HapticUtils {
    
    // Standard intensity levels based on Android documentation
    object Intensity {
        const val OFF = 0
        const val LIGHT = 85          // ~33% of max (255 * 0.33)
        const val MEDIUM = 170        // ~66% of max (255 * 0.66) 
        const val STRONG = 255        // 100% max amplitude
        const val DEFAULT = VibrationEffect.DEFAULT_AMPLITUDE // -1
    }
    
    // Default durations for different haptic types
    private const val SHORT_DURATION = 50L
    private const val MEDIUM_DURATION = 100L
    private const val LONG_DURATION = 200L

    /**
     * Performs light haptic feedback with specified intensity
     */
    fun performLightHaptic(
        context: Context, 
        hapticFeedback: HapticFeedback,
        intensity: Int = Intensity.LIGHT
    ) {
        // First try Compose haptic feedback (respects user settings)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        
        // Then add our custom intensity if supported
        if (intensity != Intensity.DEFAULT) {
            performCustomVibration(context, SHORT_DURATION, intensity)
        }
    }
    
    /**
     * Performs medium haptic feedback with specified intensity
     */
    fun performMediumHaptic(
        context: Context,
        hapticFeedback: HapticFeedback,
        intensity: Int = Intensity.MEDIUM
    ) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        
        if (intensity != Intensity.DEFAULT) {
            performCustomVibration(context, MEDIUM_DURATION, intensity)
        }
    }
    
    /**
     * Performs strong haptic feedback with specified intensity
     */
    fun performStrongHaptic(
        context: Context,
        hapticFeedback: HapticFeedback,
        intensity: Int = Intensity.STRONG
    ) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        
        if (intensity != Intensity.DEFAULT) {
            performCustomVibration(context, LONG_DURATION, intensity)
        }
    }

    /**
     * Performs button click haptic feedback
     */
    fun performClickHaptic(
        context: Context,
        hapticFeedback: HapticFeedback,
        intensity: Int = Intensity.LIGHT
    ) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        
        if (intensity != Intensity.DEFAULT) {
            performCustomVibration(context, SHORT_DURATION, intensity)
        }
    }

    /**
     * Performs success haptic feedback
     */
    fun performSuccessHaptic(
        context: Context,
        hapticFeedback: HapticFeedback,
        intensity: Int = Intensity.MEDIUM
    ) {
        // Use predefined VibrationEffect if available (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val vibrator = getVibrator(context)
            if (vibrator.hasVibrator()) {
                try {
                    val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    vibrator.vibrate(effect)
                    return
                } catch (e: Exception) {
                    // Fall back to custom vibration
                }
            }
        }
        
        // Fallback to custom pattern
        performCustomVibration(context, SHORT_DURATION, intensity)
    }
    
    /**
     * Performs error haptic feedback
     */
    fun performErrorHaptic(
        context: Context,
        hapticFeedback: HapticFeedback,
        intensity: Int = Intensity.STRONG
    ) {
        // Create a double-pulse pattern for error feedback
        val pattern = longArrayOf(0, 100, 50, 100) // off, on, off, on
        val amplitudes = intArrayOf(0, intensity, 0, intensity)
        
        performCustomVibrationPattern(context, pattern, amplitudes)
    }
    
    /**
     * Tests vibration with specific intensity
     * Used by settings to let users feel different intensity levels
     */
    fun testVibrationIntensity(context: Context, intensity: Int) {
        performCustomVibration(context, MEDIUM_DURATION, intensity)
    }
    
    /**
     * Performs custom vibration with specified duration and intensity
     */
    private fun performCustomVibration(
        context: Context,
        duration: Long,
        intensity: Int
    ) {
        val vibrator = getVibrator(context)
        if (!vibrator.hasVibrator()) return
        
        val safeIntensity = validateIntensity(intensity, vibrator)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val effect = if (safeIntensity == Intensity.DEFAULT) {
                    VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                } else {
                    VibrationEffect.createOneShot(duration, safeIntensity)
                }
                vibrator.vibrate(effect)
            } catch (e: Exception) {
                // Fallback for very old devices
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } else {
            // Pre-API 26 fallback
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    /**
     * Performs custom vibration pattern with amplitudes
     */
    private fun performCustomVibrationPattern(
        context: Context,
        pattern: LongArray,
        amplitudes: IntArray
    ) {
        val vibrator = getVibrator(context)
        if (!vibrator.hasVibrator()) return
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vibrator.hasAmplitudeControl()) {
            try {
                val effect = VibrationEffect.createWaveform(pattern, amplitudes, -1)
                vibrator.vibrate(effect)
            } catch (e: Exception) {
                // Fallback to pattern without amplitudes
            @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } else {
            // Fallback for devices without amplitude control
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    /**
     * Validates and adjusts intensity based on device capabilities
     */
    private fun validateIntensity(intensity: Int, vibrator: Vibrator): Int {
        return when {
            intensity == Intensity.DEFAULT -> intensity
            intensity <= 0 -> Intensity.OFF
            intensity > 255 -> Intensity.STRONG
            !vibrator.hasAmplitudeControl() -> {
                // Device doesn't support amplitude control
                // Convert to simple on/off: any non-zero value becomes max
                if (intensity > 0) Intensity.STRONG else Intensity.OFF
            }
            else -> intensity
        }
    }

    /**
     * Gets the system vibrator service
     */
    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    /**
     * Converts user intensity (0.0-1.0) to Android amplitude (1-255)
     */
    fun convertUserIntensityToAndroidAmplitude(userIntensity: Float): Int {
        return when {
            userIntensity <= 0.0f -> Intensity.OFF
            userIntensity >= 1.0f -> Intensity.STRONG
            else -> {
                // Scale 0.0-1.0 to 1-255: multiply by 254 then add 1
                val scaled = (userIntensity * 254f).toInt() + 1
                // Ensure we never go below 1 (unless OFF) or above 255
                scaled.coerceIn(1, 255)
            }
        }
    }

    /**
     * Performs haptic with specified user intensity (0.0-1.0 range)
     */
    fun performHapticWithIntensity(
        context: Context,
        hapticFeedback: HapticFeedback,
        userIntensity: Float,
        type: HapticType = HapticType.LIGHT
    ) {
        val androidIntensity = convertUserIntensityToAndroidAmplitude(userIntensity)
        
        when (type) {
            HapticType.LIGHT -> performLightHaptic(context, hapticFeedback, androidIntensity)
            HapticType.MEDIUM -> performMediumHaptic(context, hapticFeedback, androidIntensity)
            HapticType.STRONG -> performStrongHaptic(context, hapticFeedback, androidIntensity)
            HapticType.CLICK -> performClickHaptic(context, hapticFeedback, androidIntensity)
            HapticType.SUCCESS -> performSuccessHaptic(context, hapticFeedback, androidIntensity)
            HapticType.ERROR -> performErrorHaptic(context, hapticFeedback, androidIntensity)
        }
    }
    
    /**
     * Legacy compatibility methods for existing code
     * These methods use the current user intensity setting from SettingsRepository
     */
    
    private fun getCurrentUserIntensity(context: Context): Float {
        return try {
            val hiltEntryPoint = EntryPointAccessors.fromApplication<AppModule.SettingsRepositoryEntryPoint>(
                context.applicationContext
            )
            val settingsRepository = hiltEntryPoint.getSettingsRepository()
            settingsRepository.getVibrationIntensity()
        } catch (e: Exception) {
            android.util.Log.w("HapticUtils", "Failed to get vibration intensity, using default", e)
            0.66f // Default to medium intensity if settings access fails
        }
    }
    
    /**
     * Legacy compatibility methods - now use user's actual intensity setting
     * These methods bridge the gap during migration to the new system
     * @deprecated Use performHapticWithIntensity() directly for better performance and consistency
     */
    @Deprecated("Use performHapticWithIntensity() directly for better performance and consistency")
    fun triggerLightFeedback(hapticFeedback: HapticFeedback, context: Context) {
        val intensity = getCurrentUserIntensity(context)
        performHapticWithIntensity(context, hapticFeedback, intensity, HapticType.LIGHT)
    }
    
    @Deprecated("Use performHapticWithIntensity() directly for better performance and consistency")
    fun triggerMediumFeedback(hapticFeedback: HapticFeedback, context: Context) {
        val intensity = getCurrentUserIntensity(context)
        performHapticWithIntensity(context, hapticFeedback, intensity, HapticType.MEDIUM)
    }
    
    @Deprecated("Use performHapticWithIntensity() directly for better performance and consistency")
    fun triggerStrongFeedback(hapticFeedback: HapticFeedback, context: Context) {
        val intensity = getCurrentUserIntensity(context)
        performHapticWithIntensity(context, hapticFeedback, intensity, HapticType.STRONG)
    }
    
    @Deprecated("Use performHapticWithIntensity() directly for better performance and consistency")
    fun triggerSuccessFeedback(hapticFeedback: HapticFeedback, context: Context) {
        val intensity = getCurrentUserIntensity(context)
        performHapticWithIntensity(context, hapticFeedback, intensity, HapticType.SUCCESS)
    }
    
    @Deprecated("Use performHapticWithIntensity() directly for better performance and consistency")
    fun triggerErrorFeedback(hapticFeedback: HapticFeedback, context: Context) {
        val intensity = getCurrentUserIntensity(context)
        performHapticWithIntensity(context, hapticFeedback, intensity, HapticType.ERROR)
    }
    
    fun testExactAmplitude(hapticFeedback: HapticFeedback, context: Context, userIntensity: Float) {
        performHapticWithIntensity(context, hapticFeedback, userIntensity, HapticType.MEDIUM)
    }

    /**
     * Gets device vibration capabilities info
     */
    fun getVibrationInfo(context: Context): VibrationInfo {
        val vibrator = getVibrator(context)
        return VibrationInfo(
            hasVibrator = vibrator.hasVibrator(),
            hasAmplitudeControl = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.hasAmplitudeControl()
            } else false,
            supportedEffects = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                listOf(
                    VibrationEffect.EFFECT_CLICK,
                    VibrationEffect.EFFECT_DOUBLE_CLICK,
                    VibrationEffect.EFFECT_TICK
                ).filter { effect ->
                    val supportResults = vibrator.areEffectsSupported(effect)
                    supportResults.isNotEmpty() && supportResults[0] == Vibrator.VIBRATION_EFFECT_SUPPORT_YES
                }
            } else emptyList()
        )
    }
}

/**
 * Enum for different haptic feedback types
 */
enum class HapticType {
    LIGHT,
    MEDIUM, 
    STRONG,
    CLICK,
    SUCCESS,
    ERROR
}

/**
 * Data class containing device vibration capabilities
 */
data class VibrationInfo(
    val hasVibrator: Boolean,
    val hasAmplitudeControl: Boolean,
    val supportedEffects: List<Int>
) 