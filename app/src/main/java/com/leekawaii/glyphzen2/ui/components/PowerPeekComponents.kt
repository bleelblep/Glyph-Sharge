package com.leekawaii.glyphzen2.ui.components

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
import android.os.PowerManager
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlin.math.sqrt

/**
 * PowerPeek Configuration Data Class
 */
data class PowerPeekConfig(
    val isEnabled: Boolean = false,
    val shakeThreshold: Float = 12.0f,
    val displayDuration: Long = 3000L, // 3 seconds
    val enableWhenScreenOff: Boolean = false
)

/**
 * PowerPeek Confirmation Dialog with three buttons:
 * 1. Test PowerPeek
 * 2. Enable PowerPeek (screen off settings)
 * 3. Cancel
 */
@Composable
fun PowerPeekConfirmationDialog(
    onTestPowerPeek: () -> Unit,
    onEnablePowerPeek: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showEnableDialog by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = { /* Prevent dismissal */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        title = {
            Text(
                text = "PowerPeek Settings",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "PowerPeek shows your battery percentage when you shake your device. Choose an option below:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                // Battery info display (current status)
                BatteryInfoCard()
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Test PowerPeek Button
                Button(
                    onClick = onTestPowerPeek,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = "Test PowerPeek",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // Enable PowerPeek Button
                Button(
                    onClick = {
                        showEnableDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(
                        text = "Enable PowerPeek",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                // Cancel Button
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    )
    
    // Enable PowerPeek Settings Dialog
    if (showEnableDialog) {
        PowerPeekEnableDialog(
            onConfirm = { config ->
                onEnablePowerPeek()
                showEnableDialog = false
                onDismiss()
            },
            onDismiss = {
                showEnableDialog = false
            }
        )
    }
}

/**
 * PowerPeek Enable Dialog for configuring screen-off detection
 */
@Composable
fun PowerPeekEnableDialog(
    onConfirm: (PowerPeekConfig) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var enableWhenScreenOff by remember { mutableStateOf(true) }
    var shakeThreshold by remember { mutableStateOf(12.0f) }
    var displayDuration by remember { mutableStateOf(3.0f) } // in seconds
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Configure PowerPeek",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Configure when PowerPeek should activate:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Screen Off Setting
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable when screen is off",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Show battery when phone is wiggled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = enableWhenScreenOff,
                            onCheckedChange = { enableWhenScreenOff = it }
                        )
                    }
                }
                
                // Shake Sensitivity
                Text(
                    text = "Shake Sensitivity: ${shakeThreshold.toInt()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = shakeThreshold,
                    onValueChange = { shakeThreshold = it },
                    valueRange = 8f..20f,
                    steps = 11,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Display Duration
                Text(
                    text = "Display Duration: ${displayDuration.toInt()}s",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = displayDuration,
                    onValueChange = { displayDuration = it },
                    valueRange = 2f..10f,
                    steps = 7,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                
                Button(
                    onClick = {
                        val config = PowerPeekConfig(
                            isEnabled = true,
                            shakeThreshold = shakeThreshold,
                            displayDuration = (displayDuration * 1000).toLong(),
                            enableWhenScreenOff = enableWhenScreenOff
                        )
                        onConfirm(config)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Enable")
                }
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    )
}

/**
 * Battery Info Card - displays current battery status
 */
@Composable
private fun BatteryInfoCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var batteryInfo by remember { mutableStateOf<BatteryInfo?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryIntent = context.registerReceiver(null, intentFilter)
            
            if (batteryIntent != null) {
                val batteryLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val batteryScale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryStatus = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                               batteryStatus == BatteryManager.BATTERY_STATUS_FULL
                
                val batteryPercentage = if (batteryLevel != -1 && batteryScale != -1) {
                    (batteryLevel * 100 / batteryScale.toFloat()).toInt()
                } else {
                    50 // Default fallback
                }
                
                batteryInfo = BatteryInfo(batteryPercentage, isCharging)
            }
        } catch (e: Exception) {
            Log.e("PowerPeekComponents", "Error getting battery info: ${e.message}")
        }
    }
    
    batteryInfo?.let { info ->
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Current Battery",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "${info.percentage}%",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (info.isCharging) "⚡ Charging" else "🔋 Discharging",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Battery Info Data Class
 */
data class BatteryInfo(
    val percentage: Int,
    val isCharging: Boolean
)

/**
 * PowerPeek Manager - handles shake detection and battery display
 */
class PowerPeekManager(
    private val context: Context,
    private val config: PowerPeekConfig,
    private val onBatteryDisplay: (BatteryInfo) -> Unit
) : SensorEventListener {
    
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    
    private var lastUpdate = 0L
    private var isListening = false
    
    fun startListening() {
        if (!isListening && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            isListening = true
            Log.d("PowerPeekManager", "Started listening for shake events")
        }
    }
    
    fun stopListening() {
        if (isListening) {
            sensorManager.unregisterListener(this)
            isListening = false
            Log.d("PowerPeekManager", "Stopped listening for shake events")
        }
    }
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val currentTime = System.currentTimeMillis()
            
            // Throttle sensor events to prevent excessive processing
            if (currentTime - lastUpdate > 500) { // 500ms throttle
                lastUpdate = currentTime
                
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                
                // Calculate acceleration magnitude
                val acceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                
                // Check if shake threshold is exceeded
                if (acceleration > config.shakeThreshold) {
                    // Check if screen is off when required
                    val isScreenOff = !powerManager.isInteractive
                    
                    if (config.enableWhenScreenOff && isScreenOff || !config.enableWhenScreenOff) {
                        handleShakeDetected()
                    }
                }
            }
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }
    
    private fun handleShakeDetected() {
        try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryIntent = context.registerReceiver(null, intentFilter)
            
            if (batteryIntent != null) {
                val batteryLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val batteryScale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryStatus = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING ||
                               batteryStatus == BatteryManager.BATTERY_STATUS_FULL
                
                val batteryPercentage = if (batteryLevel != -1 && batteryScale != -1) {
                    (batteryLevel * 100 / batteryScale.toFloat()).toInt()
                } else {
                    50 // Default fallback
                }
                
                val batteryInfo = BatteryInfo(batteryPercentage, isCharging)
                onBatteryDisplay(batteryInfo)
                
                Log.d("PowerPeekManager", "Shake detected! Battery: ${batteryInfo.percentage}%, Charging: ${batteryInfo.isCharging}")
            }
        } catch (e: Exception) {
            Log.e("PowerPeekManager", "Error handling shake detection: ${e.message}")
        }
    }
} 