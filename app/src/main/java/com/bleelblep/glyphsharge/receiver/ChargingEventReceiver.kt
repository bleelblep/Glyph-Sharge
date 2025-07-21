package com.bleelblep.glyphsharge.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.bleelblep.glyphsharge.data.repository.ChargingSessionRepository
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository

@AndroidEntryPoint
class ChargingEventReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: ChargingSessionRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ChargeReceiver", "onReceive: action=${intent.action}")
        if (!settingsRepository.isBatteryStoryEnabled()) {
            Log.d("ChargeReceiver", "Battery Story disabled – ignoring charging event")
            return
        }
        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                Log.d("ChargeReceiver", "Power connected - starting session")
                val (pct, temp) = queryBatteryState(context)
                scope.launch {
                    repository.startSession(pct, temp)
                    Log.d("ChargeReceiver", "Session started: $pct% at ${temp}°C")
                }

                // Start foreground tracker service to keep session updated
                val svcIntent = Intent(context, com.bleelblep.glyphsharge.services.ChargeTrackerService::class.java)
                Log.d("ChargeReceiver", "Starting ChargeTrackerService")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(svcIntent)
                } else {
                    context.startService(svcIntent)
                }
                Log.d("ChargeReceiver", "ChargeTrackerService start requested")
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                Log.d("ChargeReceiver", "Power disconnected - finishing session")
                val (pct, temp) = queryBatteryState(context)
                scope.launch {
                    try {
                        repository.finishSession(pct, temp)
                        Log.d("ChargeReceiver", "Session finished: $pct% at ${temp}°C")
                    } catch (e: Exception) {
                        Log.e("ChargeReceiver", "Error finishing session: ${e.message}")
                    }
                }

                // Stop tracker service
                val svcIntent = Intent(context, com.bleelblep.glyphsharge.services.ChargeTrackerService::class.java)
                context.stopService(svcIntent)
                Log.d("ChargeReceiver", "ChargeTrackerService stop requested")
            }
        }
    }

    private fun queryBatteryState(context: Context): Pair<Int, Float> {
        val batteryIntent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val percentage = if (level >= 0 && scale > 0) (level * 100 / scale) else 0
        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val tempC = tempTenths / 10f
        return percentage to tempC
    }
} 