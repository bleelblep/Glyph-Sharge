package com.bleelblep.glyphsharge.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.BatteryManager
import androidx.core.app.NotificationCompat
import com.bleelblep.glyphsharge.R
import com.bleelblep.glyphsharge.data.repository.ChargingSessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import javax.inject.Inject
import android.util.Log
import kotlinx.coroutines.SupervisorJob
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository

/**
 * Foreground service that samples BATTERY_CHANGED intents while the device is
 * plugged in and updates the ongoing charging session.
 */
@AndroidEntryPoint
class ChargeTrackerService : Service() {

    @Inject
    lateinit var repository: ChargingSessionRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val TAG = "ChargeTracker"
    private var isServiceActive = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)



    data class BatteryState(
        val percentage: Int,
        val temperatureC: Float,
        val isCharging: Boolean,
        val isFull: Boolean
    )

    private fun queryBatteryState(context: Context): BatteryState {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)?.toFloat()?.div(10f) ?: 0f
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
        val isFull = status == BatteryManager.BATTERY_STATUS_FULL

        val batteryPct = if (level != -1 && scale != -1) {
            level * 100 / scale.toFloat()
        } else {
            0f
        }

        return BatteryState(
            percentage = batteryPct.toInt(),
            temperatureC = temperature,
            isCharging = isCharging,
            isFull = isFull
        )
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                val batteryInfo = queryBatteryState(context)
                
                scope.launch {
                    when {
                        batteryInfo.isCharging -> {
                            // Start new session if not already charging
                            val openSession = repository.getOpenSession()
                            if (openSession == null) {
                                repository.startSession(batteryInfo.percentage, batteryInfo.temperatureC)
                                Log.d(TAG, "Started new charging session at ${batteryInfo.percentage}%")
                            } else {
                                repository.updateOngoingSession(batteryInfo.percentage, batteryInfo.temperatureC)
                            }
                        }
                        batteryInfo.isFull -> {
                            // Finish session when battery is full
                            repository.finishSession(batteryInfo.percentage, batteryInfo.temperatureC)
                            Log.d(TAG, "Finished charging session at ${batteryInfo.percentage}% (full)")
                        }
                        else -> {
                            // Finish session when disconnected
                            repository.finishSession(batteryInfo.percentage, batteryInfo.temperatureC)
                            Log.d(TAG, "Finished charging session at ${batteryInfo.percentage}% (disconnected)")
                        }
                    }


                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isServiceActive = true
        Log.d(TAG, "Service created")
        if (!settingsRepository.isBatteryStoryEnabled()) {
            Log.d(TAG, "Battery Story disabled – stopping ChargeTrackerService")
            stopSelf()
            return
        }
        try {
            registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            Log.d(TAG, "Battery receiver registered")
            startForeground(NOTIF_ID, buildNotification())
            Log.d("ChargeTrackerService", "Started as foreground service")
        } catch (e: Exception) {
            Log.e(TAG, "Error registering battery receiver", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        return START_STICKY
    }

    override fun onDestroy() {
        isServiceActive = false
        try {
            unregisterReceiver(batteryReceiver)
            Log.d(TAG, "Battery receiver unregistered")
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering battery receiver", e)
        }
        scope.cancel()
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val channelId = "charge_tracker"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(channelId) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(
                        channelId,
                        "Charge Tracker",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply {
                        description = "Tracks battery charging sessions"
                        enableLights(false)
                        enableVibration(false)
                        setShowBadge(false)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            setAllowBubbles(false)
                        }
                    }
                )
            }
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("🔋 Battery Story Active")
            .setContentText("Monitoring battery level and temperature")
            .setSmallIcon(R.drawable._44)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .build()
    }

    companion object {
        const val NOTIF_ID = 42
    }


} 