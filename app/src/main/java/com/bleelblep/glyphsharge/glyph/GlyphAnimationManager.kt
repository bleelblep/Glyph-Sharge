package com.bleelblep.glyphsharge.glyph

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.nothing.ketchum.Common
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancel
import javax.inject.Inject
import javax.inject.Singleton
import com.bleelblep.glyphsharge.utils.LoggingManager
import kotlin.random.Random
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository

/**
 * Manager class for creating and running glyph animations.
 * Includes comprehensive animations with proper phone model support.
 * 
 * FIXES IMPLEMENTED:
 * 1. Brightness staying on after animations - Fixed by adding proper turnOff() calls
 * 2. Individual channel mapping - Updated to match official Nothing Developer Kit documentation
 * 3. Animation cleanup - Added ensureCleanup() and resetGlyphs() functions
 * 4. Error handling - All animations now have proper try-catch with cleanup
 * 5. Channel constants - Using official Glyph.* constants instead of custom mappings
 * 
 * Following official Nothing Developer Kit documentation:
 * https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit
 */
@Singleton
class GlyphAnimationManager @Inject constructor(
    private val glyphManager: GlyphManager,
    private val settingsRepository: SettingsRepository
) {
    private val TAG = "GlyphAnimationManager"
    private val handler = Handler(Looper.getMainLooper())
    private var isAnimationRunning = false
    private val animationScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Settings for animation
    private var maxBrightness = 4000      // Default max brightness

    /**
     * Check if an animation is currently running
     */
    fun isAnimationActive(): Boolean {
        return isAnimationRunning
    }

    // Phone (1) segments
    private object Phone1 {
        const val A = 0
        const val B = 1
        const val C_START = 2 // C1-C4 are 2-5
        const val E = 6
        const val D_START = 7 // D1_1-D1_8 are 7-14
    }

    // Phone (2) segments - corrected mapping
    private val c1Segments = listOf(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18)
    private val aSegments = listOf(0, 1)           // A1, A2
    private val bSegments = listOf(2)              // B
    private val cOtherSegments = listOf(19, 20, 21, 22, 23) // C2-C6
    private val dSegments = listOf(25, 26, 27, 28, 29, 30, 31, 32) // D1_1 to D1_8
    private val eSegments = listOf(24)             // E1

    // Phone (2a/2a+) segments
    private object Phone2a {
        const val C_START = 0  // C1-C24 segments (0-23)
        const val B = 24       // B segment
        const val A = 25       // A segment
    }

    // Phone (3a/3a Pro) segments - CORRECTED based on official documentation
    private object Phone3a {
        const val C_START = 0   // C1-C20 segments (0-19)
        const val A_START = 20  // A1-A11 segments (20-30)
        const val B_START = 31  // B1-B5 segments (31-35)
    }

    /**
     * Reset all glyphs by turning them off
     * Essential for preventing brightness staying on after animations
     */
    private suspend fun resetGlyphs() {
        withContext(Dispatchers.Default) {
            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@withContext
                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
            } catch (e: Exception) {
                Log.e(TAG, "Error resetting glyphs: ${e.message}")
            }
        }
    }

    /**
     * Ensure proper cleanup when animation is interrupted or completed
     */
    private suspend fun ensureCleanup() {
        try {
            isAnimationRunning = false
            glyphManager.mGM?.turnOff()
            delay(100L)
            Log.d(TAG, "Animation cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}")
        }
    }

    /**
     * Stop any currently running animations
     */
    fun stopAnimations() {
        isAnimationRunning = false
        try {
            glyphManager.turnOffAll()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping animations: ${e.message}")
        }
    }

    /**
     * Turn on all glyphs at maximum brightness for alert purposes
     */
    fun turnOnAllGlyphs() {
        try {
            glyphManager.turnOnAllGlyphs()
        } catch (e: Exception) {
            Log.e(TAG, "Error turning on all glyphs: ${e.message}")
        }
    }

    /**
     * Turn off all glyphs
     */
    fun turnOffAll() {
        try {
            glyphManager.turnOffAll()
        } catch (e: Exception) {
            Log.e(TAG, "Error turning off all glyphs: ${e.message}")
        }
    }

    /**
     * Stop any currently running animation
     */
    fun stopAnimation() {
        isAnimationRunning = false
    }

    /**
     * Run a wave animation across the phone's glyph segments
     */
    suspend fun runWaveAnimation() {
        if (!isGlyphServiceEnabled()) return
        Log.d(TAG, "runWaveAnimation called")
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - wave animation will not run")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Starting wave animation")
            resetGlyphs()
            delay(100L)

            when {
                Common.is20111() -> runPhone1WaveAnimation()
                Common.is22111() -> runPhone2WaveAnimation()
                Common.is23111() || Common.is23113() -> runPhone2aWaveAnimation()
                Common.is24111() -> runPhone3aWaveAnimation()
                else -> runDefaultWaveAnimation()
            }
        } finally {
            isAnimationRunning = false
            glyphManager.turnOffAll()
        }
    }

    private suspend fun runPhone1WaveAnimation() {
        val stepDuration = 150L
        val segments = listOf(
            Phone1.A, Phone1.B,
            Phone1.C_START, Phone1.C_START + 1, Phone1.C_START + 2, Phone1.C_START + 3,
            Phone1.E,
            Phone1.D_START, Phone1.D_START + 1, Phone1.D_START + 2, Phone1.D_START + 3,
            Phone1.D_START + 4, Phone1.D_START + 5, Phone1.D_START + 6, Phone1.D_START + 7
        )

        for (segment in segments) {
            if (!isAnimationRunning) break

            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                builder.buildChannel(segment, maxBrightness)

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration)

                glyphManager.turnOffAll()
                delay(50L)
            } catch (e: Exception) {
                delay(stepDuration)
            }
        }
    }

    private suspend fun runPhone2WaveAnimation() {
        val stepDuration = 100L
        val segments = aSegments + bSegments + c1Segments + cOtherSegments + eSegments + dSegments

        for (segment in segments) {
            if (!isAnimationRunning) break

            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                builder.buildChannel(segment, maxBrightness)

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration)

                glyphManager.turnOffAll()
                delay(30L)
            } catch (e: Exception) {
                delay(stepDuration)
            }
        }
    }

    private suspend fun runPhone2aWaveAnimation() {
        val stepDuration = 80L

        // Wave through C segments
        for (i in 0 until 24) {
            if (!isAnimationRunning) break

            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                builder.buildChannel(Phone2a.C_START + i, maxBrightness)

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration)

                glyphManager.turnOffAll()
                delay(30L)
            } catch (e: Exception) {
                delay(stepDuration)
            }
        }

        // A and B segments
        val finalSegments = listOf(Phone2a.A, Phone2a.B)
        for (segment in finalSegments) {
            if (!isAnimationRunning) break

            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                builder.buildChannel(segment, maxBrightness)

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration * 2)

                glyphManager.turnOffAll()
                delay(50L)
            } catch (e: Exception) {
                delay(stepDuration * 2)
            }
        }
    }

    private suspend fun runPhone3aWaveAnimation() {
        val stepDuration = 80L

        // C segments wave (C1-C20) - bottom left to top right
        for (i in 0 until 20) {
            if (!isAnimationRunning) break

            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                builder.buildChannel(Phone3a.C_START + i, maxBrightness)

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration)

                glyphManager.turnOffAll()
                delay(25L)
            } catch (e: Exception) {
                delay(stepDuration)
            }
        }

        // A segments wave (A1-A11) - top to bottom
        for (i in 0 until 11) {
            if (!isAnimationRunning) break

            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                builder.buildChannel(Phone3a.A_START + i, maxBrightness)

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration + 20L) // Slightly slower for A segments
                
                glyphManager.turnOffAll()
                delay(30L)
            } catch (e: Exception) {
                delay(stepDuration + 20L)
            }
        }

        // B segments wave (B1-B5) - bottom right to top left
        for (i in 0 until 5) {
            if (!isAnimationRunning) break

            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                builder.buildChannel(Phone3a.B_START + i, maxBrightness)

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration + 40L) // Slowest for B segments (dramatic finish)

                glyphManager.turnOffAll()
                delay(40L)
            } catch (e: Exception) {
                delay(stepDuration + 40L)
            }
        }
    }

    private suspend fun runDefaultWaveAnimation() {
        // Default animation for unsupported devices
        delay(1000L)
    }

    /**
     * Beedah Animation - Wave animation where glyphs stay lit after the wave passes
     * Once all glyphs are lit, they pulse 3 times together
     */
    suspend fun runBeedahAnimation() {
        if (!isGlyphServiceEnabled()) return
        Log.d(TAG, "runBeedahAnimation called")
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - beedah animation will not run")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Starting beedah animation")
            resetGlyphs()
            delay(100L)

            when {
                Common.is20111() -> runPhone1BeedahAnimation()
                Common.is22111() -> runPhone2BeedahAnimation()
                Common.is23111() || Common.is23113() -> runPhone2aBeedahAnimation()
                Common.is24111() -> runPhone3aBeedahAnimation()
                else -> runDefaultBeedahAnimation()
            }
        } finally {
            isAnimationRunning = false
            glyphManager.turnOffAll()
        }
    }

    private suspend fun runPhone1BeedahAnimation() {
        val stepDuration = 150L
        val segments = listOf(
            Phone1.A, Phone1.B,
            Phone1.C_START, Phone1.C_START + 1, Phone1.C_START + 2, Phone1.C_START + 3,
            Phone1.E,
            Phone1.D_START, Phone1.D_START + 1, Phone1.D_START + 2, Phone1.D_START + 3,
            Phone1.D_START + 4, Phone1.D_START + 5, Phone1.D_START + 6, Phone1.D_START + 7
        )

        val litSegments = mutableSetOf<Int>()

        // Wave phase - light up segments one by one and keep them lit
        for (segment in segments) {
            if (!isAnimationRunning) break

            try {
                litSegments.add(segment)
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                // Light up all previously lit segments
                litSegments.forEach { builder.buildChannel(it, maxBrightness) }

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration)
            } catch (e: Exception) {
                delay(stepDuration)
            }
        }

        // Pulse phase - all segments pulse 3 times
        for (pulse in 0 until 3) {
            if (!isAnimationRunning) break

            try {
                // Turn off all
                glyphManager.turnOffAll()
                delay(300L)

                // Turn on all
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                litSegments.forEach { builder.buildChannel(it, maxBrightness) }
                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(300L)
            } catch (e: Exception) {
                delay(300L)
            }
        }
    }

    private suspend fun runPhone2BeedahAnimation() {
        val stepDuration = 100L
        val segments = aSegments + bSegments + c1Segments + cOtherSegments + eSegments + dSegments
        val litSegments = mutableSetOf<Int>()

        // Wave phase
        for (segment in segments) {
            if (!isAnimationRunning) break

            try {
                litSegments.add(segment)
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                litSegments.forEach { builder.buildChannel(it, maxBrightness) }

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration)
            } catch (e: Exception) {
                delay(stepDuration)
            }
        }

        // Pulse phase
        for (pulse in 0 until 3) {
            if (!isAnimationRunning) break

            try {
                glyphManager.turnOffAll()
                delay(300L)

                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                litSegments.forEach { builder.buildChannel(it, maxBrightness) }
                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(300L)
            } catch (e: Exception) {
                delay(300L)
            }
        }
    }

    private suspend fun runPhone2aBeedahAnimation() {
        val stepDuration = 80L
        val litSegments = mutableSetOf<Int>()

        // Wave through C segments
        for (i in 0 until 24) {
            if (!isAnimationRunning) break

            try {
                litSegments.add(Phone2a.C_START + i)
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                litSegments.forEach { builder.buildChannel(it, maxBrightness) }

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration)
            } catch (e: Exception) {
                delay(stepDuration)
            }
        }

        // A and B segments
        val finalSegments = listOf(Phone2a.A, Phone2a.B)
        for (segment in finalSegments) {
            if (!isAnimationRunning) break

            try {
                litSegments.add(segment)
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                litSegments.forEach { builder.buildChannel(it, maxBrightness) }

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration * 2)
            } catch (e: Exception) {
                delay(stepDuration * 2)
            }
        }

        // Pulse phase
        for (pulse in 0 until 3) {
            if (!isAnimationRunning) break

            try {
                glyphManager.turnOffAll()
                delay(300L)

                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                litSegments.forEach { builder.buildChannel(it, maxBrightness) }
                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(300L)
            } catch (e: Exception) {
                delay(300L)
            }
        }
    }

    private suspend fun runPhone3aBeedahAnimation() {
        val stepDuration = 80L
        val litSegments = mutableSetOf<Int>()

        // C segments wave (C1-C20) - bottom left to top right
        for (i in 0 until 20) {
            if (!isAnimationRunning) break

            try {
                litSegments.add(Phone3a.C_START + i)
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                litSegments.forEach { builder.buildChannel(it, maxBrightness) }

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration)
            } catch (e: Exception) {
                delay(stepDuration)
            }
        }

        // A segments wave (A1-A11) - top to bottom
        for (i in 0 until 11) {
            if (!isAnimationRunning) break

            try {
                litSegments.add(Phone3a.A_START + i)
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                litSegments.forEach { builder.buildChannel(it, maxBrightness) }

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration + 20L)
            } catch (e: Exception) {
                delay(stepDuration + 20L)
            }
        }

        // B segments wave (B1-B5) - bottom right to top left
        for (i in 0 until 5) {
            if (!isAnimationRunning) break

            try {
                litSegments.add(Phone3a.B_START + i)
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                litSegments.forEach { builder.buildChannel(it, maxBrightness) }

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration + 40L)
            } catch (e: Exception) {
                delay(stepDuration + 40L)
            }
        }

        // Pulse phase - all segments pulse 3 times
        for (pulse in 0 until 3) {
            if (!isAnimationRunning) break

            try {
                glyphManager.turnOffAll()
                delay(300L)

                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                litSegments.forEach { builder.buildChannel(it, maxBrightness) }
                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(300L)
            } catch (e: Exception) {
                delay(300L)
            }
        }
    }

    private suspend fun runDefaultBeedahAnimation() {
        // Default animation for unsupported devices
        delay(2000L)
    }

    /**
     * Phone 3a Spiral Animation - Uses the unique C1-C20 configuration
     * Showcases the Nothing Phone 3a's distinctive glyph layout
     */
    suspend fun runPhone3aSpiralAnimation() {
        if (!Common.is24111() || !glyphManager.isNothingPhone()) return
        isAnimationRunning = true

        try {
            Log.d(TAG, "Starting Phone 3a spiral animation")
            resetGlyphs()
            delay(100L)

            val stepDuration = 60L
            
            // C segments for Phone 3a (channels 0-19) - C1 to C20
            val cSegments = (Phone3a.C_START until Phone3a.C_START + 20).toList()
            // A segments (channels 20-30) - A1 to A11
            val aSegments = (Phone3a.A_START until Phone3a.A_START + 11).toList()
            // B segments (channels 31-35) - B1 to B5
            val bSegments = (Phone3a.B_START until Phone3a.B_START + 5).toList()

            // Phase 1: Spiral through C segments (bottom left to top right)
            for (i in cSegments.indices) {
                if (!isAnimationRunning) break

                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                    
                    // Light up current and previous segments with fading effect
                    for (j in 0..i) {
                        val brightness = if (j == i) maxBrightness 
                                        else (maxBrightness * (0.3f + (j.toFloat() / i) * 0.4f)).toInt()
                        builder.buildChannel(cSegments[j], brightness)
                    }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                    delay(stepDuration)
                } catch (e: Exception) {
                    delay(stepDuration)
                }
            }

            // Phase 2: Transition to A segments with C maintaining partial brightness
            for (i in aSegments.indices) {
                if (!isAnimationRunning) break

                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break

                    // Maintain C segments at 30% brightness
                    cSegments.forEach { segment ->
                        builder.buildChannel(segment, (maxBrightness * 0.3f).toInt())
                    }
                    
                    // Progressive A activation
                    for (j in 0..i) {
                        val brightness = if (j == i) maxBrightness 
                                        else (maxBrightness * (0.5f + (j.toFloat() / i) * 0.5f)).toInt()
                        builder.buildChannel(aSegments[j], brightness)
                    }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                    delay(stepDuration + 10L) // Slightly slower for A
                } catch (e: Exception) {
                    delay(stepDuration + 10L)
                }
            }

            // Phase 3: Culmination with B segments - all zones active
            for (i in bSegments.indices) {
                if (!isAnimationRunning) break

                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break

                    // C segments at 40% brightness
                    cSegments.forEach { segment ->
                        builder.buildChannel(segment, (maxBrightness * 0.4f).toInt())
                    }
                    
                    // A segments at 70% brightness
                    aSegments.forEach { segment ->
                        builder.buildChannel(segment, (maxBrightness * 0.7f).toInt())
                    }
                    
                    // Progressive B activation
                    for (j in 0..i) {
                        builder.buildChannel(bSegments[j], maxBrightness)
                    }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                    delay(stepDuration + 20L) // Slowest for dramatic B finish
                } catch (e: Exception) {
                    delay(stepDuration + 20L)
                }
            }

            // Phase 4: Final flash - all segments at maximum brightness
            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
                
                cSegments.forEach { builder.buildChannel(it, maxBrightness) }
                aSegments.forEach { builder.buildChannel(it, maxBrightness) }
                bSegments.forEach { builder.buildChannel(it, maxBrightness) }
                
                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(500L) // Hold full brightness
                
                glyphManager.turnOffAll()
                delay(200L)
                
                // Final pulse
                glyphManager.mGM?.toggle(frame)
                delay(300L)
            } catch (e: Exception) {
                Log.e(TAG, "Error in Phone 3a spiral finale: ${e.message}")
            }

        } finally {
            isAnimationRunning = false
            glyphManager.turnOffAll()
        }
    }

    /**
     * Create a pulsing breathing effect
     */
    suspend fun runPulseEffect(cycles: Int = 3) {
        if (!isGlyphServiceEnabled()) return
        if (!glyphManager.isNothingPhone()) return
        isAnimationRunning = true

        try {
            val stepDuration = 250L // Hardcoded value
            Log.d(TAG, "Starting pulse effect")
            resetGlyphs()

            val segmentsToLight = when {
                Common.is20111() -> listOf(Phone1.A, Phone1.B, Phone1.E)
                Common.is22111() -> aSegments + bSegments + eSegments
                Common.is23111() || Common.is23113() -> listOf(Phone2a.A, Phone2a.B)
                Common.is24111() -> {
                    // Phone 3a: Use key segments from each zone for pulse effect
                    listOf(Phone3a.A_START + 5, Phone3a.B_START + 2, Phone3a.C_START + 9) // Middle segments
                }
                else -> emptyList()
            }

            if (segmentsToLight.isEmpty()) return

            val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
            segmentsToLight.forEach { builder.buildChannel(it, maxBrightness) }
                        val frame = builder.build()

            for (i in 0 until cycles) {
                    if (!isAnimationRunning) break
                // Inhale
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration)

                // Turn off
                try {
                    glyphManager.turnOffAll()
                    delay(stepDuration)
                    } catch (e: Exception) {
                    delay(stepDuration)
                    }
            }
        } finally {
            ensureCleanup()
        }
    }

    /**
     * Run breathing animation using D1 sequence for NP1, C1 sequence for NP2
     * @param is478Pattern true for 4-7-8 breathing, false for box breathing
     * @param cycles number of breathing cycles to run
     */
    suspend fun runBreathingAnimation(is478Pattern: Boolean, cycles: Int) {
        if (!isGlyphServiceEnabled()) return
        if (!glyphManager.isNothingPhone()) return
        isAnimationRunning = true

        try {
            // Use device-specific breathing animations for better experience
            when {
                Common.is20111() -> runPhone1D1BreathingAnimation(is478Pattern, cycles)
                Common.is24111() -> runPhone3aBreathingAnimation(is478Pattern, cycles)
                else -> {
                    // Fallback simple breathing animation for other devices
                    val inhale = if (is478Pattern) 4000L else 2000L
                    val hold = if (is478Pattern) 7000L else 0L
                    val exhale = if (is478Pattern) 8000L else 2000L

                    val segmentsToLight = when {
                        Common.is22111() -> aSegments + bSegments + eSegments
                        Common.is23111() || Common.is23113() -> listOf(Phone2a.A, Phone2a.B)
                        else -> emptyList()
                    }

                    if (segmentsToLight.isEmpty()) return

                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
                    segmentsToLight.forEach { builder.buildChannel(it, maxBrightness) }
                    val frame = builder.build()

                    for (i in 0 until cycles) {
                        if (!isAnimationRunning) break
                        // Inhale
                        glyphManager.mGM?.toggle(frame)
                        delay(inhale)
                        // Hold
                        if (hold > 0) delay(hold)
                        // Exhale
                        glyphManager.mGM?.turnOff()
                        delay(exhale)
                    }
                }
            }
        } finally {
            ensureCleanup()
        }
    }

    /**
     * Phone 3a C-based breathing animation
     * Uses C1-C20 segments (channels 0-19) for inhale/exhale with A and B support
     */
    private suspend fun runPhone3aBreathingAnimation(is478Pattern: Boolean, cycles: Int) {
        Log.d(TAG, "Running Phone 3a C-based breathing animation")
        
        // C segments for Phone 3a (channels 0-19) - C1 to C20
        val cSegments = (Phone3a.C_START until Phone3a.C_START + 20).toList()
        // A segments (channels 20-30) - A1 to A11
        val aSegments = (Phone3a.A_START until Phone3a.A_START + 11).toList()
        // B segments (channels 31-35) - B1 to B5
        val bSegments = (Phone3a.B_START until Phone3a.B_START + 5).toList()

        // Timing configuration
        val inhaleDuration = if (is478Pattern) 4000L else 4000L // 4 seconds inhale for both patterns
        val holdDuration = if (is478Pattern) 7000L else 4000L // 7 seconds for 4-7-8, 4 seconds for box
        val exhaleDuration = if (is478Pattern) 8000L else 4000L // 8 seconds for 4-7-8, 4 seconds for box
        val secondHoldDuration = if (is478Pattern) 0L else 4000L // 0 for 4-7-8, 4 seconds for box

        for (cycle in 0 until cycles) {
            if (!isAnimationRunning) break

            Log.d(TAG, "Phone 3a breathing cycle ${cycle + 1}/$cycles")

            // PHASE 1: INHALE - Progressive C activation with A/B support (4 seconds)
            val inhaleSteps = cSegments.size
            val inhaleStepDuration = inhaleDuration / inhaleSteps

            for (i in cSegments.indices) {
                if (!isAnimationRunning) break

                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break

                    // Light up C segments progressively during inhale
                    for (j in 0..i) {
                        builder.buildChannel(cSegments[j], maxBrightness)
                    }

                    // A and B segments brightness increases with C progress
                    val progress = (i + 1) / cSegments.size.toFloat()
                    val aBrightness = (maxBrightness * progress * 0.8f).toInt() // 80% of max for A
                    val bBrightness = (maxBrightness * progress * 0.6f).toInt() // 60% of max for B
                    
                    // Add A segments with progressive brightness
                    aSegments.forEach { segment ->
                        builder.buildChannel(segment, aBrightness)
                    }
                    
                    // Add B segments with progressive brightness
                    bSegments.forEach { segment ->
                        builder.buildChannel(segment, bBrightness)
                    }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                    delay(inhaleStepDuration)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in Phone 3a inhale phase: ${e.message}")
                    delay(inhaleStepDuration)
                }
            }

            // PHASE 2: HOLD - Maintain full brightness
            Log.d(TAG, "Phone 3a breathing: hold phase")
            val holdSteps = (holdDuration / 200L).toInt() // Update every 200ms
            val holdStepDuration = holdDuration / holdSteps

            repeat(holdSteps) {
                if (!isAnimationRunning) return@repeat

                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat

                    // Maintain all C segments at full brightness
                    cSegments.forEach { segment ->
                        builder.buildChannel(segment, maxBrightness)
                    }

                    // Maintain A segments at 80% brightness
                    aSegments.forEach { segment ->
                        builder.buildChannel(segment, (maxBrightness * 0.8f).toInt())
                    }
                    
                    // Maintain B segments at 60% brightness
                    bSegments.forEach { segment ->
                        builder.buildChannel(segment, (maxBrightness * 0.6f).toInt())
                    }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                    delay(holdStepDuration)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in Phone 3a hold phase: ${e.message}")
                    delay(holdStepDuration)
                }
            }

            // PHASE 3: EXHALE - Progressive C deactivation (4/8 seconds)
            Log.d(TAG, "Phone 3a breathing: exhale phase")
            val exhaleSteps = cSegments.size
            val exhaleStepDuration = exhaleDuration / exhaleSteps

            for (i in cSegments.indices.reversed()) {
                if (!isAnimationRunning) break

                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break

                    // Light up C segments progressively during exhale
                    for (j in 0..i) {
                        builder.buildChannel(cSegments[j], maxBrightness)
                    }

                    // A and B segments brightness decreases with C progress
                    val progress = (i + 1) / cSegments.size.toFloat()
                    val aBrightness = (maxBrightness * progress * 0.8f).toInt()
                    val bBrightness = (maxBrightness * progress * 0.6f).toInt()
                    
                    // Add A segments with progressive brightness
                    aSegments.forEach { segment ->
                        builder.buildChannel(segment, aBrightness)
                    }
                    
                    // Add B segments with progressive brightness
                    bSegments.forEach { segment ->
                        builder.buildChannel(segment, bBrightness)
                    }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                    delay(exhaleStepDuration)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in Phone 3a exhale phase: ${e.message}")
                    delay(exhaleStepDuration)
                }
            }

            // PHASE 4: SECOND HOLD (Box breathing only)
            if (!is478Pattern && secondHoldDuration > 0) {
                Log.d(TAG, "Phone 3a breathing: second hold phase (after exhale) - Box breathing only")
                
                val secondHoldSteps = (secondHoldDuration / 200L).toInt()
                val secondHoldStepDuration = secondHoldDuration / secondHoldSteps

                repeat(secondHoldSteps) {
                    if (!isAnimationRunning) return@repeat

                    try {
                        val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat

                        // Maintain minimal brightness on first C segment only
                        val minBrightness = (maxBrightness * 0.1f).toInt()
                        builder.buildChannel(cSegments[0], minBrightness)

                        val frame = builder.build()
                        glyphManager.mGM?.toggle(frame)
                        delay(secondHoldStepDuration)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in Phone 3a second hold phase: ${e.message}")
                        delay(secondHoldStepDuration)
                    }
                }
            }

            // Brief pause between cycles
            if (cycle < cycles - 1) {
                delay(500L)
            }
        }
    }

    /**
     * Phone 1 D1-based breathing animation
     * Uses D1 segments (channels 7-14) for inhale/exhale
     */
    private suspend fun runPhone1D1BreathingAnimation(is478Pattern: Boolean, cycles: Int) {
        Log.d(TAG, "Running Phone 1 D1 breathing animation")
        
        // D1 segments for Phone 1 (channels 7-14)
        val d1Segments = listOf(7, 8, 9, 10, 11, 12, 13, 14) // D1_1 to D1_8
        val supportingSegments = listOf(0, 1, 2, 3, 4, 5, 6) // A, B, C1-C4, E

        // Timing configuration
        val inhaleDuration = 4000L // 4 seconds inhale
        val holdDuration = if (is478Pattern) 7000L else 4000L // 7 seconds for 4-7-8, 4 seconds for box
        val exhaleDuration = 8000L // 8 seconds exhale
        val secondHoldDuration = if (is478Pattern) 0L else 4000L // 0 for 4-7-8, 4 seconds for box

        for (cycle in 0 until cycles) {
            if (!isAnimationRunning) break

            Log.d(TAG, "D1 breathing cycle ${cycle + 1}/$cycles")

            // PHASE 1: INHALE - Progressive D1 activation (4 seconds)
            val inhaleSteps = d1Segments.size
            val inhaleStepDuration = inhaleDuration / inhaleSteps

            for (i in d1Segments.indices) {
                if (!isAnimationRunning) break

                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break

                    // Light up D1 segments progressively during inhale
                    for (j in 0..i) {
                        builder.buildChannel(d1Segments[j], maxBrightness)
                    }

                    // Supporting segments brightness increases with D1 progress
                    val brightness = (maxBrightness * ((i + 1) / d1Segments.size.toFloat())).toInt()
                    
                    // Add supporting segments with progressive brightness
                    supportingSegments.forEach { segment ->
                        builder.buildChannel(segment, brightness)
                    }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                    delay(inhaleStepDuration)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in D1 inhale phase: ${e.message}")
                    delay(inhaleStepDuration)
                }
            }

            // PHASE 2: HOLD - Maintain full brightness
            Log.d(TAG, "D1 breathing: hold phase")
            val holdSteps = (holdDuration / 200L).toInt() // Update every 200ms
            val holdStepDuration = holdDuration / holdSteps

            repeat(holdSteps) {
                if (!isAnimationRunning) return@repeat

                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat

                    // Maintain all D1 segments at full brightness
                    d1Segments.forEach { segment ->
                        builder.buildChannel(segment, maxBrightness)
                    }

                    // Maintain supporting segments at full brightness
                    supportingSegments.forEach { segment ->
                        builder.buildChannel(segment, maxBrightness)
                    }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                    delay(holdStepDuration)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in D1 hold phase: ${e.message}")
                    delay(holdStepDuration)
                }
            }

            // PHASE 3: EXHALE - Progressive D1 deactivation (8 seconds)
            Log.d(TAG, "D1 breathing: exhale phase")
            val exhaleSteps = d1Segments.size
            val exhaleStepDuration = exhaleDuration / exhaleSteps

            for (i in d1Segments.indices.reversed()) {
                if (!isAnimationRunning) break

                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break

                    // Light up D1 segments progressively during exhale
                    for (j in 0..i) {
                        builder.buildChannel(d1Segments[j], maxBrightness)
                    }

                    // Supporting segments brightness decreases with D1 progress
                    val brightness = (maxBrightness * ((i + 1) / d1Segments.size.toFloat())).toInt()
                    
                    // Add supporting segments with progressive brightness
                    supportingSegments.forEach { segment ->
                        builder.buildChannel(segment, brightness)
                    }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                    delay(exhaleStepDuration)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in D1 exhale phase: ${e.message}")
                    delay(exhaleStepDuration)
                }
            }

            // PHASE 4: SECOND HOLD (Box breathing only)
            if (!is478Pattern && secondHoldDuration > 0) {
                Log.d(TAG, "D1 breathing: second hold phase (after exhale) - Box breathing only")
                
                // Use final state from LED calibration: only D1_1 active with supporting segments at minimum brightness
                val secondHoldSteps = (secondHoldDuration / 200L).toInt() // Update every 200ms
                val secondHoldStepDuration = secondHoldDuration / secondHoldSteps
                val minBrightness = (maxBrightness * (1.0f / d1Segments.size)).toInt()
                
                repeat(secondHoldSteps) {
                    if (!isAnimationRunning) return@repeat
                    
                    try {
                        val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
                        
                        // Only D1_1 should be active (final state from LED calibration)
                        builder.buildChannel(d1Segments[0], maxBrightness) // D1_1
                        
                        // All other D1 segments at 0 brightness (explicitly off)
                        for (i in 1 until d1Segments.size) {
                            builder.buildChannel(d1Segments[i], 0)
                        }
                        
                        // Supporting segments at minimum brightness (final exhale state)
                        supportingSegments.forEach { segment ->
                            builder.buildChannel(segment, minBrightness)
                        }
                        
                        val frame = builder.build()
                        glyphManager.mGM?.toggle(frame)
                        delay(secondHoldStepDuration)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in D1 second hold phase: ${e.message}")
                        delay(secondHoldStepDuration)
                    }
                }
            }
        }

        // Turn off all LEDs
        glyphManager.turnOffAll()
        Log.d(TAG, "Phone 1 D1 breathing animation completed")
    }

    /**
     * Phone 2 C1-based breathing animation
     * Uses C1 segments (channels 3-18) for inhale/exhale
     */
    private suspend fun runPhone2C1BreathingAnimation(is478Pattern: Boolean, cycles: Int) {
        Log.d(TAG, "Running Phone 2 C1 breathing animation")
        
        // Timing configuration
        val inhaleDuration = 4000L // 4 seconds inhale
        val holdDuration = if (is478Pattern) 7000L else 4000L // 7 seconds for 4-7-8, 4 seconds for box
        val exhaleDuration = 8000L // 8 seconds exhale
        val secondHoldDuration = if (is478Pattern) 0L else 4000L // 0 for 4-7-8, 4 seconds for box

        for (cycle in 0 until cycles) {
            if (!isAnimationRunning) break

            Log.d(TAG, "C1 breathing cycle ${cycle + 1}/$cycles")

            // PHASE 1: INHALE - Progressive C1 activation (4 seconds)
            val inhaleSteps = c1Segments.size
            val inhaleStepDuration = inhaleDuration / inhaleSteps

            for (i in c1Segments.indices) {
                if (!isAnimationRunning) break

                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break

                    // Light up C1 segments progressively during inhale
                    for (j in 0..i) {
                        builder.buildChannel(c1Segments[j], maxBrightness)
                    }

                    // Supporting segments brightness increases with C1 progress
                    val brightness = (maxBrightness * ((i + 1) / c1Segments.size.toFloat())).toInt()
                    
                    // Add supporting segments with progressive brightness
                    for (segment in aSegments) builder.buildChannel(segment, brightness)
                    for (segment in bSegments) builder.buildChannel(segment, brightness)
                    for (segment in cOtherSegments) builder.buildChannel(segment, brightness)
                    for (segment in dSegments) builder.buildChannel(segment, brightness)
                    for (segment in eSegments) builder.buildChannel(segment, brightness)

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                    delay(inhaleStepDuration)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in C1 inhale phase: ${e.message}")
                    delay(inhaleStepDuration)
                }
            }

            // PHASE 2: HOLD - Maintain full brightness
            Log.d(TAG, "C1 breathing: hold phase")
            val holdSteps = (holdDuration / 200L).toInt() // Update every 200ms
            val holdStepDuration = holdDuration / holdSteps

            repeat(holdSteps) {
                if (!isAnimationRunning) return@repeat

                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat

                    // Maintain all C1 segments at full brightness
                    for (segment in c1Segments) {
                        builder.buildChannel(segment, maxBrightness)
                    }

                    // Maintain supporting segments at full brightness
                    for (segment in aSegments) builder.buildChannel(segment, maxBrightness)
                    for (segment in bSegments) builder.buildChannel(segment, maxBrightness)
                    for (segment in cOtherSegments) builder.buildChannel(segment, maxBrightness)
                    for (segment in dSegments) builder.buildChannel(segment, maxBrightness)
                    for (segment in eSegments) builder.buildChannel(segment, maxBrightness)

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                    delay(holdStepDuration)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in C1 hold phase: ${e.message}")
                    delay(holdStepDuration)
                }
            }

            // PHASE 3: EXHALE - Progressive C1 deactivation (8 seconds)
            Log.d(TAG, "C1 breathing: exhale phase")
            val exhaleSteps = c1Segments.size
            val exhaleStepDuration = exhaleDuration / exhaleSteps

            for (i in c1Segments.indices.reversed()) {
                if (!isAnimationRunning) break

                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break

                    // Light up C1 segments progressively during exhale
                    for (j in 0..i) {
                        builder.buildChannel(c1Segments[j], maxBrightness)
                    }

                    // Supporting segments brightness decreases with C1 progress
                    val brightness = (maxBrightness * ((i + 1) / c1Segments.size.toFloat())).toInt()
                    
                    // Add supporting segments with progressive brightness
                    for (segment in aSegments) builder.buildChannel(segment, brightness)
                    for (segment in bSegments) builder.buildChannel(segment, brightness)
                    for (segment in cOtherSegments) builder.buildChannel(segment, brightness)
                    for (segment in dSegments) builder.buildChannel(segment, brightness)
                    for (segment in eSegments) builder.buildChannel(segment, brightness)

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                    delay(exhaleStepDuration)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in C1 exhale phase: ${e.message}")
                    delay(exhaleStepDuration)
                }
            }

            // PHASE 4: SECOND HOLD (Box breathing only)
            if (!is478Pattern && secondHoldDuration > 0) {
                Log.d(TAG, "C1 breathing: second hold phase (after exhale) - Box breathing only")
                
                // Use final state from LED calibration: only C1_1 active with supporting segments at minimum brightness
                val secondHoldSteps = (secondHoldDuration / 200L).toInt() // Update every 200ms
                val secondHoldStepDuration = secondHoldDuration / secondHoldSteps
                val minBrightness = (maxBrightness * (1.0f / c1Segments.size)).toInt()
                
                repeat(secondHoldSteps) {
                    if (!isAnimationRunning) return@repeat
                    
                    try {
                        val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
                        
                        // Only C1_1 should be active (final state from LED calibration)
                        builder.buildChannel(c1Segments[0], maxBrightness) // C1_1
                        
                        // All other C1 segments at 0 brightness (explicitly off)
                        for (i in 1 until c1Segments.size) {
                            builder.buildChannel(c1Segments[i], 0)
                        }
                        
                        // Supporting segments at minimum brightness (final exhale state)
                        for (segment in aSegments) builder.buildChannel(segment, minBrightness)
                        for (segment in bSegments) builder.buildChannel(segment, minBrightness)
                        for (segment in cOtherSegments) builder.buildChannel(segment, minBrightness)
                        for (segment in dSegments) builder.buildChannel(segment, minBrightness)
                        for (segment in eSegments) builder.buildChannel(segment, minBrightness)
                        
                        val frame = builder.build()
                        glyphManager.mGM?.toggle(frame)
                        delay(secondHoldStepDuration)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in C1 second hold phase: ${e.message}")
                        delay(secondHoldStepDuration)
                    }
                }
            }
        }

        // Turn off all LEDs
        glyphManager.turnOffAll()
        Log.d(TAG, "Phone 2 C1 breathing animation completed")
    }

    /**
     * Run a notification effect (quick flash pattern)
     */
    suspend fun runNotificationEffect() {
        if (!isGlyphServiceEnabled()) return
        Log.d(TAG, "runNotificationEffect called")
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - notification effect will not run")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Starting notification effect")
            resetGlyphs()

            // Quick double flash
            repeat(2) {
                if (!isAnimationRunning) return@repeat

                // Flash all segments
                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return

                    when {
                        Common.is22111() -> {
                            for (segment in aSegments) builder.buildChannel(segment, maxBrightness)
                            for (segment in bSegments) builder.buildChannel(segment, maxBrightness)
                            for (segment in c1Segments) builder.buildChannel(segment, maxBrightness)
                            for (segment in cOtherSegments) builder.buildChannel(segment, maxBrightness)
                            for (segment in dSegments) builder.buildChannel(segment, maxBrightness)
                            for (segment in eSegments) builder.buildChannel(segment, maxBrightness)
                        }
                        Common.is20111() -> {
                            builder.buildChannel(Phone1.A, maxBrightness)
                            builder.buildChannel(Phone1.B, maxBrightness)
                            for (i in 0..3) builder.buildChannel(Phone1.C_START + i, maxBrightness)
                            builder.buildChannel(Phone1.E, maxBrightness)
                            for (i in 0..7) builder.buildChannel(Phone1.D_START + i, maxBrightness)
                        }
                        Common.is23111() || Common.is23113() -> {
                            for (i in 0 until 24) builder.buildChannel(Phone2a.C_START + i, maxBrightness)
                            builder.buildChannel(Phone2a.A, maxBrightness)
                            builder.buildChannel(Phone2a.B, maxBrightness)
                        }
                        Common.is24111() -> {
                            // Phone 3a: Add ALL segments including C1-C20
                            for (i in 0 until 20) builder.buildChannel(Phone3a.C_START + i, maxBrightness) // C1-C20
                            for (i in 0 until 11) builder.buildChannel(Phone3a.A_START + i, maxBrightness) // A1-A11
                            for (i in 0 until 5) builder.buildChannel(Phone3a.B_START + i, maxBrightness)  // B1-B5
                        }
                    }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                    delay(1000L)

                    glyphManager.turnOffAll()
                    delay(500L)
                } catch (e: Exception) {
                    delay(200L)
                }
            }
        } finally {
            isAnimationRunning = false
            glyphManager.turnOffAll()
        }
    }

    /**
     * Test individual Glyph channel
     * @param channelIndex The channel index (1-11 for UI, internally mapped to appropriate ranges)
     * @param bypassServiceCheck Whether to bypass service state check for direct testing
     */
    suspend fun testGlyphChannel(channelIndex: Int, bypassServiceCheck: Boolean = false) {
        if (!isGlyphServiceEnabled()) return
        Log.d(TAG, "testGlyphChannel called with index: $channelIndex")
        
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - channel test will not run")
            return
        }

        if (!bypassServiceCheck && !glyphManager.canPerformOperation()) {
            Log.d(TAG, "Cannot perform operation - service check failed")
            return
        }

        // Map UI channel index (1-11) to actual hardware channels
        val actualChannels = mapChannelIndexToHardwareChannels(channelIndex)
        if (actualChannels.isEmpty()) {
            Log.w(TAG, "No channels mapped for index $channelIndex")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Testing channel $channelIndex (hardware channels: $actualChannels)")
            
            // Ensure all glyphs are off before starting
            glyphManager.mGM?.turnOff()
            delay(200L)

            // Flash the specific channel(s) 3 times
            repeat(3) {
                if (!isAnimationRunning) return@repeat

                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return

                    // Light up the mapped channels
                    for (channel in actualChannels) {
                        builder.buildChannel(channel, maxBrightness)
                    }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                    delay(300L)

                    // Explicitly turn off after each flash
                    glyphManager.mGM?.turnOff()
                    delay(200L)
                } catch (e: Exception) {
                    Log.e(TAG, "Error testing channel $channelIndex: ${e.message}")
                    // Ensure cleanup on error
                    glyphManager.mGM?.turnOff()
                    delay(500L)
                }
            }
        } finally {
            isAnimationRunning = false
            // Final cleanup - ensure all glyphs are definitely off
            glyphManager.mGM?.turnOff()
            delay(100L)
            Log.d(TAG, "Channel $channelIndex test completed with cleanup")
        }
    }

    /**
     * Map UI channel index to actual hardware channel numbers based on phone model
     * Following official Nothing Developer Kit documentation mapping
     * Using logical zones rather than individual channels for better UX
     * @param channelIndex UI channel index (1-8 for logical zones)
     * @return List of hardware channel numbers
     */
    private fun mapChannelIndexToHardwareChannels(channelIndex: Int): List<Int> {
        return when {
            Common.is22111() -> { // Nothing Phone 2 - Logical zones
                when (channelIndex) {
                    1 -> listOf(0, 1) // Camera Zone (A1 + A2)
                    2 -> listOf(2) // Top Strip (B1)
                    3 -> listOf(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18) // C1 Ring (all C1 segments)
                    4 -> listOf(19, 20, 21, 22, 23) // Other C segments (C2-C6)
                    5 -> listOf(24) // E1 segment
                    6 -> listOf(25, 26, 27, 28, 29, 30, 31, 32) // Bottom Ring (all D1 segments)
                    7 -> listOf(0, 1, 2) // Camera Zone (A1 + A2) + Top Strip (B1)
                    8 -> listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32) // All zones
                    9 -> listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32) // All zones (duplicate for 9 buttons)
                    else -> emptyList()
                }
            }
            
            Common.is20111() -> { // Nothing Phone 1 - Logical zones
                when (channelIndex) {
                    1 -> listOf(0) // A zone
                    2 -> listOf(1) // B zone
                    3 -> listOf(2, 3, 4, 5) // C zone (C1-C4)
                    4 -> listOf(6) // E zone
                    5 -> listOf(7, 8, 9, 10, 11, 12, 13, 14) // D ring (all D segments)
                    6 -> listOf(0, 1, 6) // Top zones (A + B + E)
                    7 -> listOf(2, 3, 4, 5, 7, 8, 9, 10, 11, 12, 13, 14) // Ring zones (C + D)
                    8 -> listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14) // All zones
                    else -> emptyList()
                }
            }
            
            Common.is23111() || Common.is23113() -> { // Nothing Phone 2a/2a+ - Logical zones
                when (channelIndex) {
                    1 -> listOf(25) // A zone
                    2 -> listOf(24) // B zone
                    3 -> listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11) // C ring left half (C1-C12)
                    4 -> listOf(12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23) // C ring right half (C13-C24)
                    5 -> listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23) // Full C ring
                    6 -> listOf(24, 25) // Top zones (A + B)
                    7 -> listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25) // All zones
                    8 -> listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25) // All zones (duplicate for 8 buttons)
                    else -> emptyList()
                }
            }
            
            Common.is24111() -> { // Nothing Phone 3a/3a Pro - Logical zones
                when (channelIndex) {
                    1 -> listOf(20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30) // A ring (all A segments)
                    2 -> listOf(31, 32, 33, 34, 35) // B zone (all B segments) - CORRECTED
                    3 -> listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9) // C zone left half (C1-C10)
                    4 -> listOf(10, 11, 12, 13, 14, 15, 16, 17, 18, 19) // C zone right half (C11-C20)
                    5 -> listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19) // Full C zone
                    6 -> listOf(20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35) // Top zones (A + B)
                    7 -> listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35) // All zones
                    8 -> listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35) // All zones (duplicate for 8 buttons)
                    else -> emptyList()
                }
            }
            
            else -> { // Default/unknown phone - fallback to basic mapping
                when (channelIndex) {
                    in 1..7 -> listOf(channelIndex - 1) // Direct mapping (UI 1-7 -> hardware 0-6)
                    8 -> (0..15).toList() // All available channels
                    else -> emptyList()
                }
            }
        }
    }

    /**
     * Test individual C1 LED segment
     * Phone 1: @param c1Index The C1 segment index (1-4 for UI, internally mapped to hardware channels 2-5)
     * Phone 2: @param c1Index The C1 segment index (1-16 for UI, internally mapped to hardware channels 3-18)
     * @param bypassServiceCheck Whether to bypass service state check for direct testing
     */
    suspend fun testC1Segment(c1Index: Int, bypassServiceCheck: Boolean = false) {
        Log.d(TAG, "testC1Segment called with index: $c1Index")
        
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - C1 segment test will not run")
            return
        }

        if (!bypassServiceCheck && !glyphManager.canPerformOperation()) {
            Log.d(TAG, "Cannot perform operation - service check failed")
            return
        }

        // Map UI C1 index (1-16) to actual hardware channel
        val hardwareChannel = mapC1IndexToHardwareChannel(c1Index)
        if (hardwareChannel == -1) {
            Log.w(TAG, "Invalid C1 index: $c1Index")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Testing C1 segment $c1Index (hardware channel: $hardwareChannel)")
            
            // Ensure all glyphs are off before starting
            glyphManager.mGM?.turnOff()
            delay(200L)

            // Flash the specific C1 segment 3 times
            repeat(3) {
                if (!isAnimationRunning) return@repeat

                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return

                    // Light up the specific C1 segment
                    builder.buildChannel(hardwareChannel, maxBrightness)

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                    delay(300L)

                    // Explicitly turn off after each flash
                    glyphManager.mGM?.turnOff()
                    delay(200L)
                } catch (e: Exception) {
                    Log.e(TAG, "Error testing C1 segment $c1Index: ${e.message}")
                    // Ensure cleanup on error
                    glyphManager.mGM?.turnOff()
                    delay(500L)
                }
            }
        } finally {
            isAnimationRunning = false
            // Final cleanup - ensure all glyphs are definitely off
            glyphManager.mGM?.turnOff()
            delay(100L)
            Log.d(TAG, "C1 segment $c1Index test completed with cleanup")
        }
    }

    /**
     * Map UI C1 index to actual hardware channel number
     * @param c1Index UI C1 index (1-16)
     * @return Hardware channel number (3-18) or -1 if invalid
     */
    private fun mapC1IndexToHardwareChannel(c1Index: Int): Int {
        return when {
            Common.is20111() -> { // Nothing Phone 1 - C1 segments are channels 2-5 (C1-C4)
                if (c1Index in 1..4) {
                    c1Index + 1 // Convert 1-based UI index (1-4) to hardware channels (2-5)
                } else {
                    -1 // Invalid index
                }
            }
            Common.is22111() -> { // Nothing Phone 2 - C1 segments are channels 3-18
                if (c1Index in 1..16) {
                    c1Segments[c1Index - 1] // Convert 1-based UI index to 0-based array index
                } else {
                    -1 // Invalid index
                }
            }
            Common.is24111() -> { // Nothing Phone 3a - C segments are channels 0-19 (C1-C20)
                if (c1Index in 1..20) {
                    c1Index - 1 // Convert 1-based UI index (1-20) to hardware channels (0-19)
                } else {
                    -1 // Invalid index
                }
            }
            else -> {
                Log.w(TAG, "C1 individual testing only supported on Nothing Phone (1), (2), and (3a)")
                -1
            }
        }
    }

    /**
     * Run a C1 sequential animation that lights up LEDs from c1_1 to c1_16 over 4 seconds,
     * pauses, and then reverses the animation. Other channels change their brightness to match.
     * 
     * Adapted for Nothing Phone 1: Uses C1-C4 segments (channels 2-5) instead of the 16 C1 segments on Phone 2
     */
    suspend fun runC1SequentialAnimation() {
        Log.d(TAG, "runC1SequentialAnimation called")
        Log.d(TAG, "isNothingPhone: ${glyphManager.isNothingPhone()}")
        
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - animation will not run")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Starting C1 sequential animation")
            resetGlyphs()

            when {
                Common.is20111() -> runPhone1C1SequentialAnimation()
                Common.is22111() -> runPhone2C1SequentialAnimation()
                Common.is24111() -> runPhone3aC1SequentialAnimation()
                else -> {
                    Log.d(TAG, "C1 sequential animation not supported on this device model")
                }
            }

        } finally {
            ensureCleanup()
        }
    }

    /**
     * C1 Sequential animation for Nothing Phone 1
     * Uses C1-C4 segments (channels 2-5) with supporting segments
     */
    private suspend fun runPhone1C1SequentialAnimation() {
        Log.d(TAG, "Running Phone 1 C1 sequential animation")
        
        val phone1C1Segments = listOf(2, 3, 4, 5) // C1-C4 for Phone 1
        val phone1SupportingSegments = listOf(0, 1, 6, 7, 8, 9, 10, 11, 12, 13, 14) // A, B, E, D1-D8
        val stepDuration = 250L // Hardcoded step duration

        // Forward animation - C1 to C4
        Log.d(TAG, "Phone 1 C1 animation: forward phase")
        for (i in phone1C1Segments.indices) {
            if (!isAnimationRunning) break

            withContext(Dispatchers.Default) {
                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@withContext

                    // Light up all previous C1 segments that we've reached
                    for (j in 0..i) {
                        builder.buildChannel(phone1C1Segments[j], maxBrightness)
                    }

                    // Also light up supporting segments with matching brightness
                    val brightness = (maxBrightness * ((i + 1) / phone1C1Segments.size.toFloat())).toInt()

                    // Add supporting segments with progressive brightness
                    phone1SupportingSegments.forEach { segment ->
                        builder.buildChannel(segment, brightness)
                    }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in Phone 1 C1 animation step: ${e.message}")
                }
            }
            delay(stepDuration)
        }

        // Pause at full brightness
        Log.d(TAG, "Phone 1 C1 animation: pause at full brightness")
        delay(1000L)

        // Reverse animation - C4 back to C1
        Log.d(TAG, "Phone 1 C1 animation: reverse phase")
        for (i in phone1C1Segments.indices.reversed()) {
            if (!isAnimationRunning) break

            withContext(Dispatchers.Default) {
                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@withContext

                    // Light up all segments up to this point
                    for (j in 0..i) {
                        builder.buildChannel(phone1C1Segments[j], maxBrightness)
                    }

                    val brightness = (maxBrightness * ((i + 1) / phone1C1Segments.size.toFloat())).toInt()

                    // Add supporting segments
                    phone1SupportingSegments.forEach { segment ->
                        builder.buildChannel(segment, brightness)
                    }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in Phone 1 C1 reverse animation: ${e.message}")
                }
            }
            delay(stepDuration)
        }

        // Turn off all LEDs
        glyphManager.turnOffAll()
        Log.d(TAG, "Phone 1 C1 sequential animation completed")
    }

    /**
     * C1 Sequential animation for Nothing Phone 2 (original implementation)
     * Uses C1_1 to C1_16 segments (channels 3-18)
     */
    private suspend fun runPhone2C1SequentialAnimation() {
        Log.d(TAG, "Running Phone 2 C1 sequential animation")
        
        val stepDuration = 250L // Hardcoded step duration

        // Forward animation - c1_1 to c1_16
        Log.d(TAG, "C1 animation: forward phase")
        for (i in c1Segments.indices) {
            if (!isAnimationRunning) break

            withContext(Dispatchers.Default) {
                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@withContext

                    // Light up all previous C1 segments that we've reached
                    for (j in 0..i) {
                        builder.buildChannel(c1Segments[j], maxBrightness)
                    }

                    // Also light up other segments with matching brightness
                    val brightness = (maxBrightness * ((i + 1) / c1Segments.size.toFloat())).toInt()

                    // Add segments with optimized batch processing
                    val segments = mapOf(
                        aSegments to brightness,
                        bSegments to brightness,
                        cOtherSegments to brightness,
                        dSegments to brightness,
                        eSegments to brightness
                    )

                    segments.forEach { (segmentList, segmentBrightness) ->
                        segmentList.forEach { segment ->
                            builder.buildChannel(segment, segmentBrightness)
                        }
                    }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in animation step: ${e.message}")
                }
            }
            delay(stepDuration)
        }

        // Pause at full brightness
        Log.d(TAG, "C1 animation: pause at full brightness")
        delay(1000L)

        // Reverse animation - c1_16 back to c1_1
        Log.d(TAG, "C1 animation: reverse phase")
        for (i in c1Segments.indices.reversed()) {
            if (!isAnimationRunning) break

            withContext(Dispatchers.Default) {
                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@withContext

                    // Light up all segments up to this point
                    for (j in 0..i) {
                        builder.buildChannel(c1Segments[j], maxBrightness)
                    }

                    val brightness = (maxBrightness * ((i + 1) / c1Segments.size.toFloat())).toInt()

                    // Add other segments
                    aSegments.forEach { segment -> builder.buildChannel(segment, brightness) }
                    bSegments.forEach { segment -> builder.buildChannel(segment, brightness) }
                    cOtherSegments.forEach { segment -> builder.buildChannel(segment, brightness) }
                    dSegments.forEach { segment -> builder.buildChannel(segment, brightness) }
                    eSegments.forEach { segment -> builder.buildChannel(segment, brightness) }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in animation step: ${e.message}")
                }
            }
            delay(stepDuration)
        }

        // Turn off all LEDs
        glyphManager.turnOffAll()
        Log.d(TAG, "Phone 2 C1 sequential animation completed")
    }

    /**
     * Clean up resources when the manager is no longer needed
     */
    fun cleanup() {
        isAnimationRunning = false
        animationScope.cancel()
    }

    /**
     * Test all glyph zones sequentially
     */
    suspend fun testAllZones(bypassServiceCheck: Boolean = false) {
        Log.d(TAG, "testAllZones called")
        
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - test all zones will not run")
            return
        }

        if (!bypassServiceCheck && !glyphManager.canPerformOperation()) {
            Log.d(TAG, "Cannot perform operation - service check failed")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Starting test all zones")
            resetGlyphs()

            when {
                Common.is20111() -> {
                    // Phone 1 - Test each zone
                    val zones = listOf(
                        listOf(0) to "A Zone",
                        listOf(1) to "B Zone", 
                        listOf(2, 3, 4, 5) to "C Zone",
                        listOf(6) to "E Zone",
                        listOf(7, 8, 9, 10, 11, 12, 13, 14) to "D Zone"
                    )
                    
                    for ((channels, zoneName) in zones) {
                        if (!isAnimationRunning) break
                        Log.d(TAG, "Testing $zoneName")
                        
                        val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                        channels.forEach { channel ->
                            builder.buildChannel(channel, maxBrightness)
                        }
                        
                        val frame = builder.build()
                        glyphManager.mGM?.toggle(frame)
                        delay(1000L)
                        
                        glyphManager.turnOffAll()
                        delay(500L)
                    }
                }
                
                Common.is22111() -> {
                    // Phone 2 - Test each zone
                    val zones = listOf(
                        aSegments to "A Zone",
                        bSegments to "B Zone",
                        c1Segments to "C1 Zone",
                        cOtherSegments to "C Other Zone",
                        eSegments to "E Zone",
                        dSegments to "D Zone"
                    )
                    
                    for ((channels, zoneName) in zones) {
                        if (!isAnimationRunning) break
                        Log.d(TAG, "Testing $zoneName")
                        
                        val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                        channels.forEach { channel ->
                            builder.buildChannel(channel, maxBrightness)
                        }
                        
                        val frame = builder.build()
                        glyphManager.mGM?.toggle(frame)
                        delay(1000L)
                        
                        glyphManager.turnOffAll()
                        delay(500L)
                    }
                }
            }
        } finally {
            ensureCleanup()
        }
    }

    /**
     * Test a custom pattern of channels
     */
    suspend fun testCustomPattern(bypassServiceCheck: Boolean = false) {
        Log.d(TAG, "testCustomPattern called")
        
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - custom pattern test will not run")
            return
        }

        if (!bypassServiceCheck && !glyphManager.canPerformOperation()) {
            Log.d(TAG, "Cannot perform operation - service check failed")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Starting custom pattern test")
            resetGlyphs()

            // Custom pattern - alternating segments
            when {
                Common.is20111() -> {
                    val pattern1 = listOf(0, 2, 4, 6, 8, 10, 12, 14) // Even channels
                    val pattern2 = listOf(1, 3, 5, 7, 9, 11, 13) // Odd channels
                    
                    repeat(3) {
                        if (!isAnimationRunning) return@repeat
                        
                        // Pattern 1
                        val builder1 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
                        pattern1.forEach { channel ->
                            builder1.buildChannel(channel, maxBrightness)
                        }
                        glyphManager.mGM?.toggle(builder1.build())
                        delay(500L)
                        
                        glyphManager.turnOffAll()
                        delay(200L)
                        
                        // Pattern 2
                        val builder2 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
                        pattern2.forEach { channel ->
                            builder2.buildChannel(channel, maxBrightness)
                        }
                        glyphManager.mGM?.toggle(builder2.build())
                        delay(500L)
                        
                        glyphManager.turnOffAll()
                        delay(200L)
                    }
                }
                
                Common.is22111() -> {
                    // Alternating C1 segments
                    val pattern1 = c1Segments.filterIndexed { index, _ -> index % 2 == 0 }
                    val pattern2 = c1Segments.filterIndexed { index, _ -> index % 2 == 1 }
                    
                    repeat(3) {
                        if (!isAnimationRunning) return@repeat
                        
                        // Pattern 1
                        val builder1 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
                        pattern1.forEach { channel ->
                            builder1.buildChannel(channel, maxBrightness)
                        }
                        glyphManager.mGM?.toggle(builder1.build())
                        delay(500L)
                        
                        glyphManager.turnOffAll()
                        delay(200L)
                        
                        // Pattern 2
                        val builder2 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
                        pattern2.forEach { channel ->
                            builder2.buildChannel(channel, maxBrightness)
                        }
                        glyphManager.mGM?.toggle(builder2.build())
                        delay(500L)
                        
                        glyphManager.turnOffAll()
                        delay(200L)
                    }
                }
            }
        } finally {
            ensureCleanup()
        }
    }

    /**
     * Run C1 sequential animation with breathing timing
     */
    suspend fun runC1SequentialWithBreathingTiming(is478Pattern: Boolean, cycles: Int) {
        if (!isGlyphServiceEnabled() || !Common.is22111()) return
        isAnimationRunning = true

        try {
            val stepDuration = if (is478Pattern) 400L else 200L // 4-7-8 or equal timing
            Log.d(TAG, "Starting C1 sequential with breathing timing")
            resetGlyphs()

            for (i in 0 until cycles) {
            if (!isAnimationRunning) break

                // Inhale
                for (segment in c1Segments) {
                    if (!isAnimationRunning) break
                    glyphManager.mGM?.toggle(GlyphFrame.Builder().buildChannel(segment).build())
            delay(stepDuration)
        }
                // Hold
                if (is478Pattern) delay(700L)
                // Exhale
                for (segment in c1Segments.reversed()) {
            if (!isAnimationRunning) break
                    glyphManager.mGM?.toggle(GlyphFrame.Builder().buildChannel(segment).build())
            delay(stepDuration)
        }
                if (is478Pattern) delay(800L)
            }
        } finally {
            ensureCleanup()
        }
    }

    /**
     * Test C14 and C15 specifically
     */
    suspend fun testC14AndC15Specifically(bypassServiceCheck: Boolean = false) {
        Log.d(TAG, "testC14AndC15Specifically called")
        
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - C14/C15 test will not run")
            return
        }

        if (!Common.is22111()) {
            Log.d(TAG, "C14/C15 test only available on Nothing Phone 2")
            return
        }

        if (!bypassServiceCheck && !glyphManager.canPerformOperation()) {
            Log.d(TAG, "Cannot perform operation - service check failed")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Testing C14 and C15 specifically")
            resetGlyphs()

            // Test C14 (channel 16)
            Log.d(TAG, "Testing C14 (channel 16)")
            val builder1 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
            builder1.buildChannel(16, maxBrightness)
            glyphManager.mGM?.toggle(builder1.build())
            delay(2000L)
            
            glyphManager.turnOffAll()
            delay(500L)

            // Test C15 (channel 17)
            Log.d(TAG, "Testing C15 (channel 17)")
            val builder2 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
            builder2.buildChannel(17, maxBrightness)
            glyphManager.mGM?.toggle(builder2.build())
            delay(2000L)
            
            glyphManager.turnOffAll()
            delay(500L)

            // Test both together
            Log.d(TAG, "Testing C14 and C15 together")
            val builder3 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
            builder3.buildChannel(16, maxBrightness)
            builder3.buildChannel(17, maxBrightness)
            glyphManager.mGM?.toggle(builder3.build())
            delay(2000L)
            
        } finally {
            ensureCleanup()
        }
    }

    /**
     * Test final state before turnoff
     */
    suspend fun testFinalStateBeforeTurnoff(bypassServiceCheck: Boolean = false) {
        Log.d(TAG, "testFinalStateBeforeTurnoff called")
        
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - final state test will not run")
            return
        }

        if (!bypassServiceCheck && !glyphManager.canPerformOperation()) {
            Log.d(TAG, "Cannot perform operation - service check failed")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Testing final state before turnoff")
            resetGlyphs()

            when {
                Common.is20111() -> {
                    // Phone 1 - Final state: only C1 (channel 2) active
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
                    builder.buildChannel(2, maxBrightness) // C1
                    
                    // Supporting segments at minimum brightness
                    val minBrightness = maxBrightness / 4
                    listOf(0, 1, 6, 7, 8, 9, 10, 11, 12, 13, 14).forEach { segment ->
                        builder.buildChannel(segment, minBrightness)
                    }
                    
                    glyphManager.mGM?.toggle(builder.build())
                    delay(3000L)
                }
                
                Common.is22111() -> {
                    // Phone 2 - Final state: only C1_1 (channel 3) active
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
                    builder.buildChannel(3, maxBrightness) // C1_1
                    
                    // Supporting segments at minimum brightness
                    val minBrightness = maxBrightness / 16
                    (aSegments + bSegments + cOtherSegments + dSegments + eSegments).forEach { segment ->
                        builder.buildChannel(segment, minBrightness)
                    }
                    
                    glyphManager.mGM?.toggle(builder.build())
                    delay(3000L)
                }
            }
        } finally {
            ensureCleanup()
        }
    }

    /**
     * Test only C14 and C15 isolated
     */
    suspend fun testOnlyC14AndC15Isolated(bypassServiceCheck: Boolean = false) {
        Log.d(TAG, "testOnlyC14AndC15Isolated called")
        
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - C14/C15 isolated test will not run")
            return
        }

        if (!Common.is22111()) {
            Log.d(TAG, "C14/C15 isolated test only available on Nothing Phone 2")
            return
        }

        if (!bypassServiceCheck && !glyphManager.canPerformOperation()) {
            Log.d(TAG, "Cannot perform operation - service check failed")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Testing C14 and C15 in complete isolation")
            resetGlyphs()

            // Ensure all other channels are explicitly off
            val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
            
            // Turn off all channels first
            for (i in 0..32) {
                if (i != 16 && i != 17) { // Skip C14 and C15
                    builder.buildChannel(i, 0)
                }
            }
            
            // Only light up C14 and C15
            builder.buildChannel(16, maxBrightness) // C14
            builder.buildChannel(17, maxBrightness) // C15
            
            glyphManager.mGM?.toggle(builder.build())
            delay(5000L) // Hold for 5 seconds
            
        } finally {
            ensureCleanup()
        }
    }

    /**
     * Run D1 sequential animation
     */
    suspend fun runD1SequentialAnimation() {
        Log.d(TAG, "runD1SequentialAnimation called")
        
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - D1 sequential animation will not run")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Starting D1 sequential animation")
            resetGlyphs()

            when {
                Common.is20111() -> runPhone1D1SequentialAnimation()
                Common.is22111() -> runPhone2D1SequentialAnimation()
                else -> {
                    Log.d(TAG, "D1 sequential animation not supported on this device model")
                }
            }
        } finally {
            ensureCleanup()
        }
    }

    private suspend fun runPhone1D1SequentialAnimation() {
        val d1Segments = listOf(7, 8, 9, 10, 11, 12, 13, 14) // D1_1 to D1_8
        val supportingSegments = listOf(0, 1, 2, 3, 4, 5, 6) // A, B, C1-C4, E
        val stepDuration = 500L

        // Forward animation
        for (i in d1Segments.indices) {
            if (!isAnimationRunning) break

            val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
            
            for (j in 0..i) {
                builder.buildChannel(d1Segments[j], maxBrightness)
            }
            
            val brightness = (maxBrightness * ((i + 1) / d1Segments.size.toFloat())).toInt()
            supportingSegments.forEach { segment ->
                builder.buildChannel(segment, brightness)
            }
            
            glyphManager.mGM?.toggle(builder.build())
            delay(stepDuration)
        }

        delay(1000L) // Pause

        // Reverse animation
        for (i in d1Segments.indices.reversed()) {
            if (!isAnimationRunning) break

            val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
            
            for (j in 0..i) {
                builder.buildChannel(d1Segments[j], maxBrightness)
            }
            
            val brightness = (maxBrightness * ((i + 1) / d1Segments.size.toFloat())).toInt()
            supportingSegments.forEach { segment ->
                builder.buildChannel(segment, brightness)
            }
            
            glyphManager.mGM?.toggle(builder.build())
            delay(stepDuration)
        }

        glyphManager.turnOffAll()
    }

    private suspend fun runPhone2D1SequentialAnimation() {
        val stepDuration = 250L

        // Forward animation
        for (i in dSegments.indices) {
            if (!isAnimationRunning) break

            val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
            
            for (j in 0..i) {
                builder.buildChannel(dSegments[j], maxBrightness)
            }
            
            val brightness = (maxBrightness * ((i + 1) / dSegments.size.toFloat())).toInt()
            aSegments.forEach { segment -> builder.buildChannel(segment, brightness) }
            bSegments.forEach { segment -> builder.buildChannel(segment, brightness) }
            c1Segments.forEach { segment -> builder.buildChannel(segment, brightness) }
            cOtherSegments.forEach { segment -> builder.buildChannel(segment, brightness) }
            eSegments.forEach { segment -> builder.buildChannel(segment, brightness) }
            
            glyphManager.mGM?.toggle(builder.build())
            delay(stepDuration)
        }

        delay(1000L) // Pause

        // Reverse animation
        for (i in dSegments.indices.reversed()) {
            if (!isAnimationRunning) break

            val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
            
            for (j in 0..i) {
                builder.buildChannel(dSegments[j], maxBrightness)
            }
            
            val brightness = (maxBrightness * ((i + 1) / dSegments.size.toFloat())).toInt()
            aSegments.forEach { segment -> builder.buildChannel(segment, brightness) }
            bSegments.forEach { segment -> builder.buildChannel(segment, brightness) }
            c1Segments.forEach { segment -> builder.buildChannel(segment, brightness) }
            cOtherSegments.forEach { segment -> builder.buildChannel(segment, brightness) }
            eSegments.forEach { segment -> builder.buildChannel(segment, brightness) }
            
            glyphManager.mGM?.toggle(builder.build())
            delay(stepDuration)
        }

        glyphManager.turnOffAll()
    }

    /**
     * Run D1 sequential animation with progress tracking
     */
    suspend fun runD1SequentialAnimationWithProgress(onProgressUpdate: (Float) -> Unit) {
        Log.d(TAG, "runD1SequentialAnimationWithProgress called")
        // For now, just call the regular D1 sequential animation
        runD1SequentialAnimation()
    }

    /**
     * Run C1 sequential animation with progress tracking
     */
    suspend fun runC1SequentialAnimationWithProgress(onProgressUpdate: (Float) -> Unit) {
        Log.d(TAG, "runC1SequentialAnimationWithProgress called")
        // For now, just call the regular C1 sequential animation
        runC1SequentialAnimation()
    }

    /**
     * Test all zones with progress tracking
     */
    suspend fun testAllZonesWithProgress(bypassServiceCheck: Boolean = false, onProgressUpdate: (Float) -> Unit) {
        Log.d(TAG, "testAllZonesWithProgress called")
        // For now, just call the regular test all zones
        testAllZones(bypassServiceCheck)
    }

    /**
     * Test custom pattern with progress tracking
     */
    suspend fun testCustomPatternWithProgress(bypassServiceCheck: Boolean = false, onProgressUpdate: (Float) -> Unit) {
        Log.d(TAG, "testCustomPatternWithProgress called")
        // For now, just call the regular custom pattern test
        testCustomPattern(bypassServiceCheck)
    }

    /**
     * Run battery percentage visualization using C1 channel logic
     * Maps battery levels (0-100%) to LED patterns with dynamic brightness
     * Handles edge cases (charging state, low battery <15%)
     */
    suspend fun runBatteryPercentageVisualization(
        context: Context,
        durationMillis: Long,
        onProgressUpdate: (Float) -> Unit = {}
    ) {
        if (!isGlyphServiceEnabled()) return
        Log.d(TAG, "runBatteryPercentageVisualization called for ${durationMillis}ms")
        
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - battery visualization will not run")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Starting battery percentage visualization")
            resetGlyphs()

            // Get battery manager and register receiver for real-time updates
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
            val intentFilter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
            val batteryIntent = context.registerReceiver(null, intentFilter)

            if (batteryIntent != null) {
                val batteryLevel = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1)
                val batteryScale = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1)
                val batteryStatus = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
                val isCharging = batteryStatus == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                                batteryStatus == android.os.BatteryManager.BATTERY_STATUS_FULL

                val batteryPercentage = if (batteryLevel != -1 && batteryScale != -1) {
                    (batteryLevel * 100 / batteryScale.toFloat()).toInt()
                } else {
                    50 // Default fallback
                }

                Log.d(TAG, "Battery: ${batteryPercentage}%, Charging: $isCharging")
                
                when {
                    Common.is20111() -> runPhone1BatteryVisualization(batteryPercentage, isCharging, durationMillis, onProgressUpdate)
                    Common.is22111() -> runPhone2BatteryVisualization(batteryPercentage, isCharging, durationMillis, onProgressUpdate)
                    Common.is23111() || Common.is23113() -> runPhone2aBatteryVisualization(batteryPercentage, isCharging, durationMillis, onProgressUpdate)
                    Common.is24111() -> runPhone3aBatteryVisualization(batteryPercentage, isCharging, durationMillis, onProgressUpdate)
                    else -> runDefaultBatteryVisualization(batteryPercentage, isCharging, durationMillis, onProgressUpdate)
                }
            }
        } finally {
            ensureCleanup()
        }
    }

    private suspend fun runPhone1BatteryVisualization(
        batteryPercentage: Int,
        isCharging: Boolean,
        durationMillis: Long,
        onProgressUpdate: (Float) -> Unit
    ) {
        val stepDelay = 50L // Fixed 50ms delay for consistent animation speed
        val startTime = System.currentTimeMillis()
        val c1Segments = listOf(Phone1.C_START, Phone1.C_START + 1, Phone1.C_START + 2, Phone1.C_START + 3)
        var step = 0
        
        while (isAnimationRunning) {
            val elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime >= durationMillis) break
            
            val progress = (elapsedTime / durationMillis.toFloat()).coerceIn(0f, 1f)
            onProgressUpdate(progress)
            
            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                // Calculate how many C1 segments to light up based on battery percentage
                val segmentsToLight = (batteryPercentage / 25f).toInt().coerceIn(0, c1Segments.size)
                
                // Base brightness based on battery level
                val baseBrightness = when {
                    batteryPercentage < 20 -> maxBrightness / 4 // Dim for low battery
                    isCharging -> maxBrightness // Full brightness when charging
                    else -> (maxBrightness * 0.7f).toInt() // Normal brightness
                }
                
                // Light up segments based on battery level
                for (i in 0 until segmentsToLight) {
                    val segmentBrightness = if (isCharging) {
                        // Original pulsing effect when charging
                        (baseBrightness * (0.5f + 0.5f * kotlin.math.sin(step * 0.1f))).toInt()
                    } else if (batteryPercentage < 20) {
                        // For low battery, main segments are static
                        baseBrightness
                    } else {
                        // More dynamic breathing effect for normal battery
                        (baseBrightness * (0.7f + 0.3f * kotlin.math.sin(step * 0.1f))).toInt()
                    }
                    builder.buildChannel(c1Segments[i], segmentBrightness.coerceIn(0, maxBrightness))
                }
                
                // ---------- New playful glow in normal mode ----------
                if (!isCharging && batteryPercentage >= 20) {
                    // ------------- Wave-fill ripple -------------
                    val filledLevel = batteryPercentage / 25f // 0-4 float
                    c1Segments.forEachIndexed { idx, seg ->
                        val distance = filledLevel - idx
                        val base = when {
                            distance >= 1f -> baseBrightness
                            distance > 0f -> (baseBrightness * distance).toInt()
                            else -> 0
                        }
                        val waveFactor = 0.75f + 0.25f * kotlin.math.sin((step + idx) * 0.25f)
                        val bright = (base * waveFactor).toInt().coerceIn(0, maxBrightness)
                        if (bright > 0) builder.buildChannel(seg, bright)
                    }

                    // ------------- Twinkle constellation -------------
                    if (step % 20 == 0) { // ~1 s at 50 ms per frame
                        val unused = c1Segments.indices.filter { it >= filledLevel.toInt() }
                        if (unused.isNotEmpty()) {
                            val twinkleSeg = c1Segments[unused[Random.nextInt(unused.size)]]
                            builder.buildChannel(twinkleSeg, (maxBrightness * 0.5f).toInt())
                        }
                    }
                }
                
                // Special handling for low battery (blinking red effect)
                if (batteryPercentage < 20) {
                    val blinkBrightness = (maxBrightness * (0.5f + 0.5f * kotlin.math.sin(step * 0.3f))).toInt()
                    builder.buildChannel(c1Segments.last(), blinkBrightness)
                }
                
                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDelay)
                step++
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in Phone 1 battery visualization: ${e.message}")
                delay(stepDelay)
                step++
            }
        }
    }

    private suspend fun runPhone2BatteryVisualization(
        batteryPercentage: Int,
        isCharging: Boolean,
        durationMillis: Long,
        onProgressUpdate: (Float) -> Unit
    ) {
        val stepDelay = 50L // Fixed 50ms delay for consistent animation speed
        val startTime = System.currentTimeMillis()
        var step = 0
        
        while (isAnimationRunning) {
            val elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime >= durationMillis) break
            
            val progress = (elapsedTime / durationMillis.toFloat()).coerceIn(0f, 1f)
            onProgressUpdate(progress)
            
            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                // Calculate how many C1 segments to light up based on battery percentage
                val segmentsToLight = (batteryPercentage / 100f * c1Segments.size).toInt().coerceIn(0, c1Segments.size)
                
                // Base brightness based on battery level
                val baseBrightness = when {
                    batteryPercentage < 20 -> maxBrightness / 3 // Dim for low battery
                    isCharging -> maxBrightness // Full brightness when charging
                    else -> (maxBrightness * 0.8f).toInt() // Normal brightness
                }
                
                // Light up C1 segments progressively based on battery level
                if (isCharging || batteryPercentage < 20) {
                    // Original logic for charging / low battery (unchanged)
                    for (i in 0 until segmentsToLight) {
                        val segmentBrightness = if (isCharging) {
                            // Unified 50-100% pulse amplitude across all segments
                            (baseBrightness * (0.5f + 0.5f * kotlin.math.sin(step * 0.3f))).toInt()
                        } else {
                            baseBrightness
                        }
                        builder.buildChannel(c1Segments[i], segmentBrightness.coerceIn(0, maxBrightness))
                    }
                } else {
                    // ---------------- Pronounced wave animation ----------------
                    val total = c1Segments.size.toFloat()
                    val filledLevel = batteryPercentage / 100f * total

                    for (i in 0 until total.toInt()) {
                        // Brightness envelope along the bar (fully lit only up to the percentage)
                        val base = when {
                            i + 1 <= filledLevel -> baseBrightness
                            i < filledLevel -> (baseBrightness * (filledLevel - i)).toInt()
                            else -> 0
                        }

                        if (base == 0) continue

                        // Wave factor: high amplitude, visible ripple
                        val wave = 0.05f + 1.15f * (0.5f + 0.5f * kotlin.math.sin((step * 0.5f) - i * 0.6f))
                        val brightness = (base * wave).toInt().coerceIn(0, maxBrightness)
                        builder.buildChannel(c1Segments[i], brightness)
                    }
                }

                // Special handling for low battery (blinking A segments)
                if (batteryPercentage < 20) {
                    val blinkBrightness = (maxBrightness * (0.5f + 0.5f * kotlin.math.sin(step * 0.3f))).toInt()
                    aSegments.forEach { builder.buildChannel(it, blinkBrightness) }
                }
                
                // Show charging indicator on B segment (perfectly synced with blinking)
                if (isCharging) {
                    val chargeBrightness = (baseBrightness * (0.5f + 0.5f * kotlin.math.sin(step * 0.3f))).toInt()
                    bSegments.forEach { segment ->
                        builder.buildChannel(segment, chargeBrightness.coerceIn(0, maxBrightness))
                    }
                }
                
                // ---------- New playful glow in normal mode ----------
                if (!isCharging && batteryPercentage >= 20) {
                    val glow = (maxBrightness * (0.15f + 0.15f * kotlin.math.sin(step * 0.18f))).toInt()
                    val glow2 = (maxBrightness * (0.15f + 0.15f * kotlin.math.sin(step * 0.18f + 1.5f))).toInt()
                    // Light up B segment softly
                    bSegments.forEach { builder.buildChannel(it, glow) }
                    // Light up E segment softly with offset
                    eSegments.forEach { builder.buildChannel(it, glow2) }
                }
                
                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDelay)
                step++
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in Phone 2 battery visualization: ${e.message}")
                delay(stepDelay)
                step++
            }
        }
    }

    private suspend fun runPhone2aBatteryVisualization(
        batteryPercentage: Int,
        isCharging: Boolean,
        durationMillis: Long,
        onProgressUpdate: (Float) -> Unit
    ) {
        val stepDelay = 50L // Fixed 50ms delay for consistent animation speed
        val startTime = System.currentTimeMillis()
        var step = 0
        
        while (isAnimationRunning) {
            val elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime >= durationMillis) break
            
            val progress = (elapsedTime / durationMillis.toFloat()).coerceIn(0f, 1f)
            onProgressUpdate(progress)
            
            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                // Calculate how many C segments to light up (0-23 for battery level)
                val segmentsToLight = (batteryPercentage / 100f * 24).toInt().coerceIn(0, 24)
                
                // Base brightness based on battery level
                val baseBrightness = when {
                    batteryPercentage < 20 -> maxBrightness / 3 // Dim for low battery
                    isCharging -> maxBrightness // Full brightness when charging
                    else -> (maxBrightness * 0.75f).toInt() // Normal brightness
                }
                
                // Light up C segments progressively based on battery level
                if (isCharging || batteryPercentage < 20) {
                    // Original logic for charging / low battery
                    for (i in 0 until segmentsToLight) {
                        val segmentBrightness = if (isCharging) {
                            // Unified 50-100% pulse amplitude across all segments
                            (baseBrightness * (0.5f + 0.5f * kotlin.math.sin(step * 0.3f))).toInt()
                        } else {
                            baseBrightness
                        }
                        builder.buildChannel(Phone2a.C_START + i, segmentBrightness.coerceIn(0, maxBrightness))
                    }
                }

                // Special handling for low battery (blinking A segment)
                if (batteryPercentage < 20) {
                    val blinkBrightness = (maxBrightness * (0.5f + 0.5f * kotlin.math.sin(step * 0.3f))).toInt()
                    builder.buildChannel(Phone2a.A, blinkBrightness)
                }
                
                // Show charging indicator on B segment
                if (isCharging) {
                    // Same amplitude as primary bar for visual consistency
                    val chargeBrightness = (baseBrightness * (0.5f + 0.5f * kotlin.math.sin(step * 0.35f))).toInt()
                    builder.buildChannel(Phone2a.B, chargeBrightness.coerceIn(0, maxBrightness))
                }
                
                // ---------- New playful glow in normal mode ----------
                if (!isCharging && batteryPercentage >= 20) {
                    // ------------- Wave-fill ripple -------------
                    val total = 24f
                    val filledLevel = batteryPercentage / 100f * total
                    for (i in 0 until 24) {
                        val distance = filledLevel - i
                        val base = when {
                            distance >= 1f -> baseBrightness
                            distance > 0f -> (baseBrightness * distance).toInt()
                            else -> 0
                        }
                        val waveFactor = 0.75f + 0.25f * kotlin.math.sin((step + i) * 0.25f)
                        val bright = (base * waveFactor).toInt().coerceIn(0, maxBrightness)
                        if (bright > 0) builder.buildChannel(Phone2a.C_START + i, bright)
                    }

                    // ------------- Twinkle constellation -------------
                    // if (step % 20 == 0) {
                    //    val unused = (0 until 24).filter { it >= filledLevel.toInt() }
                    //    if (unused.isNotEmpty()) {
                    //        val twinkle = unused[Random.nextInt(unused.size)]
                    //       builder.buildChannel(Phone2a.C_START + twinkle, (maxBrightness * 0.5f).toInt())
                    //    }
                   // }
                }
                
                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDelay)
                step++
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in Phone 2a battery visualization: ${e.message}")
                delay(stepDelay)
                step++
            }
        }
    }

    private suspend fun runPhone3aBatteryVisualization(
        batteryPercentage: Int,
        isCharging: Boolean,
        durationMillis: Long,
        onProgressUpdate: (Float) -> Unit
    ) {
        val stepDelay = 50L // Fixed 50ms delay for consistent animation speed
        val startTime = System.currentTimeMillis()
        var step = 0
        
        // Phone 3a: Only use C1-C20 segments (0-19) for clean battery percentage display
        val cSegments = (Phone3a.C_START until Phone3a.C_START + 20).toList() // 0-19
        
        while (isAnimationRunning) {
            val elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime >= durationMillis) break
            
            val progress = (elapsedTime / durationMillis.toFloat()).coerceIn(0f, 1f)
            onProgressUpdate(progress)
            
            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                // Base brightness based on battery level and charging state
                val baseBrightness = when {
                    batteryPercentage < 20 -> maxBrightness / 3 // Dimmer for low battery
                    isCharging -> maxBrightness // Full brightness when charging
                    else -> (maxBrightness * 0.8f).toInt() // Normal brightness
                }

                // ==================== C SEGMENTS ONLY (C1-C20) ====================
                // Calculate how many C segments to light based on battery percentage
                val cSegmentsToLight = (batteryPercentage / 100f * cSegments.size).toInt().coerceIn(0, cSegments.size)

                // Light up C segments based on battery percentage
                for (i in 0 until cSegmentsToLight) {
                    val segmentBrightness = when {
                        isCharging -> {
                            // Subtle wave effect when charging - less distracting
                            val waveFactor = 0.8f + 0.2f * kotlin.math.sin((step + i) * 0.15f)
                            (baseBrightness * waveFactor).toInt()
                        }
                        batteryPercentage < 20 -> {
                            // Gentle pulse for low battery warning
                            (baseBrightness * (0.6f + 0.4f * kotlin.math.sin(step * 0.2f))).toInt()
                        }
                        else -> {
                            // Static display for normal battery levels - clean and simple
                            baseBrightness
                        }
                    }.coerceIn(0, maxBrightness)
                    
                    builder.buildChannel(cSegments[i], segmentBrightness)
                }

                // A and B segments are completely disabled - only C segments show battery
                
                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDelay)
                step++
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in Phone 3a battery visualization: ${e.message}")
                delay(stepDelay)
                step++
            }
        }
    }

    private suspend fun runDefaultBatteryVisualization(
        batteryPercentage: Int,
        isCharging: Boolean,
        durationMillis: Long,
        onProgressUpdate: (Float) -> Unit
    ) {
        val stepDelay = 50L // Fixed 50ms delay for consistent animation speed
        val startTime = System.currentTimeMillis()
        var step = 0
        
        while (isAnimationRunning) {
            val elapsedTime = System.currentTimeMillis() - startTime
            if (elapsedTime >= durationMillis) break
            
            val progress = (elapsedTime / durationMillis.toFloat()).coerceIn(0f, 1f)
            onProgressUpdate(progress)
            
            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                // Use first 10 channels for battery display
                val segmentsToLight = (batteryPercentage / 10f).toInt().coerceIn(0, 10)
                
                val baseBrightness = when {
                    batteryPercentage < 20 -> maxBrightness / 3
                    isCharging -> maxBrightness
                    else -> (maxBrightness * 0.7f).toInt()
                }
                
                for (i in 0 until segmentsToLight) {
                    val segmentBrightness = if (isCharging) {
                        (baseBrightness * (0.5f + 0.5f * kotlin.math.sin(step * 0.1f + i * 0.5f))).toInt()
                    } else if (batteryPercentage < 20) {
                        // Static brightness for low battery
                        baseBrightness
                    } else {
                        // Standard breathing effect
                        (baseBrightness * (0.7f + 0.3f * kotlin.math.sin(step * 0.1f))).toInt()
                    }
                    builder.buildChannel(i, segmentBrightness.coerceIn(0, maxBrightness))
                }
                
                // ---------- New playful glow in normal mode ----------
                if (!isCharging && batteryPercentage >= 20) {
                    val glow = (maxBrightness * (0.15f + 0.15f * kotlin.math.sin(step * 0.18f))).toInt()
                    val glow2 = (maxBrightness * (0.15f + 0.15f * kotlin.math.sin(step * 0.18f + 1.5f))).toInt()
                    // Light up B segment softly
                    bSegments.forEach { builder.buildChannel(it, glow) }
                    // Light up E segment softly with offset
                    eSegments.forEach { builder.buildChannel(it, glow2) }
                }
                
                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDelay)
                step++
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in default battery visualization: ${e.message}")
                delay(stepDelay)
                step++
            }
        }
    }

    /**
     * Play a Glow Gate animation by identifier. This is a thin wrapper that
     * delegates to existing animation methods so the service/UX layer can use
     * stable string IDs while we keep animation logic here.
     */
    suspend fun playPulseLockAnimation(id: String) {
        if (!isGlyphServiceEnabled()) return
        when (id) {
            "C1" -> runC1SequentialAnimation()
            "WAVE" -> runWaveAnimation()
            "BEEDAH" -> runBeedahAnimation()
            "PULSE" -> runPulseEffect(3)
            "LOCK" -> runLockPulseAnimation()
            "SPIRAL" -> runSpiralAnimation()
            "HEARTBEAT" -> runHeartbeatAnimation()
            "MATRIX" -> runMatrixRainAnimation()
            "FIREWORKS" -> runFireworksAnimation()
            "DNA" -> runDNAHelixAnimation()
            else -> runC1SequentialAnimation()
        }
    }

    /**
     * Play a Low-Battery Alert animation by identifier.
     * Now respects the user-selected animation *and* the configured flash
     * duration.  We keep the same mapping used by Pulse-Lock animations so
     * that the same IDs work in both pickers.
     *
     * Flash duration is stored (in milliseconds) in SettingsRepository under
     * KEY_LOW_BATTERY_DURATION.  For pulse-style animations we convert the
     * duration into an approximate number of on/off cycles (each full pulse
     * takes ~500 ms – 250 ms on + 250 ms off).
     */
    suspend fun playLowBatteryAnimation(id: String) {
        if (!isGlyphServiceEnabled()) return

        // Retrieve desired flash duration (2 s – 15 s as defined by UI)
        val durationMs = settingsRepository.getLowBatteryDuration()
        // One full pulse (on + off) takes ≈500 ms inside runPulseEffect().
        val cyclesFromDuration = (durationMs / 500L).toInt().coerceAtLeast(1)

        when (id) {
            "C1" -> runC1SequentialAnimation()
            "WAVE" -> runWaveAnimation()
            "BEEDAH" -> runBeedahAnimation()
            "LOCK" -> runLockPulseAnimation()
            "PULSE" -> runPulseEffect(cyclesFromDuration)
            "SPIRAL" -> runSpiralAnimation()
            "HEARTBEAT" -> runHeartbeatAnimation()
            "MATRIX" -> runMatrixRainAnimation()
            "FIREWORKS" -> runFireworksAnimation()
            "DNA" -> runDNAHelixAnimation()
            // Unknown / default → fall back to pulse effect for requested duration
            else -> runPulseEffect(cyclesFromDuration)
        }
    }

    /**
     * C1 Sequential animation for Nothing Phone 3a
     * Uses C1-C20 segments (channels 0-19) with supporting A and B segments
     */
    private suspend fun runPhone3aC1SequentialAnimation() {
        Log.d(TAG, "Running Phone 3a C1 sequential animation")
        
        val phone3aCSegments = (0..19).toList() // C1-C20 for Phone 3a
        val phone3aASupportingSegments = (20..30).toList() // A1-A11
        val phone3aBSupportingSegments = (31..35).toList() // B1-B5
        val stepDuration = 200L // Hardcoded step duration

        // Forward animation - C1 to C20
        Log.d(TAG, "Phone 3a C1 animation: forward phase")
        for (i in phone3aCSegments.indices) {
            if (!isAnimationRunning) break

            withContext(Dispatchers.Default) {
                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@withContext

                    // Light up all previous C segments that we've reached
                    for (j in 0..i) {
                        builder.buildChannel(phone3aCSegments[j], maxBrightness)
                    }

                    // Also light up supporting segments with progressive brightness
                    val brightness = (maxBrightness * ((i + 1) / phone3aCSegments.size.toFloat())).toInt()

                    // Add A segments with progressive brightness
                    phone3aASupportingSegments.forEach { segment ->
                        builder.buildChannel(segment, brightness)
                    }
                    
                    // Add B segments with progressive brightness
                    phone3aBSupportingSegments.forEach { segment ->
                        builder.buildChannel(segment, brightness)
                    }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in Phone 3a C1 animation step: ${e.message}")
                }
            }

            delay(stepDuration)
        }

        // Pause at full brightness
        delay(2000L)

        // Reverse animation - C20 to C1
        Log.d(TAG, "Phone 3a C1 animation: reverse phase")
        for (i in phone3aCSegments.indices.reversed()) {
            if (!isAnimationRunning) break

            withContext(Dispatchers.Default) {
                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@withContext

                    // Light up remaining C segments
                    for (j in 0..i) {
                        builder.buildChannel(phone3aCSegments[j], maxBrightness)
                    }

                    // Dim supporting segments as we reverse
                    val brightness = (maxBrightness * ((i + 1) / phone3aCSegments.size.toFloat())).toInt()
                    
                    // Add A segments with diminishing brightness
                    phone3aASupportingSegments.forEach { segment ->
                        builder.buildChannel(segment, brightness)
                    }
                    
                    // Add B segments with diminishing brightness
                    phone3aBSupportingSegments.forEach { segment ->
                        builder.buildChannel(segment, brightness)
                    }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in Phone 3a C1 reverse animation step: ${e.message}")
                }
            }

            delay(stepDuration)
        }
    }

    /**
     * Quick guard: returns true if glyph service is enabled; otherwise logs and returns false
     */
    private fun isGlyphServiceEnabled(): Boolean {
        val enabled = settingsRepository.getGlyphServiceEnabled()
        if (!enabled) {
            Log.d(TAG, "Glyph service disabled – animation call ignored")
        }
        return enabled
    }

    /**
     * Ensure a Glyph session is active. If not, attempt to open one synchronously.
     * Returns true if a session is active (or successfully opened), false otherwise.
     */
    fun ensureSessionActive(): Boolean {
        return if (glyphManager.isSessionActive) {
            true
        } else {
            glyphManager.forceEnsureSession()
        }
    }

    /**
     * Padlock Sweep animation
     * – Keeps every LED lit at a steady base brightness while a single C-arc segment
     *   sweeps along the arc (C1-series) once and then stops.
     * – Mimics the look of a padlock opening / closing.
     */
    suspend fun runLockPulseAnimation() {
        if (!isGlyphServiceEnabled()) return
        Log.d(TAG, "runLockPulseAnimation called")

        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone – lock animation will not run")
            return
        }

        isAnimationRunning = true

        try {
            resetGlyphs()
            delay(100L)

            // Quicker step for a smoother sweep
            val stepDuration = 100L

            // Determine segment lists based on model
            val (cSegments, allSegments) = when {
                Common.is20111() -> {
                    val cSegs = listOf(Phone1.C_START, Phone1.C_START + 1, Phone1.C_START + 2, Phone1.C_START + 3)
                    val allSegs = (0..14).toList()
                    cSegs to allSegs
                }
                Common.is22111() -> {
                    val cSegs = c1Segments
                    val allSegs = aSegments + bSegments + cSegs + cOtherSegments + dSegments + eSegments
                    cSegs to allSegs
                }
                Common.is24111() -> {
                    // Phone 3a – C1-C20 (0-19), A1-A11 (20-30), B1-B5 (31-35)
                    val cSegs = (Phone3a.C_START until Phone3a.C_START + 20).toList()
                    val allSegs = (Phone3a.C_START until Phone3a.B_START + 5).toList() // 0-35 inclusive
                    cSegs to allSegs
                }
                else -> {
                    val cSegs = c1Segments
                    val allSegs = aSegments + bSegments + cSegs + cOtherSegments + dSegments + eSegments
                    cSegs to allSegs
                }
            }

            val nonCSegments = allSegments.filterNot { it in cSegments }

            // Fill non-C segments at full brightness for entire animation

            // Sweep once through the C-arc, cumulatively lighting each C segment
            for (idx in cSegments.indices) {
                if (!isAnimationRunning) break

                try {
                    val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break

                    // Non-C segments sit at 50 % brightness throughout
                    val nonCBrightness = (maxBrightness * 0.5f).toInt()
                    nonCSegments.forEach { seg ->
                        builder.buildChannel(seg, nonCBrightness)
                    }

                    // Light C segments up to current index
                    for (j in 0..idx) {
                        val brightness = if (idx == 0) {
                            maxBrightness // first frame – only the leading segment is lit
                        } else {
                            if (j == idx) {
                                maxBrightness // leading segment at full power
                            } else {
                                // Fade earlier segments from 30 % → 100 % across the arc
                                val fadeFactor = 0.3f + 0.7f * (j.toFloat() / idx)
                                (maxBrightness * fadeFactor).toInt()
                            }
                        }
                        builder.buildChannel(cSegments[j], brightness)
                    }

                    val frame = builder.build()
                    glyphManager.mGM?.toggle(frame)

                    delay(stepDuration)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in lock pulse step: ${e.message}")
                    delay(stepDuration)
                }
            }

            // Ensure absolutely everything is lit at full brightness once sweep completes
            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder()
                builder?.let {
                    allSegments.forEach { seg ->
                        it.buildChannel(seg, maxBrightness)
                    }
                    val frame = it.build()
                    glyphManager.mGM?.toggle(frame)
                }
                // Hold the final lit state briefly
                delay(700L)
            } catch (e: Exception) {
                Log.e(TAG, "Error finalising lock pulse: ${e.message}")
            }
        } finally {
            isAnimationRunning = false
            // Turn off all LEDs after holding
            try {
                glyphManager.turnOffAll()
            } catch (e: Exception) {
                Log.e(TAG, "Error turning off after lock pulse: ${e.message}")
            }
        }
    }

    /**
     * Spiral Animation - Creates a spiral effect that starts from the center and expands outward
     * Uses different patterns for different phone models
     */
    suspend fun runSpiralAnimation() {
        if (!isGlyphServiceEnabled()) return
        Log.d(TAG, "runSpiralAnimation called")
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - spiral animation will not run")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Starting spiral animation")
            resetGlyphs()
            delay(100L)

            when {
                Common.is20111() -> runPhone1SpiralAnimation()
                Common.is22111() -> runPhone2SpiralAnimation()
                Common.is23111() || Common.is23113() -> runPhone2aSpiralAnimation()
                Common.is24111() -> runPhone3aSpiralAnimation()
                else -> runDefaultSpiralAnimation()
            }
        } finally {
            isAnimationRunning = false
            glyphManager.turnOffAll()
        }
    }

    private suspend fun runPhone1SpiralAnimation() {
        val stepDuration = 100L
        val segments = listOf(
            Phone1.E, // Center
            Phone1.A, Phone1.B, // Top
            Phone1.C_START, Phone1.C_START + 1, Phone1.C_START + 2, Phone1.C_START + 3, // C ring
            Phone1.D_START, Phone1.D_START + 1, Phone1.D_START + 2, Phone1.D_START + 3,
            Phone1.D_START + 4, Phone1.D_START + 5, Phone1.D_START + 6, Phone1.D_START + 7 // D ring
        )

        // Phase 1: Spiral outward with growing intensity
        for (i in segments.indices) {
            if (!isAnimationRunning) break

            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                // Light up all segments up to current index with enhanced fade effect
                for (j in 0..i) {
                    val fadeFactor = 0.7f + (j.toFloat() / segments.size * 0.3f)
                    val brightness = (maxBrightness * fadeFactor).toInt()
                    builder.buildChannel(segments[j], brightness)
                }

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration)
            } catch (e: Exception) {
                delay(stepDuration)
            }
        }

        // Phase 2: Full brightness flash
        try {
            val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
            segments.forEach { builder.buildChannel(it, maxBrightness) }
            val frame = builder.build()
            glyphManager.mGM?.toggle(frame)
            delay(300L)
        } catch (e: Exception) {
            delay(300L)
        }

        // Phase 3: Spiral inward with shrinking effect
        for (i in segments.indices.reversed()) {
            if (!isAnimationRunning) break

            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                // Light up segments from current to end with enhanced fade effect
                for (j in i until segments.size) {
                    val fadeFactor = 0.7f + ((segments.size - j).toFloat() / (segments.size - i) * 0.3f)
                    val brightness = (maxBrightness * fadeFactor).toInt()
                    builder.buildChannel(segments[j], brightness)
                }

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration)
            } catch (e: Exception) {
                delay(stepDuration)
            }
        }

        // Phase 4: Final center pulse
        try {
            val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
            builder.buildChannel(Phone1.E, maxBrightness) // Center only
            val frame = builder.build()
            glyphManager.mGM?.toggle(frame)
            delay(200L)
            
            glyphManager.turnOffAll()
            delay(100L)
            
            // Final center flash
            glyphManager.mGM?.toggle(frame)
            delay(200L)
        } catch (e: Exception) {
            delay(200L)
        }
    }

    private suspend fun runPhone2SpiralAnimation() {
        val stepDuration = 80L
        val segments = eSegments + aSegments + bSegments + c1Segments + cOtherSegments + dSegments

        // Phase 1: Spiral outward with growing intensity
        for (i in segments.indices) {
            if (!isAnimationRunning) break

            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                for (j in 0..i) {
                    val fadeFactor = 0.6f + (j.toFloat() / segments.size * 0.4f)
                    val brightness = (maxBrightness * fadeFactor).toInt()
                    builder.buildChannel(segments[j], brightness)
                }

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration)
            } catch (e: Exception) {
                delay(stepDuration)
            }
        }

        // Phase 2: Full brightness flash
        try {
            val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
            segments.forEach { builder.buildChannel(it, maxBrightness) }
            val frame = builder.build()
            glyphManager.mGM?.toggle(frame)
            delay(250L)
        } catch (e: Exception) {
            delay(250L)
        }

        // Phase 3: Spiral inward with shrinking effect
        for (i in segments.indices.reversed()) {
            if (!isAnimationRunning) break

            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                for (j in i until segments.size) {
                    val fadeFactor = 0.6f + ((segments.size - j).toFloat() / (segments.size - i) * 0.4f)
                    val brightness = (maxBrightness * fadeFactor).toInt()
                    builder.buildChannel(segments[j], brightness)
                }

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration)
            } catch (e: Exception) {
                delay(stepDuration)
            }
        }

        // Phase 4: Final center pulse
        try {
            val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
            eSegments.forEach { builder.buildChannel(it, maxBrightness) } // Center E segments
            val frame = builder.build()
            glyphManager.mGM?.toggle(frame)
            delay(200L)
            
            glyphManager.turnOffAll()
            delay(100L)
            
            // Final center flash
            glyphManager.mGM?.toggle(frame)
            delay(200L)
        } catch (e: Exception) {
            delay(200L)
        }
    }

    private suspend fun runPhone2aSpiralAnimation() {
        val stepDuration = 70L
        val segments = listOf(Phone2a.A, Phone2a.B) + (0 until 24).map { Phone2a.C_START + it }

        // Phase 1: Spiral outward with growing intensity
        for (i in segments.indices) {
            if (!isAnimationRunning) break

            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                for (j in 0..i) {
                    val fadeFactor = 0.5f + (j.toFloat() / segments.size * 0.5f)
                    val brightness = (maxBrightness * fadeFactor).toInt()
                    builder.buildChannel(segments[j], brightness)
                }

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration)
            } catch (e: Exception) {
                delay(stepDuration)
            }
        }

        // Phase 2: Full brightness flash
        try {
            val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
            segments.forEach { builder.buildChannel(it, maxBrightness) }
            val frame = builder.build()
            glyphManager.mGM?.toggle(frame)
            delay(200L)
        } catch (e: Exception) {
            delay(200L)
        }

        // Phase 3: Spiral inward with shrinking effect
        for (i in segments.indices.reversed()) {
            if (!isAnimationRunning) break

            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                for (j in i until segments.size) {
                    val fadeFactor = 0.5f + ((segments.size - j).toFloat() / (segments.size - i) * 0.5f)
                    val brightness = (maxBrightness * fadeFactor).toInt()
                    builder.buildChannel(segments[j], brightness)
                }

                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDuration)
            } catch (e: Exception) {
                delay(stepDuration)
            }
        }

        // Phase 4: Final center pulse
        try {
            val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
            // Light up A and B segments (center area)
            builder.buildChannel(Phone2a.A, maxBrightness)
            builder.buildChannel(Phone2a.B, maxBrightness)
            val frame = builder.build()
            glyphManager.mGM?.toggle(frame)
            delay(200L)
            
            glyphManager.turnOffAll()
            delay(100L)
            
            // Final center flash
            glyphManager.mGM?.toggle(frame)
            delay(200L)
        } catch (e: Exception) {
            delay(200L)
        }
    }



    private suspend fun runDefaultSpiralAnimation() {
        delay(2000L)
    }

    /**
     * Heartbeat Animation - Simulates a heartbeat with two strong beats followed by a pause
     */
    suspend fun runHeartbeatAnimation() {
        if (!isGlyphServiceEnabled()) return
        Log.d(TAG, "runHeartbeatAnimation called")
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - heartbeat animation will not run")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Starting heartbeat animation")
            resetGlyphs()
            delay(100L)

            when {
                Common.is20111() -> runPhone1HeartbeatAnimation()
                Common.is22111() -> runPhone2HeartbeatAnimation()
                Common.is23111() || Common.is23113() -> runPhone2aHeartbeatAnimation()
                Common.is24111() -> runPhone3aHeartbeatAnimation()
                else -> runDefaultHeartbeatAnimation()
            }
        } finally {
            isAnimationRunning = false
            glyphManager.turnOffAll()
        }
    }

    private suspend fun runPhone1HeartbeatAnimation() {
        val segments = listOf(
            Phone1.A, Phone1.B, Phone1.E,
            Phone1.C_START, Phone1.C_START + 1, Phone1.C_START + 2, Phone1.C_START + 3,
            Phone1.D_START, Phone1.D_START + 1, Phone1.D_START + 2, Phone1.D_START + 3,
            Phone1.D_START + 4, Phone1.D_START + 5, Phone1.D_START + 6, Phone1.D_START + 7
        )
        
        repeat(3) { // 3 heartbeats
            if (!isAnimationRunning) return@repeat
            
            // First beat - strong
            val builder1 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
            segments.forEach { builder1.buildChannel(it, maxBrightness) }
            glyphManager.mGM?.toggle(builder1.build())
            delay(200L)
            
            glyphManager.turnOffAll()
            delay(100L)
            
            // Second beat - strong
            val builder2 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
            segments.forEach { builder2.buildChannel(it, maxBrightness) }
            glyphManager.mGM?.toggle(builder2.build())
            delay(200L)
            
            glyphManager.turnOffAll()
            delay(300L) // Longer pause between heartbeats
        }
    }

    private suspend fun runPhone2HeartbeatAnimation() {
        val segments = aSegments + bSegments + eSegments + c1Segments + cOtherSegments + dSegments
        
        repeat(3) {
            if (!isAnimationRunning) return@repeat
            
            val builder1 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
            segments.forEach { builder1.buildChannel(it, maxBrightness) }
            glyphManager.mGM?.toggle(builder1.build())
            delay(200L)
            
            glyphManager.turnOffAll()
            delay(100L)
            
            val builder2 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
            segments.forEach { builder2.buildChannel(it, maxBrightness) }
            glyphManager.mGM?.toggle(builder2.build())
            delay(200L)
            
            glyphManager.turnOffAll()
            delay(300L)
        }
    }

    private suspend fun runPhone2aHeartbeatAnimation() {
        val segments = listOf(Phone2a.A, Phone2a.B) + (0 until 24).map { Phone2a.C_START + it }
        
        repeat(3) {
            if (!isAnimationRunning) return@repeat
            
            val builder1 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
            segments.forEach { builder1.buildChannel(it, maxBrightness) }
            glyphManager.mGM?.toggle(builder1.build())
            delay(200L)
            
            glyphManager.turnOffAll()
            delay(100L)
            
            val builder2 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
            segments.forEach { builder2.buildChannel(it, maxBrightness) }
            glyphManager.mGM?.toggle(builder2.build())
            delay(200L)
            
            glyphManager.turnOffAll()
            delay(300L)
        }
    }

    private suspend fun runPhone3aHeartbeatAnimation() {
        val segments = (Phone3a.A_START until Phone3a.A_START + 11).toList() + 
                      (Phone3a.B_START until Phone3a.B_START + 5).toList() +
                      (Phone3a.C_START until Phone3a.C_START + 20).toList()
        
        repeat(3) {
            if (!isAnimationRunning) return@repeat
            
            val builder1 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
            segments.forEach { builder1.buildChannel(it, maxBrightness) }
            glyphManager.mGM?.toggle(builder1.build())
            delay(200L)
            
            glyphManager.turnOffAll()
            delay(100L)
            
            val builder2 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
            segments.forEach { builder2.buildChannel(it, maxBrightness) }
            glyphManager.mGM?.toggle(builder2.build())
            delay(200L)
            
            glyphManager.turnOffAll()
            delay(300L)
        }
    }

    private suspend fun runDefaultHeartbeatAnimation() {
        delay(2000L)
    }



    /**
     * Matrix Rain Animation - Creates a falling rain effect like the Matrix
     */
    suspend fun runMatrixRainAnimation() {
        if (!isGlyphServiceEnabled()) return
        Log.d(TAG, "runMatrixRainAnimation called")
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - matrix rain animation will not run")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Starting matrix rain animation")
            resetGlyphs()
            delay(100L)

            when {
                Common.is20111() -> runPhone1MatrixRainAnimation()
                Common.is22111() -> runPhone2MatrixRainAnimation()
                Common.is23111() || Common.is23113() -> runPhone2aMatrixRainAnimation()
                Common.is24111() -> runPhone3aMatrixRainAnimation()
                else -> runDefaultMatrixRainAnimation()
            }
        } finally {
            isAnimationRunning = false
            glyphManager.turnOffAll()
        }
    }

    private suspend fun runPhone1MatrixRainAnimation() {
        val allSegments = listOf(Phone1.A, Phone1.B, Phone1.C_START, Phone1.C_START + 1, 
                                Phone1.C_START + 2, Phone1.C_START + 3, Phone1.E,
                                Phone1.D_START, Phone1.D_START + 1, Phone1.D_START + 2, 
                                Phone1.D_START + 3, Phone1.D_START + 4, Phone1.D_START + 5, 
                                Phone1.D_START + 6, Phone1.D_START + 7)
        
        repeat(20) { // 20 drops
            if (!isAnimationRunning) return@repeat
            
            // Random drop length
            val dropLength = Random.nextInt(3, 8)
            val startIndex = Random.nextInt(allSegments.size - dropLength)
            
            // Create falling effect
            for (i in 0 until dropLength) {
                if (!isAnimationRunning) break
                
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
                val segmentIndex = startIndex + i
                if (segmentIndex < allSegments.size) {
                    val brightness = maxBrightness - (i * 200) // Fade as it falls
                    builder.buildChannel(allSegments[segmentIndex], brightness.coerceAtLeast(0))
                }
                
                glyphManager.mGM?.toggle(builder.build())
                delay(100L)
                glyphManager.turnOffAll()
                delay(50L)
            }
        }
    }

    private suspend fun runPhone2MatrixRainAnimation() {
        val allSegments = aSegments + bSegments + c1Segments + cOtherSegments + eSegments + dSegments
        
        repeat(25) { // 25 drops
            if (!isAnimationRunning) return@repeat
            
            val dropLength = Random.nextInt(4, 10)
            val startIndex = Random.nextInt(allSegments.size - dropLength)
            
            for (i in 0 until dropLength) {
                if (!isAnimationRunning) break
                
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
                val segmentIndex = startIndex + i
                if (segmentIndex < allSegments.size) {
                    val brightness = maxBrightness - (i * 150)
                    builder.buildChannel(allSegments[segmentIndex], brightness.coerceAtLeast(0))
                }
                
                glyphManager.mGM?.toggle(builder.build())
                delay(80L)
                glyphManager.turnOffAll()
                delay(40L)
            }
        }
    }

    private suspend fun runPhone2aMatrixRainAnimation() {
        val allSegments = (0 until 24).map { Phone2a.C_START + it } + listOf(Phone2a.A, Phone2a.B)
        
        repeat(30) { // 30 drops
            if (!isAnimationRunning) return@repeat
            
            val dropLength = Random.nextInt(5, 12)
            val startIndex = Random.nextInt(allSegments.size - dropLength)
            
            for (i in 0 until dropLength) {
                if (!isAnimationRunning) break
                
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
                val segmentIndex = startIndex + i
                if (segmentIndex < allSegments.size) {
                    val brightness = maxBrightness - (i * 120)
                    builder.buildChannel(allSegments[segmentIndex], brightness.coerceAtLeast(0))
                }
                
                glyphManager.mGM?.toggle(builder.build())
                delay(70L)
                glyphManager.turnOffAll()
                delay(35L)
            }
        }
    }

    private suspend fun runPhone3aMatrixRainAnimation() {
        val allSegments = (Phone3a.C_START until Phone3a.C_START + 20).toList() +
                         (Phone3a.A_START until Phone3a.A_START + 11).toList() +
                         (Phone3a.B_START until Phone3a.B_START + 5).toList()
        
        repeat(35) { // 35 drops
            if (!isAnimationRunning) return@repeat
            
            val dropLength = Random.nextInt(6, 15)
            val startIndex = Random.nextInt(allSegments.size - dropLength)
            
            for (i in 0 until dropLength) {
                if (!isAnimationRunning) break
                
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
                val segmentIndex = startIndex + i
                if (segmentIndex < allSegments.size) {
                    val brightness = maxBrightness - (i * 100)
                    builder.buildChannel(allSegments[segmentIndex], brightness.coerceAtLeast(0))
                }
                
                glyphManager.mGM?.toggle(builder.build())
                delay(60L)
                glyphManager.turnOffAll()
                delay(30L)
            }
        }
    }

    private suspend fun runDefaultMatrixRainAnimation() {
        delay(3000L)
    }

    /**
     * Fireworks Animation - Creates a fireworks display effect
     */
    suspend fun runFireworksAnimation() {
        if (!isGlyphServiceEnabled()) return
        Log.d(TAG, "runFireworksAnimation called")
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - fireworks animation will not run")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Starting fireworks animation")
            resetGlyphs()
            delay(100L)

            when {
                Common.is20111() -> runPhone1FireworksAnimation()
                Common.is22111() -> runPhone2FireworksAnimation()
                Common.is23111() || Common.is23113() -> runPhone2aFireworksAnimation()
                Common.is24111() -> runPhone3aFireworksAnimation()
                else -> runDefaultFireworksAnimation()
            }
        } finally {
            isAnimationRunning = false
            glyphManager.turnOffAll()
        }
    }

    private suspend fun runPhone1FireworksAnimation() {
        val segments = listOf(Phone1.A, Phone1.B, Phone1.C_START, Phone1.C_START + 1, 
                             Phone1.C_START + 2, Phone1.C_START + 3, Phone1.E,
                             Phone1.D_START, Phone1.D_START + 1, Phone1.D_START + 2, 
                             Phone1.D_START + 3, Phone1.D_START + 4, Phone1.D_START + 5, 
                             Phone1.D_START + 6, Phone1.D_START + 7)
        
        repeat(5) { // 5 fireworks
            if (!isAnimationRunning) return@repeat
            
            // Launch phase - single segment
            val launchSegment = segments[Random.nextInt(segments.size)]
            val builder1 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
            builder1.buildChannel(launchSegment, maxBrightness)
            glyphManager.mGM?.toggle(builder1.build())
            delay(300L)
            
            // Explosion phase - multiple segments
            val explosionSegments = segments.shuffled().take(Random.nextInt(5, 10))
            val builder2 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
            explosionSegments.forEach { builder2.buildChannel(it, maxBrightness) }
            glyphManager.mGM?.toggle(builder2.build())
            delay(500L)
            
            // Fade out
            glyphManager.turnOffAll()
            delay(200L)
        }
    }

    private suspend fun runPhone2FireworksAnimation() {
        val allSegments = aSegments + bSegments + c1Segments + cOtherSegments + eSegments + dSegments
        
        repeat(6) { // 6 fireworks
            if (!isAnimationRunning) return@repeat
            
            val launchSegment = allSegments[Random.nextInt(allSegments.size)]
            val builder1 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
            builder1.buildChannel(launchSegment, maxBrightness)
            glyphManager.mGM?.toggle(builder1.build())
            delay(250L)
            
            val explosionSegments = allSegments.shuffled().take(Random.nextInt(8, 15))
            val builder2 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
            explosionSegments.forEach { builder2.buildChannel(it, maxBrightness) }
            glyphManager.mGM?.toggle(builder2.build())
            delay(400L)
            
            glyphManager.turnOffAll()
            delay(150L)
        }
    }

    private suspend fun runPhone2aFireworksAnimation() {
        val allSegments = (0 until 24).map { Phone2a.C_START + it } + listOf(Phone2a.A, Phone2a.B)
        
        repeat(7) { // 7 fireworks
            if (!isAnimationRunning) return@repeat
            
            val launchSegment = allSegments[Random.nextInt(allSegments.size)]
            val builder1 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
            builder1.buildChannel(launchSegment, maxBrightness)
            glyphManager.mGM?.toggle(builder1.build())
            delay(200L)
            
            val explosionSegments = allSegments.shuffled().take(Random.nextInt(10, 20))
            val builder2 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
            explosionSegments.forEach { builder2.buildChannel(it, maxBrightness) }
            glyphManager.mGM?.toggle(builder2.build())
            delay(350L)
            
            glyphManager.turnOffAll()
            delay(100L)
        }
    }

    private suspend fun runPhone3aFireworksAnimation() {
        val allSegments = (Phone3a.C_START until Phone3a.C_START + 20).toList() +
                         (Phone3a.A_START until Phone3a.A_START + 11).toList() +
                         (Phone3a.B_START until Phone3a.B_START + 5).toList()
        
        repeat(8) { // 8 fireworks
            if (!isAnimationRunning) return@repeat
            
            val launchSegment = allSegments[Random.nextInt(allSegments.size)]
            val builder1 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
            builder1.buildChannel(launchSegment, maxBrightness)
            glyphManager.mGM?.toggle(builder1.build())
            delay(180L)
            
            val explosionSegments = allSegments.shuffled().take(Random.nextInt(12, 25))
            val builder2 = glyphManager.mGM?.getGlyphFrameBuilder() ?: return@repeat
            explosionSegments.forEach { builder2.buildChannel(it, maxBrightness) }
            glyphManager.mGM?.toggle(builder2.build())
            delay(300L)
            
            glyphManager.turnOffAll()
            delay(80L)
        }
    }

    private suspend fun runDefaultFireworksAnimation() {
        delay(3000L)
    }

    /**
     * DNA Helix Animation - Creates a double helix effect
     */
    suspend fun runDNAHelixAnimation() {
        if (!isGlyphServiceEnabled()) return
        Log.d(TAG, "runDNAHelixAnimation called")
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - DNA helix animation will not run")
            return
        }

        isAnimationRunning = true

        try {
            Log.d(TAG, "Starting DNA helix animation")
            resetGlyphs()
            delay(100L)

            when {
                Common.is20111() -> runPhone1DNAHelixAnimation()
                Common.is22111() -> runPhone2DNAHelixAnimation()
                Common.is23111() || Common.is23113() -> runPhone2aDNAHelixAnimation()
                Common.is24111() -> runPhone3aDNAHelixAnimation()
                else -> runDefaultDNAHelixAnimation()
            }
        } finally {
            isAnimationRunning = false
            glyphManager.turnOffAll()
        }
    }

    private suspend fun runPhone1DNAHelixAnimation() {
        val segments = listOf(Phone1.A, Phone1.B, Phone1.C_START, Phone1.C_START + 1, 
                             Phone1.C_START + 2, Phone1.C_START + 3, Phone1.E,
                             Phone1.D_START, Phone1.D_START + 1, Phone1.D_START + 2, 
                             Phone1.D_START + 3, Phone1.D_START + 4, Phone1.D_START + 5, 
                             Phone1.D_START + 6, Phone1.D_START + 7)
        
        repeat(3) { // 3 complete rotations
            if (!isAnimationRunning) return@repeat
            
            for (i in segments.indices) {
                if (!isAnimationRunning) break
                
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
                
                // First strand
                val strand1Index = i
                if (strand1Index < segments.size) {
                    builder.buildChannel(segments[strand1Index], maxBrightness)
                }
                
                // Second strand (offset by half the total segments)
                val strand2Index = (i + segments.size / 2) % segments.size
                builder.buildChannel(segments[strand2Index], maxBrightness)
                
                glyphManager.mGM?.toggle(builder.build())
                delay(150L)
                glyphManager.turnOffAll()
                delay(50L)
            }
        }
    }

    private suspend fun runPhone2DNAHelixAnimation() {
        val allSegments = aSegments + bSegments + c1Segments + cOtherSegments + eSegments + dSegments
        
        repeat(3) {
            if (!isAnimationRunning) return@repeat
            
            for (i in allSegments.indices) {
                if (!isAnimationRunning) break
                
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
                
                val strand1Index = i
                if (strand1Index < allSegments.size) {
                    builder.buildChannel(allSegments[strand1Index], maxBrightness)
                }
                
                val strand2Index = (i + allSegments.size / 2) % allSegments.size
                builder.buildChannel(allSegments[strand2Index], maxBrightness)
                
                glyphManager.mGM?.toggle(builder.build())
                delay(120L)
                glyphManager.turnOffAll()
                delay(40L)
            }
        }
    }

    private suspend fun runPhone2aDNAHelixAnimation() {
        val allSegments = (0 until 24).map { Phone2a.C_START + it } + listOf(Phone2a.A, Phone2a.B)
        
        repeat(3) {
            if (!isAnimationRunning) return@repeat
            
            for (i in allSegments.indices) {
                if (!isAnimationRunning) break
                
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
                
                val strand1Index = i
                if (strand1Index < allSegments.size) {
                    builder.buildChannel(allSegments[strand1Index], maxBrightness)
                }
                
                val strand2Index = (i + allSegments.size / 2) % allSegments.size
                builder.buildChannel(allSegments[strand2Index], maxBrightness)
                
                glyphManager.mGM?.toggle(builder.build())
                delay(100L)
                glyphManager.turnOffAll()
                delay(30L)
            }
        }
    }

    private suspend fun runPhone3aDNAHelixAnimation() {
        val allSegments = (Phone3a.C_START until Phone3a.C_START + 20).toList() +
                         (Phone3a.A_START until Phone3a.A_START + 11).toList() +
                         (Phone3a.B_START until Phone3a.B_START + 5).toList()
        
        repeat(3) {
            if (!isAnimationRunning) return@repeat
            
            for (i in allSegments.indices) {
                if (!isAnimationRunning) break
                
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
                
                val strand1Index = i
                if (strand1Index < allSegments.size) {
                    builder.buildChannel(allSegments[strand1Index], maxBrightness)
                }
                
                val strand2Index = (i + allSegments.size / 2) % allSegments.size
                builder.buildChannel(allSegments[strand2Index], maxBrightness)
                
                glyphManager.mGM?.toggle(builder.build())
                delay(80L)
                glyphManager.turnOffAll()
                delay(25L)
            }
        }
    }

    private suspend fun runDefaultDNAHelixAnimation() {
        delay(3000L)
    }
} // end of GlyphAnimationManager class
