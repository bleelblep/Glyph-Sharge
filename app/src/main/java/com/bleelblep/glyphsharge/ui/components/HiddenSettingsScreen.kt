package com.bleelblep.glyphsharge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import com.bleelblep.glyphsharge.ui.theme.GlyphZenRed
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.bleelblep.glyphsharge.ui.theme.FontVariant
import com.bleelblep.glyphsharge.ui.theme.AppThemeStyle
import android.content.Intent
import kotlin.system.exitProcess
import com.bleelblep.glyphsharge.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import android.net.Uri
import com.bleelblep.glyphsharge.utils.LoggingManager
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Context
import androidx.compose.ui.graphics.vector.ImageVector
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository
import com.bleelblep.glyphsharge.ui.theme.NothingRed
import androidx.compose.ui.platform.LocalHapticFeedback
import com.bleelblep.glyphsharge.ui.utils.HapticUtils

// Constants for common dimensions and values
private object Dimensions {
    const val CARD_HEIGHT = 120
    const val CARD_ELEVATION = 1
    const val ICON_SIZE = 24
    const val LARGE_ICON_SIZE = 28
    const val SPACING = 16
    const val PADDING = 20
    const val SMALL_SPACING = 8
    const val TINY_SPACING = 4
}

// Common card modifier for consistent styling
private fun Modifier.standardCard() = this
    .fillMaxWidth()
    .height(Dimensions.CARD_HEIGHT.dp)
    .padding(Dimensions.PADDING.dp)

/**
 * Hidden settings screen for developer and advanced options
 * Accessed through secret tap sequence on main screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenSettingsScreen(
    onBackClick: () -> Unit,
    onLEDCalibrationClick: () -> Unit,
    onCardExamplesClick: () -> Unit = {},
    onHardwareDiagnosticsClick: () -> Unit = {},
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier
) {
    // State management
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showCountdownDialog by remember { mutableStateOf(false) }
    var showRunningDialog by remember { mutableStateOf(false) }
    var dialogState by remember { mutableStateOf<DialogState?>(null) }
    var cycleSpeedMultiplier by remember { mutableStateOf(1f) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    // Scroll state management
    val scrollState = rememberLazyListState()
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
                        text = "Developer Settings",
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
                start = Dimensions.SPACING.dp,
                end = Dimensions.SPACING.dp,
                top = paddingValues.calculateTopPadding() + Dimensions.SPACING.dp,
                bottom = paddingValues.calculateBottomPadding() + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + Dimensions.SPACING.dp
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.SPACING.dp)
        ) {
            // Warning Card
            item {
                WarningCard()
            }

            // Debug Logging Section
            item {
                HiddenSectionHeader("Debug Logging")
            }

            // Debug Logging Card
            item {
                DebugLoggingCard(context)
            }

            // Card Examples Card
            item {
                CardExamplesCard(onCardExamplesClick)
            }

            // Hardware Control Section
            item {
                HiddenSectionHeader("Hardware Control")
            }

            // Hardware Diagnostics Card
            item {
                HardwareDiagnosticsCard(onHardwareDiagnosticsClick)
            }

            // LED Calibration Card
            item {
                LEDCalibrationCard(onLEDCalibrationClick)
            }

            // Dialog Test Section
            item {
                HiddenSectionHeader(
                    title = "Dialog Tests",
                    modifier = Modifier.padding(top = Dimensions.SPACING.dp)
                )
            }

            // Cycle Speed Control Card
            item {
                CycleSpeedControlCard(cycleSpeedMultiplier) { cycleSpeedMultiplier = it }
            }

            // Dialog Test Cards
            item {
                DialogTestCards(
                    onConfirmationClick = { 
                        dialogState = DialogState.CONFIRMATION
                        showConfirmationDialog = true 
                    },
                    onCountdownClick = { 
                        dialogState = DialogState.COUNTDOWN
                        showCountdownDialog = true 
                    },
                    onRunningClick = { 
                        dialogState = DialogState.ANIMATION_PROGRESS
                        showRunningDialog = true 
                    }
                )
            }

            // Run Onboarding Again Card
            item {
                SimpleCard(
                    title = "Run Onboarding Again",
                    description = "Reset intro walkthrough and view it once more.",
                    onClick = {
                        HapticUtils.triggerMediumFeedback(haptic, context)
                        settingsRepository.setOnboardingComplete(false)
                        android.widget.Toast.makeText(context, "Onboarding will show next launch", android.widget.Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Danger Zone Section
            item {
                HiddenSectionHeader("Danger Zone")
            }

            // Danger Zone Cards
            item {
                DangerZoneCards(context, settingsRepository)
            }
        }
    }

    // Dialog states
    if (showConfirmationDialog) {
        FeatureConfirmationDialog(
            title = "Confirmation Test",
            description = "This is a test of the confirmation dialog state. It shows the initial confirmation screen with custom text.",
            onConfirm = { progressCallback ->
                progressCallback(0f)
                delay(5000)
                progressCallback(1f)
            },
            onDismiss = { 
                showConfirmationDialog = false
                dialogState = null
            },
            initialDialogState = DialogState.CONFIRMATION,
            isTestMode = true,
            customButtons = {
                DialogTestButtons(
                    onExit = { 
                        showConfirmationDialog = false
                        dialogState = null
                    }
                )
            }
        )
    }

    if (showCountdownDialog) {
        FeatureConfirmationDialog(
            title = "Countdown Test",
            description = "This is a test of the countdown dialog state. It will show a 5-second countdown before proceeding.",
            onConfirm = { /* Do nothing - we don't want to trigger the running dialog */ },
            onDismiss = { 
                showCountdownDialog = false
                dialogState = null
            },
            initialDialogState = DialogState.COUNTDOWN,
            isTestMode = true,
            customButtons = {
                DialogTestButtons(
                    onExit = { 
                        showCountdownDialog = false
                        dialogState = null
                    }
                )
            }
        )
    }

    if (showRunningDialog) {
        FeatureConfirmationDialog(
            title = "Running Dialog Test",
            description = "This is a test of the running dialog.",
            onConfirm = { progressCallback ->
                for (i in 0..100) {
                    progressCallback(i / 100f)
                    delay(50)
                }
            },
            onDismiss = { showRunningDialog = false },
            initialDialogState = DialogState.ANIMATION_PROGRESS,
            isTestMode = true
        )
    }
}

@Composable
private fun WarningCard() {
                    Card(
        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.CARD_ELEVATION.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
            modifier = Modifier.padding(Dimensions.PADDING.dp)
                        ) {
                            Row(
                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(Dimensions.ICON_SIZE.dp)
                                )
                                
                Spacer(modifier = Modifier.width(Dimensions.SPACING.dp))
                                
                                Text(
                    text = "Developer Settings",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

            Spacer(modifier = Modifier.height(Dimensions.SPACING.dp))

                            Text(
                text = "Advanced Configuration",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(Dimensions.SMALL_SPACING.dp))

                            Text(
                text = "Control and customize the Glyph interface on Nothing phones",
                                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
                            )

            Spacer(modifier = Modifier.height(Dimensions.SMALL_SPACING.dp))
                            
                            Text(
                text = "Use with caution",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun DebugLoggingCard(context: Context) {
                var isLoggingEnabled by remember { mutableStateOf(LoggingManager.isLoggingEnabled()) }
                val haptic = LocalHapticFeedback.current
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
            .height(160.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.CARD_ELEVATION.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                .padding(Dimensions.PADDING.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
                    ) {
                        Column(
                                modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Debug Logging",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                    Spacer(modifier = Modifier.height(Dimensions.TINY_SPACING.dp))

                            Text(
                                    text = "Capture C15 bug & export logs",
                                style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }

                Spacer(modifier = Modifier.width(Dimensions.SPACING.dp))
                            
                            MorphingToggleButton(
                                checked = isLoggingEnabled,
                                onCheckedChange = { enabled ->
                                    isLoggingEnabled = enabled
                                    LoggingManager.setLoggingEnabled(enabled)
                                    Toast.makeText(
                                        context,
                                        if (enabled) "Logging enabled" else "Logging disabled",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                enabledIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = "Logging enabled",
                            tint = Color.White,
                            modifier = Modifier.size(Dimensions.ICON_SIZE.dp)
                                    )
                                },
                                disabledIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = "Logging disabled",
                            tint = Color.White,
                            modifier = Modifier.size(Dimensions.ICON_SIZE.dp)
                                    )
                                }
                            )
                        }
                        
            Spacer(modifier = Modifier.height(Dimensions.SPACING.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    HapticUtils.triggerLightFeedback(haptic, context)
                                    val logContent = LoggingManager.exportLogs()
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "GlyphZen Debug Logs")
                                        putExtra(Intent.EXTRA_TEXT, logContent)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Logs"))
                                },
                    enabled = isLoggingEnabled,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                        contentDescription = "Share Logs",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimensions.SMALL_SPACING.dp))
                    Text("Share Logs")
                            }
                        }
                    }
                }
            }

@Composable
private fun CardExamplesCard(onCardExamplesClick: () -> Unit) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
            .height(Dimensions.CARD_HEIGHT.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.CARD_ELEVATION.dp),
                    shape = MaterialTheme.shapes.large,
                    onClick = onCardExamplesClick
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                .padding(Dimensions.PADDING.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )

            Spacer(modifier = Modifier.width(Dimensions.SPACING.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Card Examples",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                Spacer(modifier = Modifier.height(Dimensions.TINY_SPACING.dp))

                            Text(
                                text = "View animation patterns and templates",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Open",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

@Composable
private fun HardwareDiagnosticsCard(
    onHardwareDiagnosticsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimensions.CARD_HEIGHT.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.CARD_ELEVATION.dp),
        shape = MaterialTheme.shapes.large,
        onClick = onHardwareDiagnosticsClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.PADDING.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Hardware Diagnostics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(Dimensions.TINY_SPACING.dp))

                Text(
                    text = "Test haptic feedback and hardware features",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Open",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun LEDCalibrationCard(onLEDCalibrationClick: () -> Unit) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
            .height(Dimensions.CARD_HEIGHT.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.CARD_ELEVATION.dp),
                    shape = MaterialTheme.shapes.large,
                    onClick = onLEDCalibrationClick
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                .padding(Dimensions.PADDING.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "LED Calibration",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                Spacer(modifier = Modifier.height(Dimensions.TINY_SPACING.dp))

                            Text(
                                text = "Calibrate individual LED brightness",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Calibrate",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

@Composable
private fun CycleSpeedControlCard(
    cycleSpeedMultiplier: Float,
    onSpeedChange: (Float) -> Unit
) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.CARD_ELEVATION.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                .padding(Dimensions.PADDING.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Cycle Speed Multiplier",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )

            Spacer(modifier = Modifier.height(Dimensions.TINY_SPACING.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Speed: ${cycleSpeedMultiplier}x",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = cycleSpeedMultiplier,
                    onValueChange = onSpeedChange,
                                valueRange = 1f..5f,
                                steps = 4,
                                modifier = Modifier.width(200.dp)
                            )
                        }
                    }
                }
            }

@Composable
private fun DialogTestCards(
    onConfirmationClick: () -> Unit,
    onCountdownClick: () -> Unit,
    onRunningClick: () -> Unit
                    ) {
                        Column(
        verticalArrangement = Arrangement.spacedBy(Dimensions.SPACING.dp)
    ) {
        DialogTestCard(
            title = "Confirmation Dialog",
            description = "Test the initial confirmation screen",
            icon = Icons.Default.Info,
            onClick = onConfirmationClick
        )

        DialogTestCard(
            title = "Countdown Dialog",
            description = "Test the 5-second countdown screen",
            icon = Icons.Default.Refresh,
            onClick = onCountdownClick
        )

        DialogTestCard(
            title = "Running Dialog",
            description = "Test the animation progress screen",
            icon = Icons.Default.PlayArrow,
            onClick = onRunningClick
        )
    }
}

@Composable
private fun DialogTestCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.CARD_ELEVATION.dp),
                    shape = MaterialTheme.shapes.large,
        onClick = onClick
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                .padding(Dimensions.PADDING.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                    text = title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                Spacer(modifier = Modifier.height(Dimensions.TINY_SPACING.dp))

                            Text(
                    text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Icon(
                imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                modifier = Modifier.size(Dimensions.LARGE_ICON_SIZE.dp)
            )
        }
    }
}

@Composable
private fun DialogTestButtons(
    onExit: () -> Unit
                    ) {
                        Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimensions.SMALL_SPACING.dp)
    ) {
        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFDBD7),
                contentColor = Color(0xFF000000)
            )
        ) {
            Text(
                text = "Exit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFDBD7),
                contentColor = Color(0xFF000000)
            )
        ) {
            Text(
                text = "Dismiss",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun DangerZoneCards(
    context: Context,
    settingsRepository: SettingsRepository
) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.SMALL_SPACING.dp)
    ) {
        // Factory Reset Card
        DangerZoneCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Refresh,
            title = "Factory Reset",
            description = "Reset all settings to defaults",
                        onClick = {
                            settingsRepository.saveFontVariant(FontVariant.HEADLINE)
                settingsRepository.saveTheme(false)
                            settingsRepository.saveThemeStyle(AppThemeStyle.CLASSIC)
                            settingsRepository.saveGlyphServiceEnabled(true)
                            Toast.makeText(
                                context,
                                "Settings reset to default values",
                                Toast.LENGTH_SHORT
                            ).show()
                            CoroutineScope(Dispatchers.Main).launch {
                    delay(500)
                                val intent = Intent(context, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                context.startActivity(intent)
                                exitProcess(0)
                            }
                        }
        )

        // Clear All Data Card
        DangerZoneCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Delete,
            title = "Clear All Data",
            description = "Remove all app data and preferences",
            onClick = {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:" + context.packageName)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        )
    }
}

@Composable
private fun DangerZoneCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
                    Card(
        modifier = modifier.height(120.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = GlyphZenRed,
                            contentColor = Color.White
                        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.CARD_ELEVATION.dp),
                        shape = MaterialTheme.shapes.large,
        onClick = onClick
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White,
                modifier = Modifier.size(Dimensions.LARGE_ICON_SIZE.dp)
                            )

            Spacer(modifier = Modifier.height(Dimensions.SMALL_SPACING.dp))

                            Text(
                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

            Spacer(modifier = Modifier.height(Dimensions.TINY_SPACING.dp))

                            Text(
                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
        }
    }
}

@Composable
private fun HiddenSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        letterSpacing = 0.5.sp,
        modifier = modifier.padding(vertical = 8.dp)
    )
} 