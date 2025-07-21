package com.bleelblep.glyphsharge.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import com.bleelblep.glyphsharge.data.repository.ChargingSessionRepository
import com.bleelblep.glyphsharge.services.ChargeTrackerService
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Ensures a charging session is open after a device reboot if the phone was
 * already plugged in when BOOT_COMPLETED is broadcast.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: ChargingSessionRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        Log.d("BootReceiver", "BOOT_COMPLETED received")

        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val isPlugged = plugged != 0

        val monitorNeeded = settingsRepository.isBatteryStoryEnabled()

        // Start the main persistent service if the master switch is on
        if (settingsRepository.getGlyphServiceEnabled()) {
            Log.d("BootReceiver", "Glyph master switch is ON - starting persistent foreground service")
            val persistentSvcIntent = Intent(context, com.bleelblep.glyphsharge.services.GlyphForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(persistentSvcIntent)
            } else {
                context.startService(persistentSvcIntent)
            }
        }

        // Start PowerPeek foreground service if it was enabled before reboot
        if (settingsRepository.isPowerPeekEnabled()) {
            Log.d("BootReceiver", "PowerPeek enabled – starting service at boot")
            val powerPeekIntent = Intent(context, com.bleelblep.glyphsharge.services.PowerPeekService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(powerPeekIntent)
            } else {
                context.startService(powerPeekIntent)
            }
        }

        // Start Glyph Guard service if enabled and master Glyph Service is on
        if (settingsRepository.isGlyphGuardEnabled() && settingsRepository.getGlyphServiceEnabled()) {
            Log.d("BootReceiver", "Glyph Guard enabled – starting service at boot")
            val guardIntent = Intent(context, com.bleelblep.glyphsharge.services.GlyphGuardService::class.java).apply {
                action = com.bleelblep.glyphsharge.services.GlyphGuardService.ACTION_START_GLYPH_GUARD
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(guardIntent)
            } else {
                context.startService(guardIntent)
            }
        }

        // Start Low Battery Alert service if enabled and master Glyph Service is on
        if (settingsRepository.isLowBatteryEnabled() && settingsRepository.getGlyphServiceEnabled()) {
            Log.d("BootReceiver", "Low Battery Alert enabled – starting service at boot")
            val lowBatteryIntent = Intent(context, com.bleelblep.glyphsharge.services.LowBatteryAlertService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(lowBatteryIntent)
            } else {
                context.startService(lowBatteryIntent)
            }
        }

        // Start Quiet Hours service if enabled
        if (settingsRepository.isQuietHoursEnabled()) {
            Log.d("BootReceiver", "Quiet Hours enabled – starting service at boot")
            val quietHoursIntent = Intent(context, com.bleelblep.glyphsharge.services.QuietHoursService::class.java).apply {
                action = com.bleelblep.glyphsharge.services.QuietHoursService.ACTION_START_QUIET_HOURS
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(quietHoursIntent)
            } else {
                context.startService(quietHoursIntent)
            }
        }

        if (monitorNeeded) {
            val svcIntent = Intent(context, com.bleelblep.glyphsharge.services.ChargeTrackerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(svcIntent)
            } else {
                context.startService(svcIntent)
            }
        }

        if (isCharging && isPlugged && settingsRepository.isBatteryStoryEnabled()) {
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val pct = if (level >= 0 && scale > 0) level * 100 / scale else 0
            val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
            val tempC = tempTenths / 10f

            scope.launch {
                Log.d("BootReceiver", "Opening session at boot: $pct% $tempC°C")
                repository.startSession(pct, tempC)
            }
        } else {
            Log.d("BootReceiver", "Device not charging at boot – nothing to do")
        }
    }
} 