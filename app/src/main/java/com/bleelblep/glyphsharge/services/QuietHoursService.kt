package com.bleelblep.glyphsharge.services

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bleelblep.glyphsharge.R
import com.bleelblep.glyphsharge.glyph.GlyphManager
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class QuietHoursService : Service() {
    companion object {
        private const val TAG = "QuietHoursService"
        private const val NOTIFICATION_ID = 1004
        private const val NOTIFICATION_CHANNEL_ID = "QuietHoursServiceChannel"
        const val ACTION_START_QUIET_HOURS = "com.bleelblep.glyphsharge.START_QUIET_HOURS"
        const val ACTION_STOP_QUIET_HOURS = "com.bleelblep.glyphsharge.STOP_QUIET_HOURS"
        const val ACTION_QUIET_HOURS_START = "com.bleelblep.glyphsharge.QUIET_HOURS_START"
        const val ACTION_QUIET_HOURS_END = "com.bleelblep.glyphsharge.QUIET_HOURS_END"
    }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var glyphManager: GlyphManager

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var quietHoursActive = false
    private var alarmManager: AlarmManager? = null
    private var startPendingIntent: PendingIntent? = null
    private var endPendingIntent: PendingIntent? = null

    private val quietHoursReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_QUIET_HOURS_START -> {
                    Log.d(TAG, "🕐 Quiet hours starting")
                    startQuietHours()
                }
                ACTION_QUIET_HOURS_END -> {
                    Log.d(TAG, "🌅 Quiet hours ending")
                    endQuietHours()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "QuietHoursService created")
        
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Register broadcast receiver for quiet hours start/end
        val filter = IntentFilter().apply {
            addAction(ACTION_QUIET_HOURS_START)
            addAction(ACTION_QUIET_HOURS_END)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(quietHoursReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(quietHoursReceiver, filter)
        }
        
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "QuietHoursService onStartCommand: ${intent?.action}")
        
        // Always become foreground first to satisfy Android's requirement
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Then handle specific actions
        when (intent?.action) {
            ACTION_START_QUIET_HOURS -> {
                scheduleQuietHours()
                checkCurrentQuietHoursState()
            }
            ACTION_STOP_QUIET_HOURS -> {
                stopQuietHours()
                stopSelf()
            }
            null -> {
                // Handle case when MainActivity starts us without specific action
                scheduleQuietHours()
                checkCurrentQuietHoursState()
            }
        }
        
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "QuietHoursService destroyed")
        
        try {
            unregisterReceiver(quietHoursReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "Error unregistering receiver: ${e.message}")
        }
        
        cancelScheduledAlarms()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Quiet Hours Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Manages quiet hours for glyph animations"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val status = if (quietHoursActive) "Active" else "Monitoring"
        val timeRange = "${settingsRepository.getQuietHoursStartHour()}:${String.format("%02d", settingsRepository.getQuietHoursStartMinute())} - " +
                       "${settingsRepository.getQuietHoursEndHour()}:${String.format("%02d", settingsRepository.getQuietHoursEndMinute())}"
        
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Quiet Hours")
            .setContentText("$status • $timeRange")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun scheduleQuietHours() {
        if (!settingsRepository.isQuietHoursEnabled()) {
            Log.d(TAG, "Quiet hours disabled, not scheduling")
            return
        }

        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        // Schedule start time
        val startCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, settingsRepository.getQuietHoursStartHour())
            set(Calendar.MINUTE, settingsRepository.getQuietHoursStartMinute())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // If start time has passed today, schedule for tomorrow
        if (startCalendar.timeInMillis <= now) {
            startCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Schedule end time
        val endCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, settingsRepository.getQuietHoursEndHour())
            set(Calendar.MINUTE, settingsRepository.getQuietHoursEndMinute())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        // If end time has passed today, schedule for tomorrow
        if (endCalendar.timeInMillis <= now) {
            endCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Create pending intents
        val startIntent = Intent(this, QuietHoursService::class.java).apply {
            action = ACTION_QUIET_HOURS_START
        }
        val endIntent = Intent(this, QuietHoursService::class.java).apply {
            action = ACTION_QUIET_HOURS_END
        }

        startPendingIntent = PendingIntent.getService(
            this, 0, startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        endPendingIntent = PendingIntent.getService(
            this, 1, endIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule alarms
        alarmManager?.setRepeating(
            AlarmManager.RTC_WAKEUP,
            startCalendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            startPendingIntent!!
        )
        alarmManager?.setRepeating(
            AlarmManager.RTC_WAKEUP,
            endCalendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            endPendingIntent!!
        )

        Log.d(TAG, "Scheduled quiet hours: ${startCalendar.time} to ${endCalendar.time}")
    }

    private fun cancelScheduledAlarms() {
        startPendingIntent?.let { alarmManager?.cancel(it) }
        endPendingIntent?.let { alarmManager?.cancel(it) }
        Log.d(TAG, "Cancelled scheduled quiet hours alarms")
    }

    private fun checkCurrentQuietHoursState() {
        val shouldBeActive = settingsRepository.isCurrentlyInQuietHours()
        if (shouldBeActive && !quietHoursActive) {
            startQuietHours()
        } else if (!shouldBeActive && quietHoursActive) {
            endQuietHours()
        }
    }

    private fun startQuietHours() {
        if (quietHoursActive) return
        
        Log.d(TAG, "🕐 Starting quiet hours - disabling glyph services")
        quietHoursActive = true
        
        // Disable glyph services during quiet hours
        serviceScope.launch {
            try {
                // Turn off all glyphs
                glyphManager.turnOffAll()
                
                // Disable glyph service if it's currently active
                if (glyphManager.isSessionActive) {
                    glyphManager.cleanup()
                    Log.d(TAG, "Glyph service disabled for quiet hours")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting quiet hours: ${e.message}")
            }
        }
        
        updateNotification()
    }

    private fun endQuietHours() {
        if (!quietHoursActive) return
        
        Log.d(TAG, "🌅 Ending quiet hours - re-enabling glyph services")
        quietHoursActive = false
        
        // Re-enable glyph services after quiet hours
        serviceScope.launch {
            try {
                // Re-initialize glyph manager if needed
                if (!glyphManager.isSessionActive && settingsRepository.getGlyphServiceEnabled()) {
                    glyphManager.initialize()
                    glyphManager.openSession()
                    Log.d(TAG, "Glyph service re-enabled after quiet hours")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error ending quiet hours: ${e.message}")
            }
        }
        
        updateNotification()
    }

    private fun stopQuietHours() {
        Log.d(TAG, "Stopping quiet hours service")
        cancelScheduledAlarms()
        
        if (quietHoursActive) {
            endQuietHours()
        }
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }
} 