package com.bleelblep.glyphsharge.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.bleelblep.glyphsharge.R
import com.bleelblep.glyphsharge.di.GlyphComponent
import com.bleelblep.glyphsharge.glyph.GlyphAnimationManager
import com.bleelblep.glyphsharge.glyph.GlyphFeature
import com.bleelblep.glyphsharge.glyph.GlyphFeatureCoordinator
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LowBatteryAlertService : Service() {

    @Inject
    lateinit var glyphAnimationManager: GlyphAnimationManager
    @Inject
    lateinit var settingsRepository: SettingsRepository
    @Inject
    lateinit var featureCoordinator: GlyphFeatureCoordinator

    private var powerManager: PowerManager? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    // Track whether we are currently in foreground (notification shown)
    private var isForeground = false

    // Track state to avoid repeated alerts within same discharge cycle
    @Volatile
    private var lowBatteryTriggered = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                
                val batteryPercentage = if (level >= 0 && scale > 0) level * 100 / scale else 0
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                               status == BatteryManager.BATTERY_STATUS_FULL

                // Low-Battery Alert Logic
                if (settingsRepository.isLowBatteryEnabled() && !isCharging) {
                    val threshold = settingsRepository.getLowBatteryThreshold()
                    // Fire alert once per discharge cycle
                    if (!lowBatteryTriggered && batteryPercentage <= threshold) {
                        lowBatteryTriggered = true
                        Log.d(TAG, "Low battery ${batteryPercentage}% reached – triggering alert")
                        serviceScope.launch { playLowBatterySequence() }
                    }

                    // Reset trigger when level recovers 5% above threshold or charging starts
                    if (isCharging || batteryPercentage >= threshold + 5) {
                        lowBatteryTriggered = false
                    }
                } else {
                    // If feature disabled ensure flag reset so it can trigger later if re-enabled
                    lowBatteryTriggered = false
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        
        // Acquire wake lock to keep CPU active when screen is off
        wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "LowBatteryAlert::WakeLock"
        )

        // Register battery receiver
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        registerReceiver(batteryReceiver, filter)
        Log.d(TAG, "LowBatteryAlertService created and battery receiver registered.")

        // Promote to foreground immediately to comply with Android 14 FGS start rule
        ensureForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "LowBatteryAlertService started")

        // Handle test action
        if (intent?.action == ACTION_TEST_ALERT) {
            Log.d(TAG, "Test alert requested")
            serviceScope.launch {
                playLowBatterySequence()
            }
            return START_STICKY
        }

        // Acquire wake lock to ensure service continues working when screen is off
        try {
            wakeLock?.let { wl ->
                if (!wl.isHeld) {
                    wl.acquire(10 * 60 * 1000L) // 10 minutes max timeout for safety
                    Log.d(TAG, "Wake lock acquired - service will work with screen off")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire wake lock: ${e.message}")
        }

        return START_STICKY
    }

    private fun ensureForeground() {
        if (!isForeground) {
            startForeground(NOTIFICATION_ID, createNotification())
            isForeground = true
        }
    }

    /** Plays the configured low-battery animation & optional sound */
    private suspend fun playLowBatterySequence() {
        try {
            // Check if currently in quiet hours
            if (settingsRepository.isCurrentlyInQuietHours()) {
                Log.d(TAG, "Low battery alert blocked by quiet hours")
                return
            }
            
            val animId = settingsRepository.getLowBatteryAnimationId()
            val audioEnabled = settingsRepository.isLowBatteryAudioEnabled()
            val audioUri = settingsRepository.getLowBatteryAudioUri()
            val offset = settingsRepository.getLowBatteryAudioOffset()
            val duration = settingsRepository.getLowBatteryDuration()

            // Acquire LED lock first
            if (!featureCoordinator.acquire(GlyphFeature.LOW_BATTERY)) {
                Log.d(TAG, "Low-battery alert skipped – LEDs busy by ${featureCoordinator.currentOwner.value}")
                return
            }

            kotlinx.coroutines.coroutineScope {
                val animJob = launch(Dispatchers.Default) {
                    glyphAnimationManager.playLowBatteryAnimation(animId)
                }

                // Launch duration timer that will stop the animation when time expires
                val durationJob = launch {
                    kotlinx.coroutines.delay(duration)
                    Log.d(TAG, "Duration limit reached, stopping animation and audio")
                    glyphAnimationManager.stopAnimations()
                    stopAudio()
                }

                // Handle sound timing
                if (audioEnabled && !audioUri.isNullOrEmpty()) {
                    try {
                        applicationContext.contentResolver.openInputStream(android.net.Uri.parse(audioUri))?.use {
                            if (offset < 0) {
                                playAudio(audioUri)
                                kotlinx.coroutines.delay(-offset)
                                animJob.join()
                            } else if (offset > 0) {
                                kotlinx.coroutines.delay(offset)
                                playAudio(audioUri)
                                animJob.join()
                            } else {
                                playAudio(audioUri)
                                animJob.join()
                            }
                        } ?: animJob.join()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error accessing audio URI: $audioUri", e)
                        animJob.join()
                    }
                } else {
                    animJob.join()
                }

                // Cancel the duration timer since animation completed naturally
                durationJob.cancel()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing low-battery sequence", e)
        } finally {
            featureCoordinator.release(GlyphFeature.LOW_BATTERY)
        }
    }

    // Simple MediaPlayer helper
    private var mediaPlayer: android.media.MediaPlayer? = null

    private fun playAudio(uriStr: String) {
        mediaPlayer?.let {
            try { it.stop(); it.release() } catch (_: Exception) {}
        }
        mediaPlayer = null

        try {
            val uri = android.net.Uri.parse(uriStr)
            mediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(applicationContext, uri)
                setAudioStreamType(android.media.AudioManager.STREAM_NOTIFICATION)
                setOnPreparedListener { it.start() }
                setOnCompletionListener { stopAudio() }
                setOnErrorListener { _, _, _ -> stopAudio(); false }
                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play audio: $uriStr", e)
        }
    }

    private fun stopAudio() {
        mediaPlayer?.let {
            try { it.stop(); it.release() } catch (_: Exception) {}
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        
        // Release wake lock
        try {
            wakeLock?.let { wl ->
                if (wl.isHeld) {
                    wl.release()
                    Log.d(TAG, "Wake lock released")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release wake lock: ${e.message}")
        }
        
        // Make sure notification is gone
        if (isForeground) stopForeground(true)
        serviceJob.cancel()
        Log.d(TAG, "LowBatteryAlertService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Low Battery Alert Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("🔋 Low Battery Alert Active")
            .setContentText("Monitoring battery level for alerts.")
            .setSmallIcon(R.drawable._44)
            .build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Ensure service restarts if the task is swiped away
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, LowBatteryAlertService::class.java))
        } else {
            startService(Intent(this, LowBatteryAlertService::class.java))
        }
    }

    companion object {
        private const val TAG = "LowBatteryAlertService"
        private const val NOTIFICATION_ID = 1338
        private const val NOTIFICATION_CHANNEL_ID = "LowBatteryAlertServiceChannel"
        const val ACTION_START_LOW_BATTERY_ALERT = "com.bleelblep.glyphsharge.START_LOW_BATTERY_ALERT"
        const val ACTION_STOP_LOW_BATTERY_ALERT = "com.bleelblep.glyphsharge.STOP_LOW_BATTERY_ALERT"
        const val ACTION_TEST_ALERT = "com.bleelblep.glyphsharge.TEST_LOW_BATTERY_ALERT"
    }
}
