package com.bleelblep.glyphsharge.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository
import com.bleelblep.glyphsharge.ui.utils.HapticUtils
import com.bleelblep.glyphsharge.ui.utils.HapticType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HapticTestCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick,
        enabled = enabled
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Compact haptic settings card for main settings screen
 * Shows current intensity and allows quick selection between Light, Medium, Strong
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HapticSettingsCard(
    settingsRepository: SettingsRepository,
    onNavigateToFullSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    // Use reactive state from SettingsRepository
    val currentIntensity by settingsRepository.vibrationIntensityFlow

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.large,
        onClick = onNavigateToFullSettings
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with icon and title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Vibration,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Haptic Feedback",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.weight(1f))

                // Current intensity indicator
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = when {
                            currentIntensity <= 0.0f -> "Off"
                            currentIntensity <= 0.4f -> "Light"
                            currentIntensity <= 0.7f -> "Medium"
                            currentIntensity >= 0.9f -> "Strong"
                            else -> "Custom"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Customize vibration intensity for app interactions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick intensity buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Light Button (33%)
                IntensityButton(
                    label = "Light",
                    value = "33%",
                    intensity = 0.33f,
                    currentIntensity = currentIntensity,
                    onIntensitySelected = { newIntensity ->
                        settingsRepository.saveVibrationIntensity(newIntensity)
                        HapticUtils.performHapticWithIntensity(context, haptic, newIntensity, HapticType.MEDIUM)
                    },
                    modifier = Modifier.weight(1f)
                )
                
                // Medium Button (66%)
                IntensityButton(
                    label = "Medium",
                    value = "66%",
                    intensity = 0.66f,
                    currentIntensity = currentIntensity,
                    onIntensitySelected = { newIntensity ->
                        settingsRepository.saveVibrationIntensity(newIntensity)
                        HapticUtils.performHapticWithIntensity(context, haptic, newIntensity, HapticType.MEDIUM)
                    },
                    modifier = Modifier.weight(1f)
                )
                
                // Strong Button (100%)
                IntensityButton(
                    label = "Strong",
                    value = "100%",
                    intensity = 1.0f,
                    currentIntensity = currentIntensity,
                    onIntensitySelected = { newIntensity ->
                        settingsRepository.saveVibrationIntensity(newIntensity)
                        HapticUtils.performHapticWithIntensity(context, haptic, newIntensity, HapticType.MEDIUM)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual intensity button for the haptic settings card
 */
@Composable
private fun IntensityButton(
    label: String,
    value: String,
    intensity: Float,
    currentIntensity: Float,
    onIntensitySelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = kotlin.math.abs(currentIntensity - intensity) < 0.01f
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        animationSpec = tween(300),
        label = "intensityButtonBackground"
    )
    
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(300),  
        label = "intensityButtonContent"
    )

    Button(
        onClick = { 
            HapticUtils.performHapticWithIntensity(context, haptic, currentIntensity, HapticType.LIGHT)
            onIntensitySelected(intensity) 
        },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
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
                text = value,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
} 