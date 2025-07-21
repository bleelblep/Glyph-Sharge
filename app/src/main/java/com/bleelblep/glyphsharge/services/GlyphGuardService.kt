package com.bleelblep.glyphsharge.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.Handler

import androidx.core.app.NotificationCompat
import com.bleelblep.glyphsharge.R
import com.bleelblep.glyphsharge.glyph.GlyphAnimationManager
import com.bleelblep.glyphsharge.glyph.GlyphManager
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository
import kotlinx.coroutines.*
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GlyphGuardService : Service() {
    companion object {
        private const val TAG = "GlyphGuardService"
        private const val NOTIFICATION_ID = 1003
        private const val NOTIFICATION_CHANNEL_ID = "GlyphGuardServiceChannel"
        const val ACTION_START_GLYPH_GUARD = "com.bleelblep.glyphsharge.START_GLYPH_GUARD"
        const val ACTION_STOP_GLYPH_GUARD = "com.bleelblep.glyphsharge.STOP_GLYPH_GUARD"
    }

    @Inject
    lateinit var glyphAnimationManager: GlyphAnimationManager
    @Inject
    lateinit var glyphManager: GlyphManager
    @Inject
    lateinit var settingsRepository: SettingsRepository
    private lateinit var audioManager: AudioManager
    private lateinit var powerManager: PowerManager
    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    
    private var serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isGuardActive = false
    private var wasCharging = false
    
    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "BroadcastReceiver received intent: ${intent?.action}")
            

            
            when (intent?.action) {
                Intent.ACTION_POWER_DISCONNECTED -> {
                    Log.d(TAG, "⚡ ACTION_POWER_DISCONNECTED received!")

                    if (isGuardActive) {
                        Log.d(TAG, "Guard is active, triggering alert...")
                        triggerGlyphGuardAlert()
                    } else {
                        Log.d(TAG, "Guard is not active, ignoring disconnect")
                    }
                }
                Intent.ACTION_POWER_CONNECTED -> {
                    Log.d(TAG, "🔌 ACTION_POWER_CONNECTED received!")

                    stopAlert()
                }
                Intent.ACTION_BATTERY_CHANGED -> {
                    // Monitor charging state for more reliable detection
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                   status == BatteryManager.BATTERY_STATUS_FULL
                    
                    Log.v(TAG, "BATTERY_CHANGED: status=$status, plugged=$plugged, isCharging=$isCharging, wasCharging=$wasCharging")
                    
                    if (wasCharging && !isCharging && isGuardActive) {
                        Log.d(TAG, "🔋 Charging stopped via battery change - triggering Glyph Guard alert")

                        triggerGlyphGuardAlert()
                    }
                    wasCharging = isCharging
                }
                Intent.ACTION_BATTERY_LOW -> {
                    Log.d(TAG, "🪫 ACTION_BATTERY_LOW received")
                }
                Intent.ACTION_BATTERY_OKAY -> {
                    Log.d(TAG, "🔋 ACTION_BATTERY_OKAY received")
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🛡️ GlyphGuardService created")
        
        // Dependencies are injected automatically via @Inject annotations (like PowerPeek)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        
        // Acquire wake lock to keep CPU active when screen is off (like PowerPeek)
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "GlyphGuard::WakeLock"
        )
        
        // No need for screen wake lock - glyphs work with screen off (following PowerPeek approach)
        
        // Register power receivers with extended filter
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
            // Add USB-specific intents
            addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED")
            addAction("android.hardware.usb.action.USB_DEVICE_DETACHED")
        }
        
        Log.d(TAG, "📡 Registering BroadcastReceiver with filter actions: ${filter.actionsIterator().asSequence().toList()}")
        registerReceiver(powerReceiver, filter)
        
        // Get initial charging state and log detailed info
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        batteryIntent?.let { intent ->
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            
            wasCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                         status == BatteryManager.BATTERY_STATUS_FULL
            
            val plugType = when (plugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                else -> "None"
            }
            
            Log.d(TAG, "📊 Initial battery state: level=${level}/${scale}, status=$status, plugged=$plugged ($plugType), charging=$wasCharging")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "📨 GlyphGuardService onStartCommand: ${intent?.action}")
        
        // Global glyph service master switch guard
        if (!settingsRepository.getGlyphServiceEnabled()) {
            Log.d(TAG, "Glyph service disabled – GlyphGuardService ignoring intent ${intent?.action}")
            stopSelf()
            return START_STICKY
        }

        when (intent?.action) {
            ACTION_START_GLYPH_GUARD -> {
                startGlyphGuard()
            }
            ACTION_STOP_GLYPH_GUARD -> {
                stopGlyphGuard()
            }
        }
        
        return START_STICKY
    }

    private fun startGlyphGuard() {
        if (!settingsRepository.getGlyphServiceEnabled()) {
            Log.d(TAG, "Glyph service disabled – not starting Glyph Guard")
            return
        }

        Log.d(TAG, "🚀 Starting Glyph Guard protection")
        isGuardActive = true
        
        // Acquire wake lock to ensure service continues working when screen is off
        try {
            wakeLock?.let { wl ->
                if (!wl.isHeld) {
                    wl.acquire(10 * 60 * 1000L) // 10 minutes max timeout for safety
                    Log.d(TAG, "🔓 Wake lock acquired - service will work with screen off")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire wake lock: ${e.message}")
        }
        
        // NEW ► Ensure we have an active Glyph SDK session before running any animations
        glyphManager.forceEnsureSession()
        
        // Log current state for debugging
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        batteryIntent?.let { intent ->
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            Log.d(TAG, "🔍 Guard starting with current battery status: $status, plugged: $plugged")
        }
        
        startForeground(NOTIFICATION_ID, createNotification("🛡️ Glyph Guard Active", "Monitoring for USB disconnection..."))
        
        // Schedule periodic check as fallback
        schedulePeriodicCheck()
    }

    private fun stopGlyphGuard() {
        Log.d(TAG, "Stopping Glyph Guard protection")
        isGuardActive = false
        stopAlert()
        
        // Release wake lock
        try {
            wakeLock?.let { wl ->
                if (wl.isHeld) {
                    wl.release()
                    Log.d(TAG, "🔒 CPU wake lock released")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release wake lock: ${e.message}")
        }
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun triggerGlyphGuardAlert() {
        Log.d(TAG, "🚨 Triggering Glyph Guard alert - USB unplugged!")
        
        // Check if currently in quiet hours
        if (settingsRepository.isCurrentlyInQuietHours()) {
            Log.d(TAG, "Glyph Guard alert blocked by quiet hours")
            return
        }
        
        // Ensure Glyph session is active before running any LED changes
        if (!glyphManager.isSessionActive) {
            Log.d(TAG, "Glyph session not active — attempting to restore before alert")
            glyphManager.forceEnsureSession()
        }

        serviceScope.launch {
            try {
                Log.d(TAG, "🔍 Starting glyph alert using PowerPeek approach")
                
                // Update notification
                updateAlertNotification()
                
                val alertDuration = settingsRepository.getGlyphGuardDuration()
                
                // Start glyph animation and sound simultaneously
                val glyphJob = launch { runGlyphGuardAlertVisualization(alertDuration) }
            val soundJob = if (settingsRepository.isGlyphGuardSoundEnabled()) {
                launch { playAlarmSound() }
            } else {
                launch { 
                    Log.d(TAG, "🔇 Sound alerts disabled by user")
                        delay(alertDuration)
                    }
                }
            
            // Wait for both to complete
            glyphJob.join()
            soundJob.join()
                
                Log.d(TAG, "🔚 Glyph Guard alert completed")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in Glyph Guard alert: ${e.message}")
            }
        }
    }

    /**
     * Run Glyph Guard alert visualization using PowerPeek's exact approach
     * This mirrors runBatteryPercentageVisualization but for security alerts
     */
    private suspend fun runGlyphGuardAlertVisualization(durationMillis: Long) {
        Log.d(TAG, "runGlyphGuardAlertVisualization called for ${durationMillis}ms")
        
        if (!glyphManager.isNothingPhone()) {
            Log.d(TAG, "Not a Nothing phone - alert visualization will not run")
                return
        }

        try {
            Log.d(TAG, "Starting Glyph Guard alert visualization using PowerPeek method")
            
            // Reset glyphs first (like PowerPeek does)
            resetGlyphs()

            // Run device-specific alert animation
            when {
                com.nothing.ketchum.Common.is20111() -> runPhone1AlertVisualization(durationMillis)
                com.nothing.ketchum.Common.is22111() -> runPhone2AlertVisualization(durationMillis)
                com.nothing.ketchum.Common.is23111() || com.nothing.ketchum.Common.is23113() -> runPhone2aAlertVisualization(durationMillis)
                com.nothing.ketchum.Common.is24111() -> runPhone3aAlertVisualization(durationMillis)
                else -> runDefaultAlertVisualization(durationMillis)
            }
        } finally {
            ensureCleanup()
        }
    }

    private suspend fun resetGlyphs() {
        try {
            val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: return
            val frame = builder.build()
            glyphManager.mGM?.toggle(frame)
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting glyphs: ${e.message}")
        }
    }

    private suspend fun ensureCleanup() {
        try {
            glyphManager.mGM?.turnOff()
            delay(100L)
            Log.d(TAG, "Alert cleanup completed")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}")
        }
    }

    private suspend fun runPhone1AlertVisualization(durationMillis: Long) {
        val stepDelay = 100L // Fast blinking for alert
        val startTime = System.currentTimeMillis()
        var step = 0
        
        while (isGuardActive && (System.currentTimeMillis() - startTime) < durationMillis) {
            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                // Rapid blinking effect - all channels at max brightness
                val brightness = if (step % 2 == 0) 4095 else 0
                
                // Phone 1: A, B, C1-C4, E, D1_1-D1_8
                builder.buildChannel(0, brightness) // A
                builder.buildChannel(1, brightness) // B
                for (i in 2..5) builder.buildChannel(i, brightness) // C1-C4
                builder.buildChannel(6, brightness) // E
                for (i in 7..14) builder.buildChannel(i, brightness) // D1_1-D1_8
                
                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDelay)
                step++
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in Phone 1 alert visualization: ${e.message}")
                delay(stepDelay)
                step++
            }
        }
    }

    private suspend fun runPhone2AlertVisualization(durationMillis: Long) {
        val stepDelay = 100L
        val startTime = System.currentTimeMillis()
        var step = 0
        
        while (isGuardActive && (System.currentTimeMillis() - startTime) < durationMillis) {
            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                val brightness = if (step % 2 == 0) 4095 else 0
                
                // Phone 2: All segments (0-32)
                for (i in 0..32) builder.buildChannel(i, brightness)
                
                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDelay)
                step++
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in Phone 2 alert visualization: ${e.message}")
                delay(stepDelay)
                step++
            }
        }
    }

    private suspend fun runPhone2aAlertVisualization(durationMillis: Long) {
        val stepDelay = 100L
        val startTime = System.currentTimeMillis()
        var step = 0
        
        while (isGuardActive && (System.currentTimeMillis() - startTime) < durationMillis) {
            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                val brightness = if (step % 2 == 0) 4095 else 0
                
                // Phone 2a: C1-C24, B, A (0-25)
                for (i in 0..25) builder.buildChannel(i, brightness)
                
                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDelay)
                step++
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in Phone 2a alert visualization: ${e.message}")
                delay(stepDelay)
                step++
            }
        }
    }

    private suspend fun runPhone3aAlertVisualization(durationMillis: Long) {
        val stepDelay = 100L
        val startTime = System.currentTimeMillis()
        var step = 0
        
        while (isGuardActive && (System.currentTimeMillis() - startTime) < durationMillis) {
            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                val brightness = if (step % 2 == 0) 4095 else 0
                
                // Phone 3a: A1-A11 (20-30), B1-B5 (31-35), C1-C20 (0-19) - per GDK docs
                for (i in 20..30) builder.buildChannel(i, brightness) // A1-A11
                for (i in 31..35) builder.buildChannel(i, brightness) // B1-B5
                for (i in 0..19) builder.buildChannel(i, brightness) // C1-C20
                
                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDelay)
                step++
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in Phone 3a alert visualization: ${e.message}")
                delay(stepDelay)
                step++
            }
        }
    }

    private suspend fun runDefaultAlertVisualization(durationMillis: Long) {
        val stepDelay = 100L
        val startTime = System.currentTimeMillis()
        var step = 0
        
        while (isGuardActive && (System.currentTimeMillis() - startTime) < durationMillis) {
            try {
                val builder = glyphManager.mGM?.getGlyphFrameBuilder() ?: break
                
                val brightness = if (step % 2 == 0) 4095 else 0
                
                // Use first 10 channels for default
                for (i in 0..9) builder.buildChannel(i, brightness)
                
                val frame = builder.build()
                glyphManager.mGM?.toggle(frame)
                delay(stepDelay)
                step++
                
        } catch (e: Exception) {
                Log.e(TAG, "Error in default alert visualization: ${e.message}")
                delay(stepDelay)
                step++
            }
        }
    }

    private suspend fun playAlarmSound() {
        try {
            // Stop any existing media player
            mediaPlayer?.release()
            
            // Check if user has selected a custom ringtone
            val customRingtoneUri = settingsRepository.getGlyphGuardCustomRingtoneUri()
            val alarmUri: Uri = if (customRingtoneUri != null) {
                try {
                    Uri.parse(customRingtoneUri)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse custom ringtone URI: $customRingtoneUri, using default")
                    getDefaultSoundUri()
                }
            } else {
                getDefaultSoundUri()
            }
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@GlyphGuardService, alarmUri)
                setAudioStreamType(AudioManager.STREAM_ALARM)
                isLooping = true
                prepare()
                start()
            }
            
            val soundDescription = if (customRingtoneUri != null) "custom ringtone" else "default alarm"
            Log.d(TAG, "Playing $soundDescription for USB disconnection")
            
            // Wait for the configured duration, then stop if still active
            delay(settingsRepository.getGlyphGuardDuration())
            if (isGuardActive) {
                stopAlert()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error playing alarm sound: ${e.message}")
        }
    }

    private fun stopAlert() {
        Log.d(TAG, "Stopping Glyph Guard alert")
        
        // Stop glyph alert using PowerPeek approach
        serviceScope.launch {
            ensureCleanup()
        }
        
        // Stop alarm sound
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping media player: ${e.message}")
            }
        }
        mediaPlayer = null
        
        // NEW ► Close Glyph session once alert is fully stopped to release resources
        try {
            glyphManager.closeSession()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing Glyph session after alert: ${e.message}")
        }

        // Update notification back to monitoring state
        if (isGuardActive) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(NOTIFICATION_ID, createNotification("🛡️ Glyph Guard Active", "Monitoring for USB disconnection..."))
        }
    }

    private fun updateAlertNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification("⚠️ USB DISCONNECTED!", "Glyph Guard detected charger removal"))
    }

    private fun createNotification(title: String, text: String): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Glyph Guard Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable._44)
            .build()
    }

    private fun schedulePeriodicCheck() {
        serviceScope.launch {
            while (isGuardActive) {
                delay(2000L) // Check every 2 seconds
                
                val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                batteryIntent?.let { intent ->
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                   status == BatteryManager.BATTERY_STATUS_FULL
                    
                    if (wasCharging && !isCharging && plugged == 0) {
                        Log.d(TAG, "⏰ Periodic check detected USB disconnect!")
                        triggerGlyphGuardAlert()
                    }
                    
                    wasCharging = isCharging
                }
            }
        }
    }

    // initializeGlyphSession removed - not needed with dependency injection (like PowerPeek)


    override fun onDestroy() {
        Log.d(TAG, "GlyphGuardService destroyed")
        
        try {
            unregisterReceiver(powerReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver: ${e.message}")
        }
        
        stopAlert()
        
        // Release wake lock if held
        try {
            wakeLock?.let { wl ->
                if (wl.isHeld) {
                    wl.release()
                    Log.d(TAG, "🔒 CPU wake lock released in onDestroy")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release wake lock in onDestroy: ${e.message}")
        }
        
        // Glyph resources cleaned up automatically via dependency injection
        
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
    
    // wakeAndUnlockScreen function removed - not needed since glyphs work with screen off (like PowerPeek)
    
    private fun getDefaultSoundUri(): Uri {
        val soundType = settingsRepository.getGlyphGuardSoundType()
        val ringtoneType = when (soundType) {
            "ALARM" -> RingtoneManager.TYPE_ALARM
            "NOTIFICATION" -> RingtoneManager.TYPE_NOTIFICATION
            "RINGTONE" -> RingtoneManager.TYPE_RINGTONE
            else -> RingtoneManager.TYPE_ALARM
        }
        
        return RingtoneManager.getDefaultUri(ringtoneType)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    }

    private fun checkGlyphService() {
        if (!glyphManager.isNothingPhone()) {
            Log.w(TAG, "Not a Nothing phone, stopping Glyph Guard")
            stopSelf()
            return
        }

        if (!glyphManager.isSessionActive) {
            Log.w(TAG, "Glyph session not active, attempting to reconnect")
            glyphManager.forceEnsureSession()
            return
        }

        Log.d(TAG, "Glyph service check passed")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // If the user swipes the task away, immediately restart the guard for continuous protection
        val restartIntent = Intent(this, GlyphGuardService::class.java).apply {
            action = ACTION_START_GLYPH_GUARD
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent)
        } else {
            startService(restartIntent)
        }
    }
} 