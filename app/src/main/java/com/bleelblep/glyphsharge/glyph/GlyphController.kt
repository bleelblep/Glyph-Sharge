package com.bleelblep.glyphsharge.glyph

import android.content.Context
import android.util.Log
import com.nothing.ketchum.Common
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphException
import com.nothing.ketchum.GlyphFrame
import com.nothing.ketchum.GlyphManager as NothingGlyphManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Controller class for the Nothing Glyph Interface.
 * Provides an alternative interface focused on direct channel control.
 */
@Singleton
class GlyphController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val glyphManager: GlyphManager
) {
    private val TAG = "GlyphController"

    private var isInitialized = false
    private var animationRunning = false
    private var lastPlayedFrame: GlyphFrame? = null

    // Segment mappings for different Nothing Phone models

    // Phone (1) segments
    object Phone1 {
        const val A = 0
        const val B = 1
        const val C_START = 2 // C1-C4 are 2-5
        const val E = 6
        const val D_START = 7 // D1_1-D1_8 are 7-14
    }

    // Phone (2) segments
    object Phone2 {
        // A segments
        const val A1 = 0
        const val A2 = 1

        // B segment
        const val B = 2

        // C1 segments (3-18)
        const val C1_1 = 3
        // ... through C1_16 = 18

        // C2-C6 segments (19-23)
        const val C2 = 19
        const val C3 = 20
        const val C4 = 21
        const val C5 = 22
        const val C6 = 23

        // E segment
        const val E = 24

        // D segments (25-32)
        const val D1_1 = 25
        // ... through D1_8 = 32
    }

    // Phone (2a/2a+) segments
    object Phone2a {
        // C1-C24 segments (0-23)
        const val C_START = 0

        // B segment
        const val B = 24

        // A segment
        const val A = 25
    }

    // Phone (3a/3a Pro) segments
    object Phone3a {
        // B1-B5 segments (0-4)
        const val B_START = 0

        // A1-A11 segments (20-30)
        const val A_START = 20
    }

    /**
     * Initialize the controller
     */
    fun initialize() {
        isInitialized = true
        Log.d(TAG, "Glyph Controller initialized (using existing GlyphManager)")
        
        // Enable auto-reconnection
        glyphManager.setAutoReconnect(true)
    }

    /**
     * Play a glyph frame animation
     */
    fun play(frame: GlyphFrame) {
        try {
            // Store the last played frame for recovery
            lastPlayedFrame = frame

            // Always turn off previous frames first to avoid lingering LEDs
            turnOffAll()

            // Then play the new frame
            if (glyphManager.isNothingPhone()) {
                if (!glyphManager.isSessionActive) {
                    // If session is not active, try to reconnect and restore
                    glyphManager.forceReconnect()
                    // Wait a bit for reconnection
                    Thread.sleep(100)
                }
                glyphManager.mGM?.toggle(frame)
            } else {
                Log.d(TAG, "Not a Nothing Phone, ignoring play() call")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing Glyph frame: ${e.message}")
            // Attempt recovery
            attemptRecovery()
        }
    }

    /**
     * Attempt to recover from a failed glyph operation
     */
    private fun attemptRecovery() {
        try {
            if (!glyphManager.isSessionActive) {
                glyphManager.forceReconnect()
                // If we have a last played frame, try to replay it
                lastPlayedFrame?.let { frame ->
                    Thread.sleep(100) // Wait for reconnection
                    play(frame)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during recovery attempt: ${e.message}")
        }
    }

    /**
     * Create a frame builder
     */
    fun createFrameBuilder(): GlyphFrame.Builder? {
        return try {
            if (glyphManager.isNothingPhone()) {
                glyphManager.mGM?.getGlyphFrameBuilder()
            } else {
                Log.d(TAG, "Not a Nothing Phone, returning null builder")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating frame builder: ${e.message}")
            null
        }
    }

    /**
     * Clean up resources when done
     */
    fun cleanup() {
        try {
            isInitialized = false
            lastPlayedFrame = null
            Log.d(TAG, "Glyph Controller cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up Glyph resources: ${e.message}")
        }
    }

    /**
     * Turn off all Glyph lights
     */
    fun turnOffAll() {
        try {
            if (glyphManager.isNothingPhone()) {
                glyphManager.turnOffAll()
            } else {
                Log.d(TAG, "Not a Nothing Phone, ignoring turnOffAll() call")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error turning off Glyph: ${e.message}")
        }
    }

    /**
     * Check if this is running on a Nothing phone
     */
    fun isNothingPhone(): Boolean {
        try {
            return glyphManager.isNothingPhone()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if device is a Nothing Phone: ${e.message}")
            return false
        }
    }

    /**
     * Get the current phone model
     */
    fun getPhoneModel(): String {
        return try {
            when {
                Common.is20111() -> "Phone (1)"
                Common.is22111() -> "Phone (2)"
                Common.is23111() -> "Phone (2a)"
                Common.is23113() -> "Phone (2a+)"
                Common.is24111() -> "Phone (3a/3a Pro)"
                else -> "Unknown"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting phone model: ${e.message}")
            "Unknown"
        }
    }

    companion object {
        @Volatile
        private var instance: GlyphController? = null

        /**
         * Get singleton instance - Note: with Hilt DI, this is used only for non-injected contexts
         */
        fun getInstance(context: Context): GlyphController {
            return instance ?: synchronized(this) {
                // We need a GlyphManager, but here we're handling the case where
                // we don't have DI, so create one manually
                val glyphManager = GlyphManager(context)
                instance ?: GlyphController(context, glyphManager).also { instance = it }
            }
        }
    }

    /**
     * Builder that allows setting brightness for channels
     */
    class Builder {
        private val builder = GlyphFrame.Builder()

        /**
         * Build a channel with specific brightness
         * @param channel The channel index
         * @param brightness The brightness level (0-4000)
         */
        fun buildChannel(channel: Int, brightness: Int = 4000): Builder {
            builder.buildChannel(channel, brightness)
            return this
        }

        /**
         * Build the frame
         */
        fun build(): GlyphFrame {
            return builder.build()
        }
    }

    private fun checkGlyphService() {
        if (!glyphManager.isNothingPhone()) {
            Log.w(TAG, "Not a Nothing phone, disabling Glyph service")
            setGlyphServiceEnabled(false)
            return
        }

        if (!glyphManager.isSessionActive) {
            Log.w(TAG, "Glyph session not active, attempting to reconnect")
            glyphManager.forceReconnect()
            return
        }

        Log.d(TAG, "Glyph service check passed")
    }

    /**
     * Enable or disable the Glyph service
     * @param enabled Whether to enable or disable the service
     */
    private fun setGlyphServiceEnabled(enabled: Boolean) {
        try {
            if (enabled) {
                glyphManager.initialize()
                glyphManager.openSession()
            } else {
                glyphManager.cleanup()
            }
            Log.d(TAG, "Glyph service ${if (enabled) "enabled" else "disabled"}")
        } catch (e: Exception) {
            Log.e(TAG, "Error ${if (enabled) "enabling" else "disabling"} Glyph service", e)
        }
    }

    private fun handleGlyphServiceStateChange(enabled: Boolean) {
        if (enabled) {
            if (!glyphManager.isSessionActive) {
                Log.w(TAG, "Glyph service enabled but session not active, attempting to reconnect")
                glyphManager.forceReconnect()
            }
        } else {
            // Service disabled, no action needed
        }
    }
} 