package com.bleelblep.glyphsharge

// Android imports
import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

// Compose imports
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.*
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll

// Project imports
import com.bleelblep.glyphsharge.glyph.*
import com.bleelblep.glyphsharge.services.*
import com.bleelblep.glyphsharge.ui.components.*
import com.bleelblep.glyphsharge.ui.screens.*
import com.bleelblep.glyphsharge.ui.theme.*
import com.bleelblep.glyphsharge.utils.WatermarkHelper
import com.nothing.ketchum.Common
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

// Constants for animation timing and configuration
private object AnimationConfig {
    const val DEMO_DELAY = 100L
    const val ANIMATION_DURATION = 1000L
    const val PULSE_DURATION = 500L
    const val BREATHING_DURATION = 2000L
    const val BOX_BREATHING_DURATION = 4000L
    const val NOTIFICATION_DURATION = 1000L
    const val WAVE_DURATION = 1000L
    const val WAVE_DELAY = 100L
    const val WAVE_STEPS = 10
    const val PULSE_STEPS = 5
    const val BREATHING_STEPS = 10
    const val BOX_BREATHING_STEPS = 4
    const val NOTIFICATION_STEPS = 3
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var fontState: FontState

    @Inject
    lateinit var themeState: ThemeState

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var glyphManager: GlyphManager

    @Inject
    lateinit var glyphAnimationManager: GlyphAnimationManager

    // Track current animation job to allow cancellation
    private var animJob: Job? = null

    // Track current animation index for glyph demo
    private var currentDemoAnimationIndex = 0

    // Flag to track if demo is running
    private var isGlyphDemoRunning = false

    // State for glyph service
    private var _glyphServiceState = mutableStateOf(false)
    val glyphServiceState: State<Boolean> = _glyphServiceState

    // Track if service was enabled before going to background
    private var wasServiceEnabled = false

    // Job tracking automatic session restoration
    private var restoreSessionJob: Job? = null

    // Diagnostics: pending log text and launcher to create document
    private var pendingLogText: String? = null
    private lateinit var createLogFileLauncher: androidx.activity.result.ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Debug: Dump all settings on startup
        Log.d("MainActivity", "=== App startup - checking settings persistence ===")
        settingsRepository.dumpAllSettings()
        
