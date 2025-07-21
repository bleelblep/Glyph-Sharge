package com.bleelblep.glyphsharge.ui.screens

import android.content.Context
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bleelblep.glyphsharge.ui.components.*
import com.bleelblep.glyphsharge.ui.utils.HapticUtils
import com.bleelblep.glyphsharge.ui.utils.HapticType
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Vibration Settings Screen with comprehensive haptic feedback customization
 * Updated to follow consistent UI patterns throughout the app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VibrationSettingsScreen(
    onBackClick: () -> Unit,
    settingsRepository: SettingsRepository
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    // Use reactive state from SettingsRepository
    val vibrationIntensity by settingsRepository.vibrationIntensityFlow
    val scrollState = rememberLazyListState()

    // Simple and precise scroll detection - transparent ONLY when exactly at original position
    val isScrolled by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0 || scrollState.firstVisibleItemScrollOffset > 0
        }
    }

    // Instant app bar background color change instead of animated transition
    val appBarBackgroundColor = if (isScrolled) {
        MaterialTheme.colorScheme.surface // Solid surface when scrolled
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.0f) // Fully transparent when at top
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Vibration Settings",
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        HapticUtils.triggerLightFeedback(haptic, context)
                        onBackClick() 
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = appBarBackgroundColor
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Intensity Settings Section Header
            item {
                HomeSectionHeader(
                    title = "Intensity Settings",
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            // Current Intensity Display Card
            item {
                SimpleCard(
                    title = "Current Intensity",
                    description = when {
                        vibrationIntensity <= 0.0f -> "Off (0%)"
                        vibrationIntensity <= 0.4f -> "Light (${(vibrationIntensity * 100).toInt()}%)"
                        vibrationIntensity <= 0.7f -> "Medium (${(vibrationIntensity * 100).toInt()}%)"
                        vibrationIntensity >= 0.9f -> "Strong (${(vibrationIntensity * 100).toInt()}%)"
                        else -> "Custom (${(vibrationIntensity * 100).toInt()}%)"
                    }
                )
            }

            // Intensity Presets Card
            item {
                ContentCard(
                    title = "Intensity Presets",
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Text(
                        text = "Choose a vibration intensity level",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Intensity Selection Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Light Button (33%)
                        IntensityPresetButton(
                            label = "Light",
                            percentage = "33%",
                            intensity = 0.33f,
                            currentIntensity = vibrationIntensity,
                            onSelected = { newIntensity ->
                                settingsRepository.saveVibrationIntensity(newIntensity)
                                HapticUtils.performHapticWithIntensity(context, haptic, newIntensity, HapticType.MEDIUM)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Medium Button (66%)
                        IntensityPresetButton(
                            label = "Medium",
                            percentage = "66%",
                            intensity = 0.66f,
                            currentIntensity = vibrationIntensity,
                            onSelected = { newIntensity ->
                                settingsRepository.saveVibrationIntensity(newIntensity)
                                HapticUtils.performHapticWithIntensity(context, haptic, newIntensity, HapticType.MEDIUM)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Strong Button (100%)
                        IntensityPresetButton(
                            label = "Strong",
                            percentage = "100%",
                            intensity = 1.0f,
                            currentIntensity = vibrationIntensity,
                            onSelected = { newIntensity ->
                                settingsRepository.saveVibrationIntensity(newIntensity)
                                HapticUtils.performHapticWithIntensity(context, haptic, newIntensity, HapticType.MEDIUM)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Test Feedback Section Header
            item {
                HomeSectionHeader(
                    title = "Test Feedback Types",
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            // Test Cards
            items(
                listOf(
                    Triple("Light Feedback", "Subtle vibration for light interactions", { HapticUtils.performHapticWithIntensity(context, haptic, vibrationIntensity, HapticType.LIGHT) }),
                    Triple("Medium Feedback", "Moderate vibration for significant interactions", { HapticUtils.performHapticWithIntensity(context, haptic, vibrationIntensity, HapticType.MEDIUM) }),
                    Triple("Strong Feedback", "Intense vibration for major interactions", { HapticUtils.performHapticWithIntensity(context, haptic, vibrationIntensity, HapticType.STRONG) }),
                    Triple("Success Feedback", "Positive feedback pattern", { HapticUtils.performHapticWithIntensity(context, haptic, vibrationIntensity, HapticType.SUCCESS) }),
                    Triple("Error Feedback", "Negative feedback pattern", { HapticUtils.performHapticWithIntensity(context, haptic, vibrationIntensity, HapticType.ERROR) })
                )
            ) { (title, description, action) ->
                HapticTestCard(
                    title = title,
                    description = description,
                    onClick = action
                )
            }

            // Device Information Section Header
            item {
                HomeSectionHeader(
                    title = "Device Information",
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            // Device Capabilities Card
            item {
                val vibrationInfo = remember { HapticUtils.getVibrationInfo(context) }
                
                ContentCard(
                    title = "Vibration Capabilities",
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Vibrator Available:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (vibrationInfo.hasVibrator) "✅ Yes" else "❌ No",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Amplitude Control:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (vibrationInfo.hasAmplitudeControl) "✅ Yes" else "❌ No",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Supported Effects:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${vibrationInfo.supportedEffects.size} effects",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual intensity preset button component
 */
@Composable
private fun IntensityPresetButton(
    label: String,
    percentage: String,
    intensity: Float,
    currentIntensity: Float,
    onSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = kotlin.math.abs(currentIntensity - intensity) < 0.01f
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    Button(
        onClick = { 
            HapticUtils.performHapticWithIntensity(context, haptic, currentIntensity, HapticType.LIGHT)
            onSelected(intensity) 
        },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
            Text(
                text = percentage,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun HapticTestCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.large,
        onClick = { 
            HapticUtils.triggerLightFeedback(haptic, context)
            onClick() 
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
} 