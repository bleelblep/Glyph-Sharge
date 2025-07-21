package com.bleelblep.glyphsharge.ui.components

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalHapticFeedback
import com.bleelblep.glyphsharge.glyph.PulseLockAnimations
import com.bleelblep.glyphsharge.glyph.GlyphAnimationManager
import com.bleelblep.glyphsharge.ui.theme.AppThemeStyle
import com.bleelblep.glyphsharge.ui.theme.LocalThemeState
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository
import com.bleelblep.glyphsharge.ui.utils.HapticUtils
import com.bleelblep.glyphsharge.di.GlyphComponent
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

// --------------------------- Data class ---------------------------

data class PulseLockConfig(
    val animationId: String,
    val soundEnabled: Boolean,
    val soundUri: String?,
    val soundOffsetMs: Long,
    val durationMs: Long,
)

// --------------------- TOP-LEVEL confirmation dialog ----------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulseLockConfirmationDialog(
    onTestPulseLock: () -> Unit,
    onEnablePulseLock: () -> Unit,
    onDisablePulseLock: () -> Unit,
    onDismiss: () -> Unit,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val themeState = LocalThemeState.current
    var showEnableDialog by remember { mutableStateOf(false) }
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
                    text = "✨ Glow Gate",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Unlock animation",
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
                            text = "✨ How it works:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Plays the selected Glyph animation after unlock\n• Optional sound with adjustable delay",
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
                                        // Primary Action - Test Glow Gate
                ElevatedButton(
                    onClick = { 
                        HapticUtils.triggerMediumFeedback(haptic, context)
                        onTestPulseLock() 
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
                            text = "🧪 Test Glow Gate",
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

    // nested settings dialog
    if (showEnableDialog) {
        PulseLockEnableDialog(
            settingsRepository = settingsRepository,
            onConfirm = { cfg ->
                onEnablePulseLock()
                showEnableDialog = false
                onDismiss()
            },
            onDisable = {
                onDisablePulseLock()
                showEnableDialog = false
                onDismiss()
            },
            onDismiss = { showEnableDialog = false }
        )
    }
}

// --------------------------- Enable dialog ---------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulseLockEnableDialog(
    onConfirm: (PulseLockConfig) -> Unit,
    onDismiss: () -> Unit,
    onDisable: () -> Unit,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    // Check if PulseLock is currently enabled
    val currentlyEnabled = remember { settingsRepository.isPulseLockEnabled() }
    var isSaving by remember { mutableStateOf(false) }

    // initial prefs
    var selectedAnim by remember { mutableStateOf(PulseLockAnimations.getById(settingsRepository.getPulseLockAnimationId())) }
    var soundEnabled by remember { mutableStateOf(settingsRepository.isPulseLockAudioEnabled()) }
    var soundUri by remember { mutableStateOf(settingsRepository.getPulseLockAudioUri()) }
    var soundOffset by remember { 
        mutableStateOf(
            // Ensure offset is not negative - reset to 0 if it was previously negative
            maxOf(0f, settingsRepository.getPulseLockAudioOffset().toFloat())
        ) 
    }
    var animationDuration by remember { mutableStateOf(settingsRepository.getPulseLockDuration()) }
    
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
                Log.w("PulseLockComponents", "Error getting sound name for URI: $uriString", e)
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
            settingsRepository.savePulseLockAudioUri(soundUri)
            Log.d("PulseLockComponents", "Selected ringtone URI: $soundUri")
        }
    }
    
    // Custom file picker
    val customFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                Log.d("PulseLockComponents", "Processing custom file: $uri")
                
                // Copy the file to internal storage to avoid permission issues
                val internalUri = copyFileToInternalStorage(context, uri, "pulselock_custom_audio")
                
                if (internalUri != null) {
                    soundUri = internalUri.toString()
                    settingsRepository.savePulseLockAudioUri(soundUri)
                    Log.d("PulseLockComponents", "Successfully copied custom file to internal storage: $internalUri")
                } else {
                    // Fallback: try to use original URI with persistable permission
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            uri, 
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                        soundUri = uri.toString()
                        settingsRepository.savePulseLockAudioUri(soundUri)
                        Log.d("PulseLockComponents", "Fallback: using original URI with persistable permission")
                    } catch (e: Exception) {
                        Log.e("PulseLockComponents", "Failed to copy file and take persistable permission", e)
                        // Last resort: save original URI anyway
                        soundUri = uri.toString()
                        settingsRepository.savePulseLockAudioUri(soundUri)
                    }
                }
            } catch (e: Exception) {
                Log.e("PulseLockComponents", "Error processing custom file: $uri", e)
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
                    text = "Customize unlock animation",
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
                // Animation picker card
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
                        // Display animation options as icon chips in a wrap-content row
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
                                        selectedAnim = anim
                                        settingsRepository.savePulseLockAnimationId(anim.id)
                                    },
                                    label = {
                                        Text(anim.displayName)
                                    },
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
                                            else -> glyphAnimationManager.playPulseLockAnimation(selectedAnim.id)
                                        }
                                    } catch (e: Exception) {
                                        Log.e("PulseLockComponents", "Error testing animation: ${e.message}")
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

                // Animation Duration Card
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
                                settingsRepository.savePulseLockDuration(animationDuration)
                            },
                            valueRange = 1000f..10000f, // 1 to 10 seconds
                            steps = 17, // 0.5s increments (1s to 10s in 0.5s steps = 19 values, so 18 steps)
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
                                    // Get sound duration and set animation duration to match
                                    try {
                                        val mediaPlayer = android.media.MediaPlayer()
                                        mediaPlayer.setDataSource(context, Uri.parse(soundUri))
                                        mediaPlayer.prepare()
                                        val soundDuration = mediaPlayer.duration.toLong()
                                        mediaPlayer.release()
                                        
                                        // Set animation duration to sound duration (with a minimum of 1s and max of 10s)
                                        val newDuration = soundDuration.coerceIn(1000L, 10000L)
                                        animationDuration = newDuration
                                        settingsRepository.savePulseLockDuration(newDuration)
                                    } catch (e: Exception) {
                                        Log.e("PulseLockComponents", "Error getting sound duration: ${e.message}")
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

                // Sound Alerts Card (matching GlyphGuard style)
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
                        // Sound enabled toggle
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
                                    settingsRepository.savePulseLockAudioEnabled(it)
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
                                        else -> MaterialTheme.colorScheme.primaryContainer
                                    }
                                )
                            )
                        }
                        
                        // Sound selection (only show when sound is enabled)
                        if (soundEnabled) {
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
                                    color = when (themeState.themeStyle) {
                                        AppThemeStyle.AMOLED -> Color(0xFF2D2D2D)
                                        AppThemeStyle.CLASSIC -> Color(0xFFE8DEF8)
                                        else -> MaterialTheme.colorScheme.primaryContainer
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = soundName,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (themeState.themeStyle) {
                                            AppThemeStyle.AMOLED -> Color.White
                                            AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        
                        // Sound selection buttons (only show when sound is enabled)
                        if (soundEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // System sounds button
                                Button(
                                    onClick = {
                                        HapticUtils.triggerLightFeedback(haptic, context)
                                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select PulseLock Sound")
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                            
                                            // Set current selection
                                            soundUri?.let { uriString ->
                                                try {
                                                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(uriString))
                                                } catch (e: Exception) {
                                                    // If current URI is invalid, don't set it
                                                }
                                            }
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
                                    Text(
                                        text = "🎵 System",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                // Custom file button
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
                                    Text(
                                        text = "📁 Custom",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        
                        // Audio offset slider (only show when sound is enabled)
                        if (soundEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Audio Offset:",
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
                                        text = "${soundOffset.toInt()}ms",
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
                                value = soundOffset,
                                onValueChange = {
                                    HapticUtils.triggerLightFeedback(haptic, context)
                                    soundOffset = it
                                    settingsRepository.savePulseLockAudioOffset(it.toLong())
                                },
                                valueRange = 0f..1500f,
                                steps = 29, // 50ms increments (0 to 1500ms in 50ms steps = 30 values, so 29 steps)
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
                                    text = "Simultaneous (0ms)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "After (+1.5s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                ElevatedButton(
                    onClick = {
                        HapticUtils.triggerMediumFeedback(haptic, context)
                        isSaving = true
                        settingsRepository.savePulseLockAnimationId(selectedAnim.id)
                        settingsRepository.savePulseLockDuration(animationDuration)
                        settingsRepository.savePulseLockAudioEnabled(soundEnabled)
                        settingsRepository.savePulseLockAudioUri(soundUri)
                        settingsRepository.savePulseLockAudioOffset(soundOffset.toLong())
                        onConfirm(PulseLockConfig(selectedAnim.id, soundEnabled, soundUri, soundOffset.toLong(), animationDuration))
                        isSaving = false
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
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
                            text = if (currentlyEnabled) "💾 Save Settings" else "✨ Enable Glow Gate",
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
                        onClick = { onDisable() },
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
                            text = "✖️ Disable",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    OutlinedButton(
                        onClick = onDismiss,
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
 * Copy a file from external URI to internal storage to avoid permission issues
 */
private fun copyFileToInternalStorage(context: android.content.Context, sourceUri: Uri, fileName: String): Uri? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
        if (inputStream == null) {
            Log.e("PulseLockComponents", "Could not open input stream for URI: $sourceUri")
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
        val internalFile = File(context.filesDir, "$fileName$extension")
        
        // Copy the file
        inputStream.use { input ->
            FileOutputStream(internalFile).use { output ->
                input.copyTo(output)
            }
        }
        
        Log.d("PulseLockComponents", "Successfully copied file to: ${internalFile.absolutePath}")
        
        // Return file:// URI for internal storage
        Uri.fromFile(internalFile)
        
    } catch (e: Exception) {
        Log.e("PulseLockComponents", "Failed to copy file to internal storage", e)
        null
    }
}