        // Request notification permission required for foreground services (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val perm = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(perm), 1001)
            }
        }
        
        // Register SAF create document launcher for diagnostics logs
        createLogFileLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
            uri?.let { writeLogToUri(it) }
        }
        
        configureWindow()
        initializeGlyphService()
        initializePowerPeek()
        initializeGlyphGuard()
        initializeLowBatteryAlert()
        initializeWatermark()
        initializePulseLock()
        initializeQuietHours()
        setupUI()
        startPersistentGlyphService()
    }
    
    private fun initializeWatermark() {
        // Configure watermark for debug/preview builds
        WatermarkHelper.configure {
            text = "PREVIEW"
            rotationAngle = 315f
            spacing = 35
            alpha = 0.2f
            textSize = 18f
            useThemeBasedColor = true // Automatically adapt to light/dark theme
        }
        
        // Enable watermark for debug builds (uncomment to enable)
         WatermarkHelper.disable()
    }

    private fun configureWindow() {
        // Enable edge-to-edge display
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Configure window transparency
        configureWindowTransparency()

        // Configure window performance
        configureWindowPerformance()

        // Configure system UI appearance
        configureSystemUI()
    }

    private fun configureWindowTransparency() {
        window.apply {
            statusBarColor = android.graphics.Color.TRANSPARENT
            navigationBarColor = android.graphics.Color.TRANSPARENT

            try {
                navigationBarColor = 0x00000000 // Fully transparent
            } catch (e: Exception) {
                navigationBarColor = android.graphics.Color.TRANSPARENT
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                isNavigationBarContrastEnforced = false
            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(false)
            }
        }
    }

    private fun configureWindowPerformance() {
        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                attributes.preferredDisplayModeId = 0 // Use highest available refresh rate
            }
        }
    }

    private fun configureSystemUI() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }

    private fun initializeGlyphService() {
        glyphManager.initialize()

        glyphManager.onSessionStateChanged = { isActive ->
            _glyphServiceState.value = isActive
            // Do NOT persist glyph service state automatically when a session opens; only user actions
            // via toggleGlyphService should change the persisted preference. This prevents the service
            // from being auto-enabled after a fresh install.
            // If GlyphGuard was enabled and session just became active, ensure service is running
            if (isActive && settingsRepository.isGlyphGuardEnabled()) {
                val guardIntent = Intent(this, GlyphGuardService::class.java).apply {
                    action = GlyphGuardService.ACTION_START_GLYPH_GUARD
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(guardIntent)
                } else {
                    startService(guardIntent)
                }
            }

            // If session dropped unexpectedly, try to restore it
            if (!isActive) {
                maybeRestoreSession()
            }
        }

        val savedGlyphServiceState = settingsRepository.getGlyphServiceEnabled()

        if (savedGlyphServiceState && glyphManager.isNothingPhone()) {
            lifecycleScope.launch {
                delay(AnimationConfig.DEMO_DELAY)
                if (!glyphManager.isSessionActive) {
                    toggleGlyphService(true)
                } else {
                    _glyphServiceState.value = true
                }
            }
        } else {
            _glyphServiceState.value = glyphManager.isSessionActive
        }
    }

    /**
     * Ensure PowerPeek service is running (or stopped) based on persisted preference.
     */
    private fun initializePowerPeek() {
        val enabled = settingsRepository.isPowerPeekEnabled()
        Log.d("MainActivity", "initializePowerPeek(): persisted=$enabled")
        val serviceIntent = Intent(this, PowerPeekService::class.java)
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } else {
            stopService(serviceIntent)
        }
    }

    /**
     * Ensure Glyph Guard service state matches persisted preference.
     */
    private fun initializeGlyphGuard() {
        val enabled = settingsRepository.isGlyphGuardEnabled()
        Log.d("MainActivity", "initializeGlyphGuard(): persisted=$enabled")
        if (!enabled) return

        // Only start if Glyph service is active (required for visuals)
        if (!glyphManager.isSessionActive) {
            Log.d("MainActivity", "Glyph Guard persisted on but glyph session inactive; deferring start")
            return
        }

        val serviceIntent = Intent(this, GlyphGuardService::class.java).apply {
            action = GlyphGuardService.ACTION_START_GLYPH_GUARD
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    /**
     * Ensure Low Battery Alert service state matches persisted preference.
     */
    private fun initializeLowBatteryAlert() {
        val enabled = settingsRepository.isLowBatteryEnabled()
        Log.d("MainActivity", "initializeLowBatteryAlert(): persisted=$enabled")
        val serviceIntent = Intent(this, LowBatteryAlertService::class.java)
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } else {
            stopService(serviceIntent)
        }
    }

    private fun initializePulseLock() {
        val enabled = settingsRepository.isPulseLockEnabled()
        val serviceIntent = Intent(this, com.bleelblep.glyphsharge.services.PulseLockService::class.java).apply {
            action = if (enabled) com.bleelblep.glyphsharge.services.PulseLockService.ACTION_START else com.bleelblep.glyphsharge.services.PulseLockService.ACTION_STOP
        }
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } else {
            stopService(serviceIntent)
        }
    }

    private fun initializeQuietHours() {
        val enabled = settingsRepository.isQuietHoursEnabled()
        Log.d("MainActivity", "initializeQuietHours(): persisted=$enabled")
        val serviceIntent = Intent(this, QuietHoursService::class.java)
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } else {
            stopService(serviceIntent)
        }
    }

    private fun setupUI() {
        setContent {
            GlyphZenTheme(
                themeState = themeState,
                fontState = fontState
            ) {
                val backgroundColorMain = MaterialTheme.colorScheme.background

                WatermarkBox(
                    enabled = false,
                    text = "TESTING",
                    alpha = 0.5f,
                    fontSize = 20.sp
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = backgroundColorMain
                    ) {
                        val onboardingDone = settingsRepository.isOnboardingComplete()

                        if (!onboardingDone) {
                            OnboardingScreen(onFinish = { recreate() })
                        } else {
                        MainScreen(
                            isNothingPhone = glyphManager.isNothingPhone(),
                            glyphServiceEnabled = glyphServiceState.value,
                            onGlyphServiceToggle = ::toggleGlyphService,
                            onLaunchGlyphDemo = { runGlyphDemo() },
                            onTestAllZones = { bypassServiceCheck -> testAllZones(bypassServiceCheck) },
                            onTestCustomPattern = { bypassServiceCheck -> testCustomPattern(bypassServiceCheck) },
                            onRunWaveAnimation = { runWaveAnimation() },
                            onRunPulseEffect = { runPulseEffect() },
                            onTestGlyphGuard = { testGlyphGuard() },
                onStartGlyphGuard = { startGlyphGuard() },
                onStopGlyphGuard = { stopGlyphGuard() },
                            onRunBoxBreathing = { runBoxBreathing() },
                            onTestPowerPeek = { testPowerPeek() },
                            onEnablePowerPeek = { enablePowerPeek() },
                            onDisablePowerPeek = { disablePowerPeek() },
                            onRunNotificationEffect = { runNotificationEffect() },
                            onTestGlyphChannel = { channelIndex -> testGlyphChannel(channelIndex) },
                            onTestC1Segment = { c1Index -> testC1Segment(c1Index) },
                            onTestFinalState = { testFinalStateBeforeTurnoff() },
                            onTestC14C15Isolated = { testC14AndC15Isolated() },
                            onTestPulseLock = { testPulseLock() },
                            onEnablePulseLock = { enablePulseLock() },
                            onDisablePulseLock = { disablePulseLock() },
                            onTestLowBattery = { testLowBatteryAlert() },
                            onRunDiagnostics = { runDiagnostics() },
                            backgroundColorMain = backgroundColorMain,
                            settingsRepository = settingsRepository
                        )
                        }
                    }
                }
            }
        }
    }

    /**
     * Toggle the Glyph service state (enable/disable)
     */
    fun toggleGlyphService(enabled: Boolean) {
        try {
            // If the current session state already matches the requested state, just sync the UI and return
            val currentState = glyphManager.isSessionActive
            if (currentState == enabled) {
                _glyphServiceState.value = currentState
                settingsRepository.saveGlyphServiceEnabled(currentState)
                showToast("Glyph service is already ${if (enabled) "enabled" else "disabled"}")
                return
            }

            // Cancel any running animations if we are about to disable the service
            if (!enabled) {
                cancelRunningAnimations()
                // Persist preference *before* closing session to stop maybeRestoreSession race
                settingsRepository.saveGlyphServiceEnabled(false)
                restoreSessionJob?.cancel()
            }

            // Attempt to toggle the session (this always flips the current state)
            glyphManager.toggleGlyphService()

            // After toggling, verify the real session state rather than trusting the call result
            val newState = glyphManager.isSessionActive
            val success = newState == enabled

            handleGlyphServiceToggleResult(success, newState)

            // Sync dependent services like PulseLock which are gated by Glyph Service
            if (success) {
                if (newState) {
                    // Glyph service just turned ON – (re)start PulseLock if user enabled it
                    if (settingsRepository.isPulseLockEnabled()) {
                        initializePulseLock()
                    }
                } else {
                    // Glyph service turned OFF – ensure PulseLock is stopped
                    val stopIntent = Intent(this, com.bleelblep.glyphsharge.services.PulseLockService::class.java).apply {
                        action = com.bleelblep.glyphsharge.services.PulseLockService.ACTION_STOP
                    }
                    try {
                        startService(stopIntent)
                    } catch (_: Exception) {}
                }

                // Manage persistent foreground service alignment
                val fgIntent = Intent(this, com.bleelblep.glyphsharge.services.GlyphForegroundService::class.java)
                if (newState) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        startForegroundService(fgIntent)
                    } else {
                        startService(fgIntent)
                    }
                } else {
                    stopService(fgIntent)
                }
            }

            // If disabling failed, roll back persisted preference so future restore reflects reality
            if (!enabled && !success) {
                settingsRepository.saveGlyphServiceEnabled(true)
            }
        } catch (e: Exception) {
            handleGlyphServiceError(e)
        }
    }

    private fun cancelRunningAnimations() {
        animJob?.cancel()
        isGlyphDemoRunning = false
        Log.d("MainActivity", "Cancelled all running glyph animations")
    }

    private fun handleGlyphServiceToggleResult(success: Boolean, enabled: Boolean) {
        if (success) {
            _glyphServiceState.value = enabled
            settingsRepository.saveGlyphServiceEnabled(enabled)
            showToast(if (enabled) "Glyph service enabled" else "Glyph service disabled")
        } else {
            _glyphServiceState.value = glyphManager.isSessionActive
            showToast("Failed to ${if (enabled) "enable" else "disable"} glyph service")
        }
    }

    private fun handleGlyphServiceError(e: Exception) {
        Log.e("MainActivity", "Error toggling glyph service", e)
        _glyphServiceState.value = glyphManager.isSessionActive
        showToast("Error: ${e.message}")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Check if glyph operations can be performed
     */
    private fun canPerformGlyphOperation(bypassServiceCheck: Boolean = false): Boolean {
        if (!glyphManager.isNothingPhone()) {
            Toast.makeText(
                this,
                "Glyph animations only work on Nothing phones",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        if (bypassServiceCheck) {
            // Try to ensure session for diagnostics or bypass scenarios
            if (!glyphManager.forceEnsureSession()) {
                Toast.makeText(this, "Unable to open Glyph session", Toast.LENGTH_SHORT).show()
                return false
            }
        } else if (!glyphManager.isSessionActive) {
            Toast.makeText(
                this,
                "Glyph service is not active",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        return true
    }

    /**
     * Run glyph demo animations directly in MainActivity
     */
    fun runGlyphDemo() {
        // Check if operations can be performed
        if (!canPerformGlyphOperation()) return

        // If animation is already running, cancel it
        if (animJob?.isActive == true) {
            animJob?.cancel()
            isGlyphDemoRunning = false
            Toast.makeText(this, "C1 Sequential Animation stopped", Toast.LENGTH_SHORT).show()
            return
        }

        // Otherwise, start a new animation
        isGlyphDemoRunning = true
        Toast.makeText(this, "Starting C1 Sequential Animation", Toast.LENGTH_SHORT).show()

        // Run the demo in a coroutine
        animJob = lifecycleScope.launch {
            try {
                runC1SequentialAnimation()
                // Animation has completed naturally
                isGlyphDemoRunning = false
            } catch (e: Exception) {
                Log.e("MainActivity", "Error running glyph demo: ${e.message}")
                isGlyphDemoRunning = false
            }
        }
    }

    /**
     * Run the C1 Sequential animation just once
     */
    private suspend fun runC1SequentialAnimation() {
        Log.d("MainActivity", "Running C1 Sequential Animation (one cycle)")

        try {
            // Run the C1 Sequential animation for just one cycle
            glyphAnimationManager.runC1SequentialAnimation()
            
            // Wait for the display duration before stopping
            delay(settingsRepository.getDisplayDuration())
            
            // Stop the animation after display duration
            glyphAnimationManager.stopAnimations()

            // Animation completed successfully
            Log.d("MainActivity", "C1 Sequential Animation completed")
            Toast.makeText(
                this@MainActivity,
                "C1 Sequential Animation completed",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in C1 sequential animation: ${e.message}")
            Toast.makeText(
                this@MainActivity,
                "Animation error: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Test all zones one by one, similar to HiddenMenuFragment implementation
     */
    fun testAllZones(bypassServiceCheck: Boolean = false) {
        // Check if operations can be performed
        if (!canPerformGlyphOperation(bypassServiceCheck)) return

        // Cancel any previous animation job
        animJob?.cancel()
        isGlyphDemoRunning = false

        // Show toast notification
        Toast.makeText(this, "Testing all zones...", Toast.LENGTH_SHORT).show()

        // Run the test in a coroutine
        animJob = lifecycleScope.launch {
            try {
                glyphAnimationManager.testAllZones(bypassServiceCheck)
                delay(settingsRepository.getDisplayDuration())
                glyphAnimationManager.stopAnimations()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error testing all zones: ${e.message}")
            }
        }
    }

    /**
     * Test a custom pattern animation, similar to HiddenMenuFragment implementation
     */
    fun testCustomPattern(bypassServiceCheck: Boolean = false) {
        // Check if operations can be performed
        if (!canPerformGlyphOperation(bypassServiceCheck)) return

        // Cancel any previous animation job
        animJob?.cancel()
        isGlyphDemoRunning = false

        // Show toast notification
        Toast.makeText(this, "Running custom pattern...", Toast.LENGTH_SHORT).show()

        // Run the test in a coroutine
        animJob = lifecycleScope.launch {
            try {
                glyphAnimationManager.testCustomPattern(bypassServiceCheck)
                delay(settingsRepository.getDisplayDuration())
                glyphAnimationManager.stopAnimations()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error testing custom pattern: ${e.message}")
            }
        }
    }

    /**
     * Run wave animation
     */
    fun runWaveAnimation() {
        animJob?.cancel()
        animJob = lifecycleScope.launch {
            try {
                if (glyphServiceState.value) {
                    glyphAnimationManager.runWaveAnimation()
                    delay(settingsRepository.getDisplayDuration())
                } else {
                    showServiceDisabledToast()
                }
            } finally {
                if (glyphServiceState.value) {
                    glyphAnimationManager.stopAnimations()
                }
            }
        }
    }

    /**
     * Run pulse effect
     */
    fun runPulseEffect() {
        animJob?.cancel()
        animJob = lifecycleScope.launch {
            try {
                if (glyphServiceState.value) {
                    glyphAnimationManager.runPulseEffect(3)
                    delay(settingsRepository.getDisplayDuration())
                } else {
                    showServiceDisabledToast()
                }
            } finally {
                if (glyphServiceState.value) {
                    glyphAnimationManager.stopAnimations()
                }
            }
        }
    }

    /**
     * Start Glyph Guard service for USB disconnection monitoring
     */
    fun startGlyphGuard() {
        if (!glyphServiceState.value) {
            showServiceDisabledToast()
            return
        }
        
        // Persist state
        settingsRepository.saveGlyphGuardEnabled(true)
        
        // Request battery optimization exemption for reliable background operation
        requestBatteryOptimizationExemption()
        
        val serviceIntent = Intent(this, GlyphGuardService::class.java).apply {
            action = GlyphGuardService.ACTION_START_GLYPH_GUARD
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        
        Toast.makeText(
            this,
            "🛡️ Glyph Guard activated! Your device is now protected against USB theft.",
            Toast.LENGTH_LONG
        ).show()
        
        Log.d("MainActivity", "Glyph Guard service started")
    }

    /**
     * Stop Glyph Guard service
     */
    fun stopGlyphGuard() {
        // First send stop action to the service
        settingsRepository.saveGlyphGuardEnabled(false)

        val serviceIntent = Intent(this, GlyphGuardService::class.java).apply {
            action = GlyphGuardService.ACTION_STOP_GLYPH_GUARD
        }
        startService(serviceIntent)
        
        // Also explicitly stop the service like PowerPeek does
        val stopServiceIntent = Intent(this, GlyphGuardService::class.java)
        stopService(stopServiceIntent)
        
        Toast.makeText(
            this,
            "🛡️ Glyph Guard disabled. USB theft protection is now off.",
            Toast.LENGTH_SHORT
        ).show()

        Log.d("MainActivity", "Glyph Guard service stopped")
    }

    /**
     * Test Glyph Guard functionality - simulates USB disconnect alert
     */
    fun testGlyphGuard(bypassService: Boolean = false) {
        if (!glyphServiceState.value && !bypassService) {
            showServiceDisabledToast()
            return
        }
        
        animJob?.cancel()
        animJob = lifecycleScope.launch {
            try {
                // Log session status for debugging
                Log.d("MainActivity", "🔍 Testing Glyph Guard - Session active: ${glyphManager.isSessionActive}, Service connected: ${glyphManager.isServiceConnected}")
                
                // Simulate rapid blinking for 5 seconds
                val testDuration = 5000L
                val blinkInterval = 200L
                val startTime = System.currentTimeMillis()
                
                while ((System.currentTimeMillis() - startTime) < testDuration) {
                    Log.v("MainActivity", "🔄 Turning on glyphs...")
                    glyphAnimationManager.turnOnAllGlyphs()
                    delay(blinkInterval)
                    Log.v("MainActivity", "🔄 Turning off glyphs...")
                    glyphAnimationManager.turnOffAll()
                    delay(blinkInterval)
                }
                
            } catch (e: Exception) {
                Log.e("MainActivity", "Error testing Glyph Guard: ${e.message}")
            } finally {
                glyphAnimationManager.stopAnimations()
                glyphAnimationManager.turnOffAll()
            }
        }
    }

    /**
     * Run box breathing animation
     */
    fun runBoxBreathing() {
        animJob?.cancel()
        animJob = lifecycleScope.launch {
            try {
                if (glyphServiceState.value) {
                    glyphAnimationManager.runC1SequentialWithBreathingTiming(true, 2)
                    delay(settingsRepository.getDisplayDuration())
                } else {
                    showServiceDisabledToast()
                }
            } finally {
                if (glyphServiceState.value) {
                    glyphAnimationManager.stopAnimations()
                }
            }
        }
    }

    /**
     * Test PowerPeek functionality - shows battery percentage immediately
     */
    fun testPowerPeek(bypassService: Boolean = false) {
        // Check if operations can be performed
        if (!canPerformGlyphOperation(bypassService)) return

        // Cancel any previous animation job
        animJob?.cancel()
        isGlyphDemoRunning = false

        // Show toast notification
        Toast.makeText(this, "Testing PowerPeek - showing battery percentage...", Toast.LENGTH_SHORT).show()

        // Run the test in a coroutine
        animJob = lifecycleScope.launch {
            try {
                // Run battery percentage visualization using user's display duration setting
                val duration = settingsRepository.getDisplayDuration()
                glyphAnimationManager.runBatteryPercentageVisualization(this@MainActivity, duration) { progress ->
                    // Update progress if needed
                }
                Toast.makeText(
                    this@MainActivity,
                    "PowerPeek test completed",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error testing PowerPeek: ${e.message}")
                Toast.makeText(
                    this@MainActivity,
                    "Error testing PowerPeek: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Enable PowerPeek functionality with shake detection
     */
    fun enablePowerPeek() {
        Log.d("MainActivity", "enablePowerPeek() called")
        
        // Save to settings first
        settingsRepository.savePowerPeekEnabled(true)
        
        // Verify the save worked immediately
        val verified = settingsRepository.isPowerPeekEnabled()
        Log.d("MainActivity", "PowerPeek save verification: enabled=$verified")
        
        Toast.makeText(
            this,
            "PowerPeek enabled! Shake your device when screen is off to see battery percentage.",
            Toast.LENGTH_LONG
        ).show()

        val serviceIntent = Intent(this, PowerPeekService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        
        Log.d("MainActivity", "PowerPeek functionality enabled and service started.")
    }

    /**
     * Disable PowerPeek functionality and stop the service
     */
    fun disablePowerPeek() {
        Log.d("MainActivity", "disablePowerPeek() called")
        
        // Save to settings first
        settingsRepository.savePowerPeekEnabled(false)
        
        // Verify the save worked immediately
        val verified = settingsRepository.isPowerPeekEnabled()
        Log.d("MainActivity", "PowerPeek disable verification: enabled=$verified")
        
        val serviceIntent = Intent(this, PowerPeekService::class.java)
        stopService(serviceIntent)
        Toast.makeText(this, "PowerPeek disabled", Toast.LENGTH_SHORT).show()
        Log.d("MainActivity", "PowerPeek functionality disabled and service stopped.")
    }

    /**
     * Run notification effect
     */
    fun runNotificationEffect() {
        animJob?.cancel()
        animJob = lifecycleScope.launch {
            try {
                if (glyphServiceState.value) {
                    glyphAnimationManager.runNotificationEffect()
                    delay(settingsRepository.getDisplayDuration())
                } else {
                    showServiceDisabledToast()
                }
            } finally {
                if (glyphServiceState.value) {
                    glyphAnimationManager.stopAnimations()
                }
            }
        }
    }

    /**
     * Test individual Glyph channel
     */
    fun testGlyphChannel(channelIndex: Int) {
        // Check if operations can be performed (bypass service check for direct hardware access)
        if (!glyphManager.isNothingPhone()) {
            Toast.makeText(
                this,
                "Glyph channel testing only works on Nothing phones",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Cancel any previous animation job
        animJob?.cancel()
        isGlyphDemoRunning = false

        // Show toast notification
        Toast.makeText(this, "Testing Glyph Channel $channelIndex...", Toast.LENGTH_SHORT).show()

        // Run the test in a coroutine
        animJob = lifecycleScope.launch {
            try {
                glyphAnimationManager.testGlyphChannel(channelIndex, true) // Bypass service check for direct testing
                delay(settingsRepository.getDisplayDuration())
                glyphAnimationManager.stopAnimations()
                Toast.makeText(
                    this@MainActivity,
                    "Channel $channelIndex test completed",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error testing Glyph channel $channelIndex: ${e.message}")
                Toast.makeText(
                    this@MainActivity,
                    "Error testing channel $channelIndex: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Test individual C1 segment
     */
    fun testC1Segment(c1Index: Int) {
        // Check if operations can be performed (bypass service check for direct hardware access)
        if (!glyphManager.isNothingPhone()) {
            Toast.makeText(
                this,
                "C1 segment testing only works on Nothing phones",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Cancel any previous animation job
        animJob?.cancel()
        isGlyphDemoRunning = false

        // Show toast notification
        Toast.makeText(this, "Testing C1 Segment $c1Index...", Toast.LENGTH_SHORT).show()

        // Run the test in a coroutine
        animJob = lifecycleScope.launch {
            try {
                glyphAnimationManager.testC1Segment(c1Index, true) // Bypass service check for direct testing
                delay(settingsRepository.getDisplayDuration())
                glyphAnimationManager.stopAnimations()
                Toast.makeText(
                    this@MainActivity,
                    "C1 segment $c1Index test completed",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error testing C1 segment $c1Index: ${e.message}")
                Toast.makeText(
                    this@MainActivity,
                    "Error testing C1 segment $c1Index: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Test C14 and C15 specifically for debugging dim state issues
     */
    fun testC14AndC15() {
        // Check if operations can be performed (bypass service check for direct hardware access)
        if (!glyphManager.isNothingPhone()) {
            Toast.makeText(
                this,
                "C14/C15 testing only works on Nothing phones",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Cancel any previous animation job
        animJob?.cancel()
        isGlyphDemoRunning = false

        // Show toast notification
        Toast.makeText(this, "Testing C14 and C15 specifically...", Toast.LENGTH_SHORT).show()

        // Run the test in a coroutine
        animJob = lifecycleScope.launch {
            try {
                glyphAnimationManager.testC14AndC15Specifically(true) // Bypass service check for direct testing
                delay(settingsRepository.getDisplayDuration())
                glyphAnimationManager.stopAnimations()
                Toast.makeText(
                    this@MainActivity,
                    "C14 and C15 test completed",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error testing C14 and C15: ${e.message}")
                Toast.makeText(
                    this@MainActivity,
                    "Error testing C14/C15: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Test the final state before turnoff to debug C14/C15 issue
     */
    fun testFinalStateBeforeTurnoff() {
        // Check if operations can be performed (bypass service check for direct hardware access)
        if (!glyphManager.isNothingPhone()) {
            Toast.makeText(
                this,
                "Final state testing only works on Nothing phones",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Cancel any previous animation job
        animJob?.cancel()
        isGlyphDemoRunning = false

        // Show toast notification
        Toast.makeText(this, "Testing final state before turnoff...", Toast.LENGTH_SHORT).show()

        // Run the test in a coroutine
        animJob = lifecycleScope.launch {
            try {
                glyphAnimationManager.testFinalStateBeforeTurnoff(true) // Bypass service check for direct testing
                delay(settingsRepository.getDisplayDuration())
                glyphAnimationManager.stopAnimations()
                Toast.makeText(
                    this@MainActivity,
                    "Final state test completed",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error testing final state: ${e.message}")
                Toast.makeText(
                    this@MainActivity,
                    "Error testing final state: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Test C14 and C15 in complete isolation
     */
    fun testC14AndC15Isolated() {
        // Check if operations can be performed (bypass service check for direct hardware access)
        if (!glyphManager.isNothingPhone()) {
            Toast.makeText(
                this,
                "C14/C15 isolation testing only works on Nothing phones",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // Cancel any previous animation job
        animJob?.cancel()
        isGlyphDemoRunning = false

        // Show toast notification
        Toast.makeText(this, "Testing C14 and C15 in isolation...", Toast.LENGTH_SHORT).show()

        // Run the test in a coroutine
        animJob = lifecycleScope.launch {
            try {
                glyphAnimationManager.testOnlyC14AndC15Isolated(true) // Bypass service check for direct testing
                delay(settingsRepository.getDisplayDuration())
                glyphAnimationManager.stopAnimations()
                Toast.makeText(
                    this@MainActivity,
                    "C14/C15 isolation test completed",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error testing C14/C15 isolation: ${e.message}")
                Toast.makeText(
                    this@MainActivity,
                    "Error testing C14/C15 isolation: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Run D1 sequential animation
     */
    fun runD1SequentialAnimation() {
        // Check if operations can be performed
        if (!canPerformGlyphOperation()) return

        // Cancel any previous animation job
        animJob?.cancel()
        isGlyphDemoRunning = false

        // Show toast notification
        Toast.makeText(this, "Running D1 sequential animation...", Toast.LENGTH_SHORT).show()

        // Run the animation in a coroutine
        animJob = lifecycleScope.launch {
            try {
                glyphAnimationManager.runD1SequentialAnimation()
                delay(settingsRepository.getDisplayDuration())
                glyphAnimationManager.stopAnimations()
                Toast.makeText(
                    this@MainActivity,
                    "D1 sequential animation completed",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error running D1 sequential animation: ${e.message}")
            }
        }
    }

    /**
     * Run D1 sequential animation with progress tracking
     */
    fun runD1SequentialAnimationWithProgress(onProgressUpdate: (Float) -> Unit) {
        // Check if operations can be performed
        if (!canPerformGlyphOperation()) {
            onProgressUpdate(1.0f)
            return
        }

        // Cancel any previous animation job
        animJob?.cancel()
        isGlyphDemoRunning = false

        // Run the animation in a coroutine
        animJob = lifecycleScope.launch {
            try {
                glyphAnimationManager.runD1SequentialAnimationWithProgress(onProgressUpdate)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error running D1 sequential animation with progress: ${e.message}")
                onProgressUpdate(1.0f)
            }
        }
    }

    /**
     * Run animation with progress tracking
     */
    private suspend fun runAnimationWithProgress(
        animationType: String,
        bypassServiceCheck: Boolean = false,
        onProgressUpdate: (Float) -> Unit
    ) {
        try {
            onProgressUpdate(0.0f)
            
            when (animationType.lowercase()) {
                "c1 sequential" -> {
                    // Run C1 animation with progress tracking
                    onProgressUpdate(0.1f)
                    glyphAnimationManager.runC1SequentialAnimationWithProgress(onProgressUpdate)
                }
                "d1 sequential" -> {
                    // Run D1 animation with progress tracking
                    onProgressUpdate(0.1f)
                    glyphAnimationManager.runD1SequentialAnimationWithProgress(onProgressUpdate)
                }
                "zone test" -> {
                    onProgressUpdate(0.1f)
                    glyphAnimationManager.testAllZonesWithProgress(bypassServiceCheck, onProgressUpdate)
                }
                "pattern test" -> {
                    onProgressUpdate(0.1f)
                    glyphAnimationManager.testCustomPatternWithProgress(bypassServiceCheck, onProgressUpdate)
                }
                else -> {
                    // Default fallback
                    for (i in 0..100) {
                        onProgressUpdate(i / 100f)
                        delay(30)
                    }
                }
            }
            
            onProgressUpdate(1.0f)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error running animation with progress: ${e.message}")
            onProgressUpdate(1.0f)
        }
    }

    override fun onStop() {
        super.onStop()
        // Store current service state
        wasServiceEnabled = glyphServiceState.value

        // Only stop animations and close session if PowerPeek is not enabled
        if (!settingsRepository.isPowerPeekEnabled()) {
            // Cancel any running animations
            animJob?.cancel()
            isGlyphDemoRunning = false
            // Stop glyph animations
            glyphManager.cancelAllAnimations()
            glyphAnimationManager.stopAnimations()
            // Temporarily disable service if it was enabled
            if (wasServiceEnabled) {
                glyphManager.closeSession()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-enable service if it was enabled before
        if (wasServiceEnabled) {
            glyphManager.openSession()
        }
        
        // Also attempt restoration based on persisted preference
        maybeRestoreSession()
        
        // Add watermark overlay
        WatermarkHelper.addToActivity(this)
    }

    override fun onDestroy() {
        // Cancel any running animations
        animJob?.cancel()
        isGlyphDemoRunning = false

        // Only clean up glyph resources if user preference says service should be OFF.
        if (!settingsRepository.getGlyphServiceEnabled()) {
            glyphManager.cleanup()
        }

        // Clean up watermark
        WatermarkHelper.removeFromActivity(this)

        super.onDestroy()
    }

    private fun showServiceDisabledToast() {
        Toast.makeText(this, "Glyph service is not enabled.", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Request battery optimization exemption for reliable background operation
     */
    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = android.net.Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                    Toast.makeText(
                        this,
                        "Please allow Glyph Guard to run in background for reliable protection",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to request battery optimization exemption: ${e.message}")
                }
            }
        }
    }

    /**
     * Attempt to restore a Glyph session if the user had it enabled.
     * Will retry a few times with exponential back-off.
     */
    private fun maybeRestoreSession() {
        if (!settingsRepository.getGlyphServiceEnabled()) return  // user left it off
        if (glyphManager.isSessionActive) return               // already active

        // Avoid duplicate simultaneous jobs
        if (restoreSessionJob?.isActive == true) return

        restoreSessionJob = lifecycleScope.launch {
            var attempt = 0
            val maxAttempts = 5
            var delayMs = 250L
            while (true) {
                if (!settingsRepository.getGlyphServiceEnabled()) return@launch
                if (glyphManager.isSessionActive) break

                // Wait until connected
                if (!glyphManager.isServiceConnected) {
                    delay(delayMs)
                    continue
                }

                try {
                    glyphManager.openSession()
                } catch (_: Exception) {
                    // ignore
                }
                if (!glyphManager.isSessionActive) {
                    delay(delayMs)
                    delayMs = (delayMs * 2).coerceAtMost(5000L)
                }
            }
        }
    }

    /**
     * Sequentially run PowerPeek and Glyph Guard tests, then prompt to save Logcat.
     */
    fun runDiagnostics() {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@MainActivity, "Running diagnostics…", Toast.LENGTH_SHORT).show()

                // PowerPeek test (bypass service state)
                testPowerPeek(true)
                animJob?.join()

                // Glyph Guard test (bypass service state)
                testGlyphGuard(true)
                animJob?.join()

                // Capture logcat now
                pendingLogText = withContext(Dispatchers.IO) { captureLogcatText() }

                withContext(Dispatchers.Main) {
                    android.app.AlertDialog.Builder(this@MainActivity)
                        .setTitle("Save Logcat?")
                        .setMessage("Diagnostics finished. Would you like to save the Logcat to a file?")
                        .setPositiveButton("Save") { _, _ ->
                            val filename = "glyph_diagnostics_${System.currentTimeMillis()}.log"
                            createLogFileLauncher.launch(filename)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Diagnostics error", e)
                Toast.makeText(this@MainActivity, "Diagnostics failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Capture logcat text (blocking IO call).
     */
    private fun captureLogcatText(): String {
        val process = Runtime.getRuntime().exec("logcat -d")
        return process.inputStream.bufferedReader().use { it.readText() }
    }

    /**
     * Write log to the provided SAF URI then prompt for sharing.
     */
    private fun writeLogToUri(uri: android.net.Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                    writer.write(pendingLogText ?: "")
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Log saved", Toast.LENGTH_SHORT).show()
                    promptShareLog(uri)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error writing log", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Failed to save log: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Prompt to share the saved log file.
     */
    private fun promptShareLog(uri: android.net.Uri) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Share Logcat?")
            .setMessage("Log saved successfully. Would you like to share it?")
            .setPositiveButton("Share") { _, _ ->
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Share log via"))
            }
            .setNegativeButton("Close", null)
            .show()
    }

    /**
     * Test Glow Gate animation without changing enable state
     */
    fun testPulseLock() {
        lifecycleScope.launch {
            try {
                val id = settingsRepository.getPulseLockAnimationId()
                glyphAnimationManager.playPulseLockAnimation(id)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error testing Glow Gate", e)
            }
        }
    }

    /** Enable Glow Gate */
    fun enablePulseLock() {
        settingsRepository.savePulseLockEnabled(true)
        initializePulseLock() // will start service
        Toast.makeText(this, "Glow Gate enabled", Toast.LENGTH_SHORT).show()
    }

    /** Disable Glow Gate */
    fun disablePulseLock() {
        settingsRepository.savePulseLockEnabled(false)
        initializePulseLock() // will stop service
        Toast.makeText(this, "Glow Gate disabled", Toast.LENGTH_SHORT).show()
    }

    /** Test Low Battery Alert */
    fun testLowBatteryAlert() {
        val serviceIntent = Intent(this, LowBatteryAlertService::class.java).apply {
            action = LowBatteryAlertService.ACTION_TEST_ALERT
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            Toast.makeText(this, "Testing Low Battery Alert", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to test low battery alert", e)
            Toast.makeText(this, "Failed to test alert: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startPersistentGlyphService() {
        if (!settingsRepository.getGlyphServiceEnabled()) return

        val intent = Intent(this, com.bleelblep.glyphsharge.services.GlyphForegroundService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isNothingPhone: Boolean,
    glyphServiceEnabled: Boolean,
    onGlyphServiceToggle: (Boolean) -> Unit,
    onLaunchGlyphDemo: () -> Unit,
    onTestAllZones: (Boolean) -> Unit,
    onTestCustomPattern: (Boolean) -> Unit,
    onRunWaveAnimation: () -> Unit,
    onRunPulseEffect: () -> Unit,
    onTestGlyphGuard: () -> Unit,
    onStartGlyphGuard: () -> Unit,
    onStopGlyphGuard: () -> Unit,
    onRunBoxBreathing: () -> Unit,
    onTestPowerPeek: () -> Unit,
    onEnablePowerPeek: () -> Unit,
    onDisablePowerPeek: () -> Unit,
    onRunNotificationEffect: () -> Unit,
    onTestGlyphChannel: (Int) -> Unit,
    onTestC1Segment: (Int) -> Unit,
    onTestFinalState: () -> Unit,
    onTestC14C15Isolated: () -> Unit,
    onTestPulseLock: () -> Unit,
    onEnablePulseLock: () -> Unit,
    onDisablePulseLock: () -> Unit,
    onTestLowBattery: () -> Unit,
    onRunDiagnostics: () -> Unit,
    backgroundColorMain: ComposeColor,
    settingsRepository: SettingsRepository
) {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val themeState = LocalThemeState.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onEnablePowerPeek()
        } else {
            Toast.makeText(context, "Permission denied. PowerPeek cannot run without it.", Toast.LENGTH_LONG).show()
        }
    }

    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = { MaterialSharedAxisZ.enterTransition() },
        exitTransition = { MaterialSharedAxisZ.exitTransition() },
        popEnterTransition = { MaterialSharedAxisZ.popEnterTransition() },
        popExitTransition = { MaterialSharedAxisZ.popExitTransition() }
    ) {
        composable("home") {
            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
            val scrollState = rememberLazyListState()

            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = {
                    LargeTopAppBar(
                        title = { 
                            Text(
                                "Glyph Sharge",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 42.sp
                                )
                            ) 
                        },
                        actions = {
                            IconButton(onClick = {
                                navController.navigate("settings")
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings"
                                )
                            }
                        },
                        scrollBehavior = scrollBehavior,
                        windowInsets = WindowInsets.statusBars,
                        colors = TopAppBarDefaults.largeTopAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                containerColor = backgroundColorMain,
                contentWindowInsets = WindowInsets(0.dp)
            ) { paddingValues ->
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColorMain)
                        .padding(paddingValues),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    userScrollEnabled = true
                ) {
                    // Main Glyph Lights toggle using GlyphControlCard template
                    item {
                        GlyphControlCard(
                            enabled = glyphServiceEnabled,
                            onEnabledChange = onGlyphServiceToggle,
                            illustrationRes = R.drawable.su
                        )
                    }

                    // Features Section Header
                    item {
                        HomeSectionHeader(
                            title = "Features",
                            modifier = Modifier.padding(start = 0.dp)
                        )
                    }

                    // Feature Cards Grid
                    item {
                        FeatureGrid {
                            // Power Peek Card
                            PowerPeekCard(
                                title = "Power Peek",
                                description = "Peek at your battery life with a quick shake.",
                                icon = painterResource(id = R.drawable._44),
                                onTestPowerPeek = { 
                                    if (glyphServiceEnabled) {
                                        onTestPowerPeek()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Please enable the Glyph service first",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onEnablePowerPeek = {
                                    if (glyphServiceEnabled) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                            when (ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE
                                            )) {
                                                PackageManager.PERMISSION_GRANTED -> {
                                                    onEnablePowerPeek()
                                                }
                                                else -> {
                                                    permissionLauncher.launch(Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE)
                                                }
                                            }
                                        } else {
                                            onEnablePowerPeek()
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Please enable the Glyph service first",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onDisablePowerPeek = {
                                    if (glyphServiceEnabled) {
                                        onDisablePowerPeek()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Please enable the Glyph service first",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                iconSize = 40,
                                isServiceActive = glyphServiceEnabled,
                                settingsRepository = settingsRepository
                            )

                            // Glyph Guard Card
                            GlyphGuardCard(
                                title = "Glyph Guard",
                                description = "Stay secure with Glyph and Sound Alerts if Unplugged.",
                                icon = painterResource(id = R.drawable._78),
                                onTest = {
                                    if (glyphServiceEnabled) {
                                        onTestGlyphGuard()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Please enable the Glyph service first",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onStart = {
                                    if (glyphServiceEnabled) {
                                        onStartGlyphGuard()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Please enable the Glyph service first",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                onStop = {
                                    onStopGlyphGuard()
                                },
                                modifier = Modifier.weight(1f),
                                iconSize = 32,
                                isServiceActive = glyphServiceEnabled,
                                glyphGuardMode = GlyphGuardMode.Standard,
                                settingsRepository = settingsRepository
                            )
                        }
                    }

                    // Expressive Demo Components removed for cleaner expressive theme experience

                    // Battery Story & Glow Gate feature cards
                    item {
                        FeatureGrid {
                            PulseLockCard(
                                title = "Glow Gate",
                                description = "Light up your unlock with stunning glyph animations.",
                                icon = rememberVectorPainter(image = Icons.Default.Lock),
                                modifier = Modifier.weight(1f),
                                iconSize = 32,
                                isServiceActive = glyphServiceEnabled,
                                onTestPulseLock = {
                                    if (glyphServiceEnabled) {
                                        onTestPulseLock()
                                    } else {
                                        Toast.makeText(context, "Please enable the Glyph service first", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onEnablePulseLock = {
                                    if (glyphServiceEnabled) {
                                        onEnablePulseLock()
                                    } else {
                                        Toast.makeText(context, "Please enable the Glyph service first", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onDisablePulseLock = { onDisablePulseLock() },
                                settingsRepository = settingsRepository
                            )

                            BatteryStoryCard(
                                title = "Battery Story",
                                description = "Track your device's charging patterns and battery health.",
                                icon = rememberVectorPainter(image = Icons.Default.BatteryChargingFull),
                                onOpen = {
                                    navController.navigate("battery_story")
                                },
                                modifier = Modifier.weight(1f),
                                iconSize = 32,
                                settingsRepository = settingsRepository
                            )
                        }
                    }

                    // Low Battery Alert card re-enabled after redesign
                    item {
                        LowBatteryAlertCard(
                            onTestAlert = { onTestLowBattery() },
                            modifier = Modifier.fillMaxWidth(),
                            settingsRepository = settingsRepository,
                            isServiceActive = glyphServiceEnabled
                        )
                    }

                    // Information Card (Wide)
                    item {
                        InformationCard(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        composable("settings") {
            Box(modifier = Modifier.fillMaxSize()) {
                SettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onHiddenSettingsAccess = {
                        navController.navigate("hidden_settings")
                    },
                    onThemeSettingsClick = {
                        navController.navigate("theme_settings")
                    },
                    onFontSettingsClick = {
                        navController.navigate("font_settings")
                    },
                    onVibrationSettingsClick = {
                        navController.navigate("vibration_settings")
                    },
                    onQuietHoursSettingsClick = {
                        navController.navigate("quiet_hours_settings")
                    },
                    settingsRepository = settingsRepository
                )
            }
        }
        composable("theme_settings") {
            Box(modifier = Modifier.fillMaxSize()) {
                ThemeSettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
        composable("font_settings") {
            Box(modifier = Modifier.fillMaxSize()) {
                FontSettingsScreen(
                    fontState = LocalFontState.current,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        composable("card_examples") {
            Box(modifier = Modifier.fillMaxSize()) {
                CardExamplesScreen(
                    paddingValues = PaddingValues(0.dp)
                )
            }
        }
        composable("hidden_settings") {
            Box(modifier = Modifier.fillMaxSize()) {
                HiddenSettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onLEDCalibrationClick = {
                        navController.navigate("led_calibration")
                    },
                    onCardExamplesClick = {
                        navController.navigate("card_examples")
                    },
                    onHardwareDiagnosticsClick = {
                        navController.navigate("hardware_diagnostics")
                    },
                    settingsRepository = settingsRepository
                )
            }
        }
        composable("hardware_diagnostics") {
            Box(modifier = Modifier.fillMaxSize()) {
                HardwareDiagnosticsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
        composable("led_calibration") {
            Box(modifier = Modifier.fillMaxSize()) {
                LEDCalibrationScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onTestAllZones = { onTestAllZones(true) },
                    onTestCustomPattern = { onTestCustomPattern(true) },
                    onTestChannel = { channelIndex -> onTestGlyphChannel(channelIndex) },
                    onTestC1Segment = { c1Index -> onTestC1Segment(c1Index) },
                    onTestFinalState = { onTestFinalState() },
                    onTestC14C15Isolated = { onTestC14C15Isolated() },
                    onTestD1Sequential = { 
                        val mainActivity = context as? MainActivity
                        mainActivity?.runD1SequentialAnimation()
                    }
                )
            }
        }
        composable("vibration_settings") {
            Box(modifier = Modifier.fillMaxSize()) {
                VibrationSettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    settingsRepository = settingsRepository
                )
            }
        }
        composable("quiet_hours_settings") {
            Box(modifier = Modifier.fillMaxSize()) {
                QuietHoursSettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    settingsRepository = settingsRepository
                )
            }
        }
        // Battery Story route
        composable("battery_story") {
            BatteryStoryScreen(onBackClick = { navController.popBackStack() })
        }
    }
}

// Material Design shared axis transition utility function - Z axis
// Optimized for hardware acceleration and smooth performance at 120Hz
object MaterialSharedAxisZ {
    // Ultra-optimized durations for 120Hz displays
    private const val ENTER_DURATION = 200  // Faster for 120Hz responsiveness
    private const val EXIT_DURATION = 120   // Very fast exit for snappy feel

    // Minimal scale factors for ultra-smooth transitions
    private const val SCALE_FACTOR_ENTER = 0.98f  // Minimal scale for smoothness
    private const val SCALE_FACTOR_EXIT = 1.02f   // Minimal scale change

    fun enterTransition(): EnterTransition = fadeIn(
        animationSpec = tween(
            durationMillis = ENTER_DURATION,
            easing = FastOutSlowInEasing  // Hardware-optimized easing
        )
    ) + scaleIn(
        animationSpec = tween(
            durationMillis = ENTER_DURATION,
            easing = FastOutSlowInEasing
        ),
        initialScale = SCALE_FACTOR_ENTER  // Minimal scale from 98% to 100%
    )

    fun exitTransition(): ExitTransition = fadeOut(
        animationSpec = tween(
            durationMillis = EXIT_DURATION,
            easing = EaseOut  // Faster, more responsive easing
        )
    ) + scaleOut(
        animationSpec = tween(
            durationMillis = EXIT_DURATION,
            easing = EaseOut
        ),
        targetScale = SCALE_FACTOR_EXIT  // Minimal scale to 102%
    )

    fun popEnterTransition(): EnterTransition = fadeIn(
        animationSpec = tween(
            durationMillis = ENTER_DURATION,
            easing = FastOutSlowInEasing  // Consistent smooth entering
        )
    ) + scaleIn(
        animationSpec = tween(
            durationMillis = ENTER_DURATION,
            easing = FastOutSlowInEasing
        ),
        initialScale = SCALE_FACTOR_EXIT  // Return from 102% to 100%
    )

    fun popExitTransition(): ExitTransition = fadeOut(
        animationSpec = tween(
            durationMillis = EXIT_DURATION,
            easing = EaseOut  // Fast, responsive back navigation
        )
    ) + scaleOut(
        animationSpec = tween(
            durationMillis = EXIT_DURATION,
            easing = EaseOut
        ),
        targetScale = SCALE_FACTOR_ENTER  // Scale down to 98%
    )
} 