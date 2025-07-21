package com.bleelblep.glyphsharge.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.bleelblep.glyphsharge.R
import com.bleelblep.glyphsharge.di.GlyphComponent
import com.bleelblep.glyphsharge.glyph.GlyphAnimationManager
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sqrt
import com.bleelblep.glyphsharge.glyph.GlyphFeature

@AndroidEntryPoint
class PowerPeekService : Service(), SensorEventListener {

    @Inject
    lateinit var glyphAnimationManager: GlyphAnimationManager
    @Inject
    lateinit var settingsRepository: SettingsRepository
    @Inject
    lateinit var featureCoordinator: com.bleelblep.glyphsharge.glyph.GlyphFeatureCoordinator

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var powerManager: PowerManager? = null

    private var lastShakeTime = 0L
    private val shakeCooldown = 3000L // 3 seconds cooldown to prevent multiple triggers
    // Stored as raw m/s^2 in SharedPreferences (legacy). We convert to g-force on the fly.
    private var shakeThresholdMs2 = 15.0f

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    // Track whether we are currently in foreground (notification shown)
    private var isForeground = false

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    if (!settingsRepository.getGlyphServiceEnabled()) {
                        Log.d(TAG, "Glyph service disabled – ignoring Screen OFF")
                    } else {
                        Log.d(TAG, "Screen OFF detected - starting shake listening")
                        startListening()
                    }
                }
                Intent.ACTION_SCREEN_ON -> {
                    Log.d(TAG, "Screen ON detected - stopping shake listening")
                    stopListening()
                }
                else -> {
                    Log.d(TAG, "Unknown screen state action: ${intent?.action}")
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        shakeThresholdMs2 = settingsRepository.getShakeThreshold()

        // Register screen state receiver
        val screenFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenStateReceiver, screenFilter)
        Log.d(TAG, "PowerPeekService created and screen receiver registered.")

        // Promote to foreground immediately to comply with Android 14 FGS start rule
        ensureForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "PowerPeekService started")

        // Update settings on each start command, in case they were changed
        shakeThresholdMs2 = settingsRepository.getShakeThreshold()

        // If the screen is already off when the service starts, begin listening.
        if (powerManager?.isInteractive == false) {
            startListening()
        }

        return START_STICKY
    }

    private fun startListening() {
        Log.d(TAG, "startListening() called - screen interactive: ${powerManager?.isInteractive}")
        if (!settingsRepository.getGlyphServiceEnabled()) {
            Log.d(TAG, "Glyph service disabled – not registering shake listener")
            return
        }

        if (accelerometer != null) {
            ensureForeground()
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Started listening for shake events.")
        }
    }

    private fun stopListening() {
        Log.d(TAG, "stopListening() called")
        sensorManager.unregisterListener(this)
        Log.d(TAG, "Stopped listening for shake events.")

        // We no longer need to stay in foreground once listening stops
        if (isForeground) {
            stopForeground(true)
            isForeground = false
        }
    }

    private fun ensureForeground() {
        if (!isForeground) {
            startForeground(NOTIFICATION_ID, createNotification())
            isForeground = true
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Raw acceleration magnitude (m/s^2) excluding gravity baseline
            val accelerationMagnitude = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH

            // Always fetch the latest threshold so slider changes apply instantly
            val thresholdMs2 = settingsRepository.getShakeThreshold()

            if (accelerationMagnitude > thresholdMs2) {
                Log.d(TAG, "Shake detected! accel=$accelerationMagnitude ms², threshold=$thresholdMs2 ms²")
                triggerBatteryVisualization()
            }
        }
    }

    private fun triggerBatteryVisualization() {
        // This is the single source of truth for the cooldown.
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShakeTime < shakeCooldown) {
            Log.d(TAG, "Trigger blocked by cooldown.")
            return
        }
        lastShakeTime = currentTime

        // Check if currently in quiet hours
        if (settingsRepository.isCurrentlyInQuietHours()) {
            Log.d(TAG, "PowerPeek trigger blocked by quiet hours")
            return
        }

        // Ensure a Glyph session is available – on first-time activation the user may have just
        // enabled the service and the async session callback hasn't fired yet.  Attempt to
        // establish it now so the upcoming animation has a valid builder.
        if (!glyphAnimationManager.ensureSessionActive()) {
            Log.w(TAG, "Could not establish Glyph session; aborting visualization trigger")
            return
        }

        serviceScope.launch {
            if (!featureCoordinator.acquire(GlyphFeature.POWER_PEEK)) {
                Log.d(TAG, "Power Peek skipped – LEDs currently in use by ${featureCoordinator.currentOwner.value}")
                return@launch
            }

            try {
                Log.d(TAG, "Running battery visualization...")
                val duration = settingsRepository.getDisplayDuration()
                glyphAnimationManager.runBatteryPercentageVisualization(applicationContext, duration) {}
            } catch (e: Exception) {
                Log.e(TAG, "Error running battery visualization from service", e)
            } finally {
                featureCoordinator.release(GlyphFeature.POWER_PEEK)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    override fun onDestroy() {
        super.onDestroy()
        stopListening()
        unregisterReceiver(screenStateReceiver)
        // Make sure notification is gone
        if (isForeground) stopForeground(true)
        serviceJob.cancel()
        Log.d(TAG, "PowerPeekService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "PowerPeek Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("📲 Power Peek Active")
            .setContentText("Shake to see battery when screen is off.")
            .setSmallIcon(R.drawable._44)
            .build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Ensure service restarts if the task is swiped away
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, PowerPeekService::class.java))
        } else {
            startService(Intent(this, PowerPeekService::class.java))
        }
    }

    companion object {
        private const val TAG = "PowerPeekService"
        private const val NOTIFICATION_ID = 1337
        private const val NOTIFICATION_CHANNEL_ID = "PowerPeekServiceChannel"
    }
} 