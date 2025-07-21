package com.bleelblep.glyphsharge.ui.screens

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareDiagnosticsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    val hasHapticFeedback = vibrator.hasVibrator()

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
        modifier = modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Hardware Diagnostics",
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Hardware Diagnostics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Test hardware components",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Run comprehensive tests for haptic feedback, LED patterns, and other hardware features.",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Haptic Feedback Status
            item {
                HapticStatusCard(hasHapticFeedback)
            }

            // Haptic Feedback Testing Section
            item {
                HomeSectionHeader(title = "Haptic Feedback Testing")
            }

            // Light Haptic Test
            item {
                HapticTestCard(
                    title = "Light Feedback",
                    description = "Test subtle haptic feedback for light interactions",
                    onClick = { HapticUtils.triggerLightFeedback(haptic, context) },
                    enabled = hasHapticFeedback
                )
            }

            // Medium Haptic Test
            item {
                HapticTestCard(
                    title = "Medium Feedback",
                    description = "Test medium haptic feedback for significant interactions",
                    onClick = { HapticUtils.triggerMediumFeedback(haptic, context) },
                    enabled = hasHapticFeedback
                )
            }

            // Strong Haptic Test
            item {
                HapticTestCard(
                    title = "Strong Feedback",
                    description = "Test strong haptic feedback for major interactions",
                    onClick = { HapticUtils.triggerStrongFeedback(haptic, context) },
                    enabled = hasHapticFeedback
                )
            }

            // Success Haptic Test
            item {
                HapticTestCard(
                    title = "Success Feedback",
                    description = "Test success haptic feedback pattern",
                    onClick = { HapticUtils.triggerSuccessFeedback(haptic, context) },
                    enabled = hasHapticFeedback
                )
            }

            // Error Haptic Test
            item {
                HapticTestCard(
                    title = "Error Feedback",
                    description = "Test error haptic feedback pattern",
                    onClick = { HapticUtils.triggerErrorFeedback(haptic, context) },
                    enabled = hasHapticFeedback
                )
            }
        }
    }
}

@Composable
private fun HapticStatusCard(
    hasHapticFeedback: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasHapticFeedback) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = null,
                tint = if (hasHapticFeedback) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Haptic Feedback Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (hasHapticFeedback)
                        "Haptic feedback is available and ready to test"
                    else
                        "Haptic feedback is not available on this device",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasHapticFeedback)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun HapticTestCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.large,
        onClick = if (enabled) onClick else { {} }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
} 