package com.bleelblep.glyphsharge.glyph

import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.nothing.ketchum.Common
import com.nothing.ketchum.Glyph
import com.nothing.ketchum.GlyphException
import com.nothing.ketchum.GlyphFrame
import com.bleelblep.glyphsharge.utils.LoggingManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicReference

// Placeholder until official GlyphAnimation class is available in the SDK
private typealias GlyphAnimation = Any

/**
 * Manager class for interacting with the Nothing Glyph Interface.
 * This class wraps the Nothing Glyph SDK functionality and provides
 * a simpler interface for the Glyph Sharge app.
 * 
 * Following official Nothing Glyph Developer Kit documentation:
 * https://github.com/Nothing-Developer-Programme/Glyph-Developer-Kit
 */
@Singleton
class GlyphManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "GlyphManager"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Make mGM accessible to GlyphAnimationManager
    var mGM: com.nothing.ketchum.GlyphManager? = null
        private set

    private var isInitialized = false
    private var _isSessionActive = false
    private var _isServiceConnected = false
    private var shouldAutoReconnect = true

    // Use atomic references for thread safety
    private val _sessionState = AtomicReference<Boolean>(false)
    val sessionState: Boolean get() = _sessionState.get()

    // Public properties for external access
    val isSessionActive: Boolean get() = _isSessionActive
    val isServiceConnected: Boolean get() = _isServiceConnected

    // Callback for session state changes
    var onSessionStateChanged: ((Boolean) -> Unit)? = null

    // Track active animations for proper cleanup
    private val activeAnimations = mutableSetOf<Job>()

    // Channel mappings for different phone models
    object Phone1 {
        const val A1 = 0
        const val B1 = 1
        const val C1 = 2  // Through C4 = 5
        const val E1 = 6
        const val D1_1 = 7  // Through D1_8 = 14
    }

    object Phone2 {
        const val A1 = 0
        const val A2 = 1
        const val B1 = 2
        const val C1_1 = 3  // Through C1_16 = 18
        const val C2 = 19   // Through C6 = 23
        const val E1 = 24
        const val D1_1 = 25 // Through D1_8 = 32
    }

    object Phone2a {
        const val A = 25
        const val B = 24
        const val C1 = 0    // Through C24 = 23
    }

    object Phone3a {
        const val A1 = 20   // Through A11 = 30
        const val B1 = 31   // Through B5 = 35
        const val C1 = 0    // Through C20 = 19
    }

    // Remove dependency on GlyphSession since openSession returns Unit in current SDK
    private val registeredAnimations = mutableSetOf<GlyphAnimation>()

    // Initialize callback in init block
    private val mCallback: com.nothing.ketchum.GlyphManager.Callback by lazy {
        object : com.nothing.ketchum.GlyphManager.Callback {
        override fun onServiceConnected(componentName: ComponentName) {
            Log.d(TAG, "Glyph Service Connected")
            LoggingManager.logSessionState("SERVICE_CONNECTED", "Component: ${componentName.className}")
                _isServiceConnected = true
                
                try {
                    // Initialize GlyphManager first
                    mGM?.init(mCallback)
                    
                    // Register based on device type
                    val deviceType: String = when {
                        Common.is20111() -> Glyph.DEVICE_20111
                        Common.is22111() -> Glyph.DEVICE_22111
                        Common.is23111() -> Glyph.DEVICE_23111
                        Common.is23113() -> Glyph.DEVICE_23113
                        Common.is24111() -> Glyph.DEVICE_24111
                        else -> throw GlyphException("Unsupported device type")
                    }
                    
                    // Register device
                    mGM?.register(deviceType)
                    Log.d(TAG, "Registered device type: $deviceType")
                    LoggingManager.logSDKOperation("DEVICE_REGISTRATION", "Successfully registered $deviceType")
                    
                    // Do NOT open a session automatically. A session will be opened only when the
                    // user explicitly enables the Glyph Service via the toggle, which calls
                    // GlyphManager.openSession() through toggleGlyphService().
                    
                } catch (e: GlyphException) {
                    Log.e(TAG, "Failed to initialize session: ${e.message}")
                    handleError(e)
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.d(TAG, "Glyph Service Disconnected")
            LoggingManager.logSessionState("SERVICE_DISCONNECTED", "Component: ${componentName.className}")
                _isServiceConnected = false
                cleanup()
            }
        }
    }

    /**
     * Initialize the Glyph Manager
     */
    fun initialize() {
        if (isInitialized) return

            try {
                mGM = com.nothing.ketchum.GlyphManager.getInstance(context)

            // Bind to the system Glyph service immediately. This triggers
            // onServiceConnected -> register(deviceType) -> openSession(),
            // as required by the GDK. Without this call the service never
            // connects and the SDK logs "Non registed" for each frame.
            mGM?.init(mCallback)
                isInitialized = true
            Log.d(TAG, "Glyph Manager initialized and service binding started")
            } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Glyph Manager: ${e.message}")
            handleError(e)
        }
    }

    /**
     * Clean up resources and close session
     */
    fun cleanup() {
        try {
            // Clear locally tracked animations (SDK handles its own cleanup)
            registeredAnimations.clear()
            
            // Close session
            _isSessionActive = false
            isInitialized = false
            
            Log.d(TAG, "GlyphManager cleaned up successfully")
            } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }

    /**
     * Check if operations can be performed
     */
    fun canPerformOperation(): Boolean {
        if (!isSessionActive) {
            Log.w(TAG, "Cannot perform operation - session not active")
            return false
        }
        return true
    }

    /**
     * Turn off all glyphs
     */
    fun turnOff() {
        try {
            mGM?.turnOff()
            } catch (e: Exception) {
            Log.e(TAG, "Error turning off glyphs: ${e.message}")
            handleError(e)
        }
    }

    /**
     * Handle errors and attempt recovery
     */
    private fun handleError(error: Exception) {
        Log.e(TAG, "Glyph error: ${error.message}")
        when (error) {
            is GlyphException -> {
                when (error.message) {
                    "Session not active" -> {
                        Log.d(TAG, "Attempting to recover from inactive session")
                        attemptReconnection()
                    }
                    "Service not connected" -> {
                        Log.d(TAG, "Attempting to recover from disconnected service")
                        attemptReconnection()
                    }
                    else -> {
                        Log.e(TAG, "Unrecoverable Glyph error: ${error.message}")
                        cleanup()
                    }
                }
            }
            else -> {
                Log.e(TAG, "Unexpected error: ${error.message}")
                cleanup()
            }
        }
    }

    /**
     * Attempt to reconnect to the service
     */
    private fun attemptReconnection() {
        if (!shouldAutoReconnect) return

        scope.launch {
            try {
                cleanup()
                delay(1000) // Wait before reconnecting
                initialize()
                Log.d(TAG, "Reconnection attempt completed")
            } catch (e: Exception) {
                Log.e(TAG, "Reconnection failed: ${e.message}")
            }
        }
    }

    /**
     * Cancel all active animations
     */
    fun cancelAllAnimations() {
        activeAnimations.forEach { it.cancel() }
        activeAnimations.clear()
    }

    /**
     * Check if there are any active animations
     */
    fun hasActiveAnimations(): Boolean = activeAnimations.isNotEmpty()

    /**
     * Get the current device type
     */
    fun getDeviceType(): String {
        return when {
            Common.is20111() -> Glyph.DEVICE_20111
            Common.is22111() -> Glyph.DEVICE_22111
            Common.is23111() -> Glyph.DEVICE_23111
            Common.is23113() -> Glyph.DEVICE_23113
            Common.is24111() -> Glyph.DEVICE_24111
            else -> "unknown"
        }
    }

    /**
     * Get the appropriate channel mapping for the current device
     */
    fun getChannelMapping(): Any {
        return when {
            Common.is20111() -> Phone1
            Common.is22111() -> Phone2
            Common.is23111() || Common.is23113() -> Phone2a
            Common.is24111() -> Phone3a
            else -> throw IllegalStateException("Unsupported device type")
        }
    }

    /**
     * Check if the device is a Nothing phone
     */
    fun isNothingPhone(): Boolean {
        return try {
            Common.is20111() || Common.is22111() || Common.is23111() || Common.is23113() || Common.is24111()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking device type: ${e.message}")
            false
        }
    }

    /**
     * Toggle the Glyph service state
     * Returns the new state (true = enabled, false = disabled)
     */
    fun toggleGlyphService(): Boolean {
        return if (isSessionActive) {
            closeSession()
            false
        } else {
            openSession()
            true
        }
    }

    /**
     * Force ensure a session is active for bypass scenarios
     * Returns true if session is now active, false otherwise
     */
    fun forceEnsureSession(): Boolean {
        if (!_isServiceConnected) {
            Log.d(TAG, "forceEnsureSession: Service not connected, attempting connection")
            try {
                initialize()
            } catch (_: Exception) {}

            // Wait up to 2 s for callback
            var waited = 0
            while (!_isServiceConnected && waited < 2000) {
                Thread.sleep(100)
                waited += 100
            }

            if (!_isServiceConnected) {
                Log.w(TAG, "forceEnsureSession: Service still not connected after wait")
                return false
            }
        }

        if (_isSessionActive) {
            Log.d(TAG, "Session already active")
            return true
        }

        return try {
            openSession()
            Log.d(TAG, "Temporary session opened for bypass operation")
            true
        } catch (e: GlyphException) {
            Log.e(TAG, "Error opening temporary session: ${e.message}")
            false
        }
    }

    /**
     * Open a session with the Glyph service
     */
    fun openSession() {
        try {
            mGM?.openSession()
            _isSessionActive = true
            _sessionState.set(true)
            onSessionStateChanged?.invoke(true)
            Log.d(TAG, "Glyph session opened")
            LoggingManager.logSessionState("SESSION_OPENED", "Successfully opened session")
        } catch (e: GlyphException) {
            Log.e(TAG, "Failed to open session: ${e.message}")
            throw e
        }
    }

    /**
     * Close the current session
     */
    fun closeSession() {
        try {
            mGM?.closeSession()
            _isSessionActive = false
            _sessionState.set(false)
            onSessionStateChanged?.invoke(false)
            Log.d(TAG, "Glyph session closed")
            LoggingManager.logSessionState("SESSION_CLOSED", "Session closed")
        } catch (e: GlyphException) {
            Log.e(TAG, "Failed to close session: ${e.message}")
            throw e
        }
    }

    /**
     * Turn off all glyphs
     */
    fun turnOffAll() {
        try {
            mGM?.turnOff()
            Log.d(TAG, "All glyphs turned off")
        } catch (e: Exception) {
            Log.e(TAG, "Error turning off all glyphs: ${e.message}")
            handleError(e)
        }
    }

    /**
     * Turn on all glyphs at maximum brightness for alert purposes
     */
    fun turnOnAllGlyphs() {
        if (!canPerformOperation()) return

        try {
            val builder = mGM?.getGlyphFrameBuilder() ?: return
            
            when {
                Common.is20111() -> {
                    // Phone 1: A, B, C1-C4, E, D1_1-D1_8
                    builder.buildChannel(0, 4000) // A
                    builder.buildChannel(1, 4000) // B
                    for (i in 2..5) builder.buildChannel(i, 4000) // C1-C4
                    builder.buildChannel(6, 4000) // E
                    for (i in 7..14) builder.buildChannel(i, 4000) // D1_1-D1_8
                }
                Common.is22111() -> {
                    // Phone 2: All segments (0-32)
                    for (i in 0..32) builder.buildChannel(i, 4000)
                }
                Common.is23111() || Common.is23113() -> {
                    // Phone 2a: C1-C24, B, A (0-25)
                    for (i in 0..25) builder.buildChannel(i, 4000)
                }
                Common.is24111() -> {
                    // Phone 3a: C1-C20, A1-A11, B1-B5 (0-19, 20-30, 31-35)
                    for (i in 0..19) builder.buildChannel(i, 4000) // C1-C20
                    for (i in 20..30) builder.buildChannel(i, 4000) // A1-A11
                    for (i in 31..35) builder.buildChannel(i, 4000) // B1-B5
                }
            }
            
            val frame = builder.build()
            mGM?.toggle(frame)
            Log.d(TAG, "All glyphs turned on")
        } catch (e: Exception) {
            Log.e(TAG, "Error turning on all glyphs: ${e.message}")
            handleError(e)
        }
    }

    /**
     * Enable or disable automatic service reconnection
     */
    fun setAutoReconnect(enabled: Boolean) {
        shouldAutoReconnect = enabled
        Log.d(TAG, "Auto reconnect ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Force a service reconnection
     */
    fun forceReconnect() {
        scope.launch {
            try {
                cleanup()
                delay(1000) // Wait before reconnecting
                initialize()
                Log.d(TAG, "Forced reconnection completed")
            } catch (e: Exception) {
                Log.e(TAG, "Forced reconnection failed: ${e.message}")
            }
        }
    }

    fun registerAnimation(animation: GlyphAnimation) {
        if (!isSessionActive) {
            Log.w(TAG, "Cannot register animation: Session not active")
            return
        }
        
        try {
            // TODO: Integrate with SDK's animation registration when available
            registeredAnimations.add(animation)
            Log.d(TAG, "Animation registered successfully: ${animation.javaClass.simpleName}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register animation: ${animation.javaClass.simpleName}", e)
        }
    }

    fun unregisterAnimation(animation: GlyphAnimation) {
        try {
            // TODO: Integrate with SDK's animation unregistration when available
            registeredAnimations.remove(animation)
            Log.d(TAG, "Animation unregistered successfully: ${animation.javaClass.simpleName}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister animation: ${animation.javaClass.simpleName}", e)
        }
    }

    fun isAnimationRegistered(animation: GlyphAnimation): Boolean {
        return registeredAnimations.contains(animation)
    }
} 