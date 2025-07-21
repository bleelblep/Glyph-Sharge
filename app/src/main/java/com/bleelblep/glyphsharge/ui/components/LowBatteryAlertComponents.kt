package com.bleelblep.glyphsharge.ui.components

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.activity.compose.rememberLauncherForActivityResult
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import com.bleelblep.glyphsharge.ui.theme.AppThemeStyle
import com.bleelblep.glyphsharge.ui.theme.LocalThemeState
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository
import com.bleelblep.glyphsharge.ui.utils.HapticUtils
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.bleelblep.glyphsharge.glyph.PulseLockAnimations
import com.bleelblep.glyphsharge.glyph.GlyphAnimationManager
import android.util.Log
import com.bleelblep.glyphsharge.di.GlyphComponent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

/**
 * Low Battery Alert Configuration Data Class
 */
data class LowBatteryAlertConfig(
    val isEnabled: Boolean = false,
    val threshold: Int = 20,
    val animationId: String = "PULSE",
    val audioEnabled: Boolean = false,
    val audioUri: String? = null,
    val audioOffset: Long = 0L
)

/**
 * Redesigned Low Battery Alert Confirmation Dialog with improved layout and button placement
 */
@Composable
fun LowBatteryAlertConfirmationDialog(
    onTestAlert: () -> Unit,
    onEnableAlert: () -> Unit,
    onDisableAlert: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (LowBatteryAlertConfig) -> Unit,
    modifier: Modifier = Modifier,
    settingsRepository: SettingsRepository
) {
    var showEnableDialog by remember { mutableStateOf(false) }
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    // Only show confirmation dialog when settings dialog is NOT open
    if (!showEnableDialog) {
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
                    text = "🔋 Low Battery Alert",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Alert when battery is low",
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
                            text = "• Monitors battery level continuously\n• Triggers glyph animation when level drops below threshold\n• Works even when screen is off\n• Customizable threshold and animation",
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
                // Primary Action - Test Alert
                ElevatedButton(
                    onClick = { 
                        HapticUtils.triggerMediumFeedback(haptic, context)
                        onTestAlert() 
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
                            text = "🧪 Test Low Alert",
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
                    // Settings Button
                    Button(
                        onClick = { 
                            HapticUtils.triggerLightFeedback(haptic, context)
                            showEnableDialog = true
                            // Note: Don't call onDismiss here - let the settings dialog handle dismissal
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
        shape = RoundedCornerShape(24.dp)
    )
    }
    
    // Settings dialog
    if (showEnableDialog) {
        LowBatteryAlertEnableDialog(
            onConfirm = { config ->
                // Pass config to calling code to handle saving and enabled state update
                onConfirm(config)
                showEnableDialog = false
                onDismiss()  // This dismisses the confirmation dialog
            },
            onDisable = {
                onDisableAlert()
                showEnableDialog = false
                onDismiss()  // This dismisses the confirmation dialog too
            },
            onDismiss = { showEnableDialog = false },
            settingsRepository = settingsRepository
        )
    }
}

/**
 * Redesigned Low Battery Alert Enable Dialog that follows Glow Gate structure
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LowBatteryAlertEnableDialog(
    onConfirm: (LowBatteryAlertConfig) -> Unit,
    onDismiss: () -> Unit,
    onDisable: () -> Unit,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    // Check if Low Battery Alert is currently enabled
    val currentlyEnabled = remember { settingsRepository.isLowBatteryEnabled() }
    var isSaving by remember { mutableStateOf(false) }

    // Initial preferences
    var threshold by remember { mutableStateOf(settingsRepository.getLowBatteryThreshold().toFloat()) }
    var selectedAnim by remember { mutableStateOf(PulseLockAnimations.getById(settingsRepository.getLowBatteryAnimationId())) }
    var soundEnabled by remember { mutableStateOf(settingsRepository.isLowBatteryAudioEnabled()) }
    var soundUri by remember { mutableStateOf(settingsRepository.getLowBatteryAudioUri()) }
    var soundOffset by remember { 
        mutableStateOf(
            // Ensure offset is not negative - reset to 0 if it was previously negative
            maxOf(0f, settingsRepository.getLowBatteryAudioOffset().toFloat())
        ) 
    }
    var animationDuration by remember { mutableStateOf(settingsRepository.getLowBatteryDuration().coerceIn(1000L, 10000L)) }
    
    // Get sound name for display
    val soundName = remember(soundUri) {
        soundUri?.let { uriString ->
            try {
                val uri = Uri.parse(uriString)
                
                // Try to get ringtone title first (works for system sounds)
                val ringtone = RingtoneManager.getRingtone(context, uri)
                val ringtoneTitle = ringtone?.getTitle(context)
                
                if (!ringtoneTitle.isNullOrEmpty()) {
                    ringtoneTitle
                } else {
                    // For custom files, try to get filename from URI
                    uri.lastPathSegment?.let { segment ->
                        // Remove file extension and decode
                        segment.substringBeforeLast('.').replace("%20", " ")
                    } ?: "Custom audio file"
                }
            } catch (e: Exception) {
                Log.w("LowBatteryAlertComponents", "Error getting sound name for URI: $uriString", e)
                "Custom audio file"
            }
        } ?: "No sound selected"
    }

    // Ringtone picker (system sounds)
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == ComponentActivity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            soundUri = uri?.toString()
            Log.d("LowBatteryAlertComponents", "Selected ringtone URI: $soundUri")
        }
    }
    
    // Custom file picker
    val customFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                Log.d("LowBatteryAlertComponents", "Processing custom file: $uri")
                
                // Copy the file to internal storage to avoid permission issues
                val internalUri = copyFileToInternalStorage(context, uri, "lowbattery_custom_audio")
                
                if (internalUri != null) {
                    soundUri = internalUri.toString()
                    Log.d("LowBatteryAlertComponents", "Successfully copied custom file to internal storage: $internalUri")
                } else {
                    // Fallback: try to use original URI with persistable permission
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            uri, 
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                        soundUri = uri.toString()
                        Log.d("LowBatteryAlertComponents", "Fallback: using original URI with persistable permission")
                    } catch (e: Exception) {
                        Log.e("LowBatteryAlertComponents", "Failed to copy file and take persistable permission", e)
                        // Last resort: save original URI anyway
                        soundUri = uri.toString()
                    }
                }
            } catch (e: Exception) {
                Log.e("LowBatteryAlertComponents", "Error processing custom file: $uri", e)
            }
        }
    }

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
                    text = "Customize low battery alert",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp) // Limit max height to ensure dialog doesn't get too tall
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Battery Threshold Card (unique to Low Battery Alert)
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
                        Text(
                            text = "🔋 Battery Threshold",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Alert when battery drops to:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                color = when (themeState.themeStyle) {
                                    AppThemeStyle.AMOLED -> Color(0xFF2D2D2D)
                                    AppThemeStyle.CLASSIC -> Color(0xFFE8DEF8)
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${threshold.toInt()}%",
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
                            value = threshold,
                            onValueChange = {
                                HapticUtils.triggerLightFeedback(haptic, context)
                                threshold = it.coerceIn(5f, 50f)
                            },
                            valueRange = 5f..50f,
                            steps = 45,
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
                                text = "Low (5%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "High (50%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Animation Picker Card (same as Glow Gate)
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
                        // Get GlyphAnimationManager via dependency injection
                        val glyphAnimationManager = remember {
                            EntryPointAccessors.fromApplication(
                                context.applicationContext,
                                GlyphComponent::class.java
                            ).glyphAnimationManager()
                        }
                        val coroutineScope = rememberCoroutineScope()
                        
                        Text(
                            text = "🎞️ Animation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            PulseLockAnimations.list.forEach { anim ->
                                FilterChip(
                                    selected = anim == selectedAnim,
                                    onClick = {
                                        HapticUtils.triggerLightFeedback(haptic, context)
                                        selectedAnim = anim
                                    },
                                    label = { Text(anim.displayName) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                        
                        // Test button for selected animation
                        Button(
                            onClick = {
                                HapticUtils.triggerMediumFeedback(haptic, context)
                                // Launch animation test in background
                                coroutineScope.launch {
                                    try {
                                        when (selectedAnim.id) {
                                            "SPIRAL" -> glyphAnimationManager.runSpiralAnimation()
                                            "HEARTBEAT" -> glyphAnimationManager.runHeartbeatAnimation()
                                            "MATRIX" -> glyphAnimationManager.runMatrixRainAnimation()
                                            "FIREWORKS" -> glyphAnimationManager.runFireworksAnimation()
                                            "DNA" -> glyphAnimationManager.runDNAHelixAnimation()
                                            else -> glyphAnimationManager.playLowBatteryAnimation(selectedAnim.id)
                                        }
                                    } catch (e: Exception) {
                                        Log.e("LowBatteryAlertComponents", "Error testing animation: ${e.message}")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (themeState.themeStyle) {
                                    AppThemeStyle.AMOLED -> Color(0xFF2D2D2D)
                                    AppThemeStyle.CLASSIC -> Color(0xFFE8E1F5)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                contentColor = when (themeState.themeStyle) {
                                    AppThemeStyle.AMOLED -> Color.White
                                    AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🧪 Test \"${selectedAnim.displayName}\"",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Animation Duration Card (same as Glow Gate)
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
                        Text(
                            text = "⏱️ Animation Duration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Duration:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                color = when (themeState.themeStyle) {
                                    AppThemeStyle.AMOLED -> Color(0xFF2D2D2D)
                                    AppThemeStyle.CLASSIC -> Color(0xFFE8DEF8)
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${(animationDuration / 1000f).toInt()}s",
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
                            value = animationDuration.toFloat(),
                            onValueChange = {
                                HapticUtils.triggerLightFeedback(haptic, context)
                                animationDuration = it.toLong()
                                settingsRepository.saveLowBatteryDuration(animationDuration)
                            },
                            valueRange = 1000f..10000f,
                            steps = 18 - 1, // 0.5s increments between 1s and 10s
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
                                text = "Short (1s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Long (10s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Sync with sound duration button (only show when sound is enabled and a sound is selected)
                        if (soundEnabled && soundUri != null) {
                            Button(
                                onClick = {
                                    HapticUtils.triggerMediumFeedback(haptic, context)
                                    try {
                                        val mediaPlayer = android.media.MediaPlayer()
                                        mediaPlayer.setDataSource(context, Uri.parse(soundUri))
                                        mediaPlayer.prepare()
                                        val soundDuration = mediaPlayer.duration.toLong()
                                        mediaPlayer.release()
                                        val newDuration = soundDuration.coerceIn(1000L, 10000L)
                                        animationDuration = newDuration
                                        settingsRepository.saveLowBatteryDuration(newDuration)
                                    } catch (e: Exception) {
                                        Log.e("LowBatteryAlertComponents", "Error getting sound duration: ${e.message}")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when (themeState.themeStyle) {
                                        AppThemeStyle.AMOLED -> Color(0xFF2D2D2D)
                                        AppThemeStyle.CLASSIC -> Color(0xFFE8E1F5)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    contentColor = when (themeState.themeStyle) {
                                        AppThemeStyle.AMOLED -> Color.White
                                        AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🎵 Sync with Sound Duration",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Sound Alerts Card (same as Glow Gate)
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
                        // Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔊 Sound Alerts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = { 
                                    HapticUtils.triggerLightFeedback(haptic, context)
                                    soundEnabled = it 
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = when (themeState.themeStyle) {
                                        AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                                        AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    checkedTrackColor = when (themeState.themeStyle) {
                                        AppThemeStyle.AMOLED -> Color(0xFF4CAF50).copy(alpha = 0.5f)
                                        AppThemeStyle.CLASSIC -> Color(0xFF674FA3).copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    }
                                )
                            )
                        }

                        if (soundEnabled) {
                            // Current sound name
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Selected Sound:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = soundName,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        HapticUtils.triggerLightFeedback(haptic, context)
                                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alert Sound")
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                                            soundUri?.let { uriStr -> putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(uriStr)) }
                                        }
                                        ringtonePickerLauncher.launch(intent)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when (themeState.themeStyle) {
                                            AppThemeStyle.AMOLED -> Color(0xFF2D2D2D)
                                            AppThemeStyle.CLASSIC -> Color(0xFFE8E1F5)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        contentColor = when (themeState.themeStyle) {
                                            AppThemeStyle.AMOLED -> Color.White
                                            AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("🎵 System", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = { 
                                        HapticUtils.triggerLightFeedback(haptic, context)
                                        customFilePickerLauncher.launch("audio/*") 
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when (themeState.themeStyle) {
                                            AppThemeStyle.AMOLED -> Color(0xFF2D2D2D)
                                            AppThemeStyle.CLASSIC -> Color(0xFFE8E1F5)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        contentColor = when (themeState.themeStyle) {
                                            AppThemeStyle.AMOLED -> Color.White
                                            AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("📁 Custom", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }

                            // Offset slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Audio Offset:")
                                Text("${soundOffset.toInt()}ms")
                            }
                            Slider(
                                value = soundOffset,
                                onValueChange = { 
                                    HapticUtils.triggerLightFeedback(haptic, context)
                                    soundOffset = it 
                                },
                                valueRange = 0f..1500f,
                                steps = 29,
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(
                                    thumbColor = when (themeState.themeStyle) {
                                        AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                                        AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val primaryColor = when (themeState.themeStyle) {
                AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                else -> MaterialTheme.colorScheme.primary
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ElevatedButton(
                    onClick = {
                        isSaving = true
                        HapticUtils.triggerMediumFeedback(haptic, context)
                        // Create config and let calling code handle saving
                        val config = LowBatteryAlertConfig(
                            isEnabled = true,
                            threshold = threshold.toInt(),
                            animationId = selectedAnim.id,
                            audioEnabled = soundEnabled,
                            audioUri = soundUri,
                            audioOffset = soundOffset.toLong()
                        )
                        onConfirm(config)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.elevatedButtonColors(containerColor = primaryColor, contentColor = Color.White),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp, pressedElevation = 12.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            text = if (currentlyEnabled) "💾 Save Settings" else "⚡ Enable Low Alert",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            onDisable()
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
                        Text("✖️ Disable", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { 
                            HapticUtils.triggerLightFeedback(haptic, context)
                            onDismiss() 
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("✕ Cancel", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
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
 * Copy a file from external URI to internal storage to avoid permission issues
 */
private fun copyFileToInternalStorage(context: android.content.Context, sourceUri: Uri, fileName: String): Uri? {
    return try {
        val inputStream: java.io.InputStream? = context.contentResolver.openInputStream(sourceUri)
        if (inputStream == null) {
            return null
        }
        
        // Get file extension from original URI
        val originalFileName = sourceUri.lastPathSegment ?: "audio"
        val extension = when {
            originalFileName.contains(".mp3", ignoreCase = true) -> ".mp3"
            originalFileName.contains(".wav", ignoreCase = true) -> ".wav"
            originalFileName.contains(".m4a", ignoreCase = true) -> ".m4a"
            originalFileName.contains(".aac", ignoreCase = true) -> ".aac"
            originalFileName.contains(".ogg", ignoreCase = true) -> ".ogg"
            else -> ".mp3" // default
        }
        
        // Create file in internal storage
        val internalFile = java.io.File(context.filesDir, "$fileName$extension")
        
        // Copy the file
        inputStream.use { input ->
            java.io.FileOutputStream(internalFile).use { output ->
                input.copyTo(output)
            }
        }
        
        // Return file:// URI for internal storage
        Uri.fromFile(internalFile)
        
    } catch (e: Exception) {
        null
    }
} 