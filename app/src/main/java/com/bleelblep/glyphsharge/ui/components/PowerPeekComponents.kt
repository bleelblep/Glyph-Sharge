package com.bleelblep.glyphsharge.ui.components

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
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalHapticFeedback
import com.bleelblep.glyphsharge.ui.theme.AppThemeStyle
import com.bleelblep.glyphsharge.ui.theme.LocalThemeState
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository
import com.bleelblep.glyphsharge.ui.utils.HapticUtils
import kotlinx.coroutines.delay
import kotlin.math.sqrt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.style.TextOverflow
import kotlin.math.abs
import kotlin.math.roundToInt

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
 * Redesigned PowerPeek Confirmation Dialog with improved layout and button placement
 */
@Composable
fun PowerPeekConfirmationDialog(
    onTestPowerPeek: () -> Unit,
    onEnablePowerPeek: () -> Unit,
    onDisablePowerPeek: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    settingsRepository: SettingsRepository
) {
    var showEnableDialog by remember { mutableStateOf(false) }
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = { /* Prevent dismissal */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚡ PowerPeek",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Battery peek on shake",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Feature description
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) {
                                MaterialTheme.colorScheme.surfaceContainer
                            } else {
                                Color(0xFFF8F5FF)
                            }
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📱 How it works:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Shake your device to view battery percentage\n• Works even when screen is off\n• Customizable sensitivity and display duration",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary Action - Test PowerPeek
                ElevatedButton(
                    onClick = { 
                        HapticUtils.triggerMediumFeedback(haptic, context)
                        onTestPowerPeek() 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                            AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🧪 Test Power Peek",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Secondary Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Enable PowerPeek Button
                    Button(
                        onClick = { 
                            HapticUtils.triggerLightFeedback(haptic, context)
                            showEnableDialog = true 
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color(0xFF2D4A3E)
                                AppThemeStyle.CLASSIC -> Color(0xFF8D7BA5)
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            },
                            contentColor = when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color.White
                                AppThemeStyle.CLASSIC -> Color.White
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "⚙️ Settings",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Cancel Button
                    OutlinedButton(
                        onClick = { 
                            HapticUtils.triggerLightFeedback(haptic, context)
                            onDismiss() 
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "✕ Cancel",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
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
            },
            onDisable = {
                onDisablePowerPeek()
                showEnableDialog = false
                onDismiss()
            },
            settingsRepository = settingsRepository
        )
    }
}

/**
 * Redesigned PowerPeek Enable Dialog with modern layout and better controls
 */
@Composable
fun PowerPeekEnableDialog(
    onConfirm: (PowerPeekConfig) -> Unit,
    onDismiss: () -> Unit,
    onDisable: () -> Unit,
    modifier: Modifier = Modifier,
    settingsRepository: SettingsRepository
) {
    val initialThreshold = remember { settingsRepository.getShakeThreshold() }
    val initialDuration = remember { settingsRepository.getDisplayDuration() }

    var enableWhenScreenOff by remember { mutableStateOf(true) }
    var shakeThreshold by remember { mutableStateOf(initialThreshold) }
    var displayDuration by remember { mutableStateOf(initialDuration / 1000f) } // in seconds
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Configure",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Customize your shake detection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Sensitivity Control Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) {
                                MaterialTheme.colorScheme.surfaceContainer
                            } else {
                                Color(0xFFF8F5FF)
                            }
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📳 Shake Sensitivity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = when (themeState.themeStyle) {
                                    AppThemeStyle.AMOLED -> Color(0xFF2D2D2D)
                                    AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        Color(0xFFE8DEF8)
                                    }
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = settingsRepository.getShakeIntensityLevel(shakeThreshold),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (themeState.themeStyle) {
                                        AppThemeStyle.AMOLED -> Color.White
                                        AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                                    }
                                )
                            }
                        }

                        // Simple discrete slider with exactly three stops (0,1,2)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("Medium", "Hard", "Harder").forEach { label ->
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        // Backing value: 0-Medium, 1-Hard, 2-Harder
                        var sliderStep by remember {
                            mutableStateOf(
                                when (shakeThreshold) {
                                    SettingsRepository.SHAKE_HARD      -> 1f
                                    SettingsRepository.SHAKE_HARDEST   -> 2f
                                    else                              -> 0f
                                }
                            )
                        }

                        Slider(
                            value = sliderStep,
                            onValueChange = { raw ->
                                HapticUtils.triggerLightFeedback(haptic, context)
                                sliderStep = raw.coerceIn(0f,2f)
                            },
                            onValueChangeFinished = {
                                val snapped = sliderStep.roundToInt().toFloat()
                                sliderStep = snapped
                                shakeThreshold = when (snapped.toInt()) {
                                    1 -> SettingsRepository.SHAKE_HARD
                                    2 -> SettingsRepository.SHAKE_HARDEST
                                    else -> SettingsRepository.SHAKE_MEDIUM
                                }
                            },
                            valueRange = 0f..2f,
                            steps = 0, // continuous track, no tick marks
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = when (themeState.themeStyle) {
                                    AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                                    AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                activeTrackColor = when (themeState.themeStyle) {
                                    AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                                    AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        )
                    }
                }
                
                // Duration Control Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) {
                                MaterialTheme.colorScheme.surfaceContainer
                            } else {
                                Color(0xFFF8F5FF)
                            }
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏱️ Display Duration",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = when (themeState.themeStyle) {
                                    AppThemeStyle.AMOLED -> Color(0xFF2D2D2D)
                                    AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        Color(0xFFE8DEF8)
                                    }
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${displayDuration.toInt()}s",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (themeState.themeStyle) {
                                        AppThemeStyle.AMOLED -> Color.White
                                        AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                                    }
                                )
                            }
                        }
                        
                        Slider(
                            value = displayDuration,
                            onValueChange = { 
                                HapticUtils.triggerLightFeedback(haptic, context)
                                displayDuration = it 
                            },
                            valueRange = 2f..10f,
                            steps = 7,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = when (themeState.themeStyle) {
                                    AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                                    AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Quick",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Long",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary Enable Button
                var isSaving by remember { mutableStateOf(false) }
                val currentlyEnabled = remember { settingsRepository.isPowerPeekEnabled() }

                ElevatedButton(
                    onClick = {
                        HapticUtils.triggerMediumFeedback(haptic, context)
                        isSaving = true
                        val newDuration = (displayDuration * 1000).toLong()
                        settingsRepository.saveShakeThreshold(shakeThreshold)
                        settingsRepository.saveDisplayDuration(newDuration)

                        val config = PowerPeekConfig(
                            isEnabled = true,
                            shakeThreshold = shakeThreshold,
                            displayDuration = newDuration,
                            enableWhenScreenOff = enableWhenScreenOff
                        )
                        onConfirm(config)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                            AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = if (currentlyEnabled) "💾 Save Settings" else "✅ Enable Power Peek",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Secondary Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Disable Button
                    Button(
                        onClick = {
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            onDisable()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color(0xFFB71C1C)
                                AppThemeStyle.CLASSIC -> Color(0xFFD32F2F)
                                else -> MaterialTheme.colorScheme.error
                            },
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "✖️ Disable",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Cancel Button
                    OutlinedButton(
                        onClick = { 
                            HapticUtils.triggerLightFeedback(haptic, context)
                            onDismiss() 
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "✕ Cancel",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    )
}

/**
 * Battery Info Card - displays current battery status and PowerPeek state
 */
@Composable
private fun PowerPeekBatteryInfoCard(
    modifier: Modifier = Modifier,
    settingsRepository: SettingsRepository
) {
    val context = LocalContext.current
    var batteryInfo by remember { mutableStateOf<PowerPeekBatteryInfo?>(null) }
    val themeState = LocalThemeState.current
    
    // Get current PowerPeek settings
    val isPowerPeekEnabled = remember { settingsRepository.isPowerPeekEnabled() }
    val shakeThreshold = remember { settingsRepository.getShakeThreshold() }
    val displayDuration = remember { settingsRepository.getDisplayDuration() }
    
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
                
                batteryInfo = PowerPeekBatteryInfo(batteryPercentage, isCharging)
            }
        } catch (e: Exception) {
            Log.e("PowerPeekComponents", "Error getting battery info: ${e.message}")
        }
    }
    
    batteryInfo?.let { info ->
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (themeState.themeStyle) {
                    AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                    AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) {
                        MaterialTheme.colorScheme.surfaceContainer
                    } else {
                        Color(0xFFF3F0F7)
                    }
                    else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                }
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // PowerPeek Status Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ PowerPeek Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(
                        color = if (isPowerPeekEnabled) {
                            when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                                AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isPowerPeekEnabled) "✅ Enabled" else "❌ Disabled",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isPowerPeekEnabled) {
                                Color.White
                            } else {
                                MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                    }
                }
                
                // Current Settings (only show if enabled)
                if (isPowerPeekEnabled) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Sensitivity: ${shakeThreshold.toInt()}g",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Display: ${displayDuration / 1000}s",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Divider
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
                
                // Battery Info Section
                Column(
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
                        color = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color.White
                            AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                            AppThemeStyle.NEON -> Color(0xFF00FF00)
                            else -> MaterialTheme.colorScheme.primary
                        }
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
}

/**
 * Battery Info Data Class for PowerPeek
 */
data class PowerPeekBatteryInfo(
    val percentage: Int,
    val isCharging: Boolean
)

/**
 * PowerPeek Manager - handles shake detection and battery display
 */
class PowerPeekManager(
    private val context: Context,
    private val config: PowerPeekConfig,
    private val onBatteryDisplay: (PowerPeekBatteryInfo) -> Unit
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
                
                val batteryInfo = PowerPeekBatteryInfo(batteryPercentage, isCharging)
                onBatteryDisplay(batteryInfo)
                
                Log.d("PowerPeekManager", "Shake detected! Battery: ${batteryInfo.percentage}%, Charging: ${batteryInfo.isCharging}")
            }
        } catch (e: Exception) {
            Log.e("PowerPeekManager", "Error handling shake detection: ${e.message}")
        }
    }
} 