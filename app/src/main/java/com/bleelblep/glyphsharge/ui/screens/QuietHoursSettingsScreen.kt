package com.bleelblep.glyphsharge.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.text.format.DateFormat

import com.bleelblep.glyphsharge.ui.components.*
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository
import com.bleelblep.glyphsharge.ui.utils.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import java.util.Calendar
import androidx.compose.material3.rememberTopAppBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuietHoursSettingsScreen(
    onBackClick: () -> Unit,
    settingsRepository: SettingsRepository
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberLazyListState()

    val quietHoursEnabled by remember { 
        mutableStateOf(settingsRepository.isQuietHoursEnabled()) 
    }
    
    // Update state when settings change
    LaunchedEffect(Unit) {
        // This will re-read the enabled state when the screen is opened
        // The toggle is now controlled from the main settings screen
    }
    var startHour by remember { 
        mutableStateOf(settingsRepository.getQuietHoursStartHour()) 
    }
    var startMinute by remember { 
        mutableStateOf(settingsRepository.getQuietHoursStartMinute()) 
    }
    var endHour by remember { 
        mutableStateOf(settingsRepository.getQuietHoursEndHour()) 
    }
    var endMinute by remember { 
        mutableStateOf(settingsRepository.getQuietHoursEndMinute()) 
    }

    // Save settings when they change (enabled state is now controlled from main settings)
    LaunchedEffect(startHour, startMinute) {
        settingsRepository.saveQuietHoursStartHour(startHour)
        settingsRepository.saveQuietHoursStartMinute(startMinute)
    }
    LaunchedEffect(endHour, endMinute) {
        settingsRepository.saveQuietHoursEndHour(endHour)
        settingsRepository.saveQuietHoursEndMinute(endMinute)
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Quiet Hours",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 42.sp
                        )
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
                actions = {},
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets.statusBars,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = true
        ) {
            // Status Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (quietHoursEnabled) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (quietHoursEnabled) "🔇" else "💡",
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = if (quietHoursEnabled) "Quiet Hours Enabled" else "Quiet Hours Disabled",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Text(
                                text = if (quietHoursEnabled) 
                                    "Configure your quiet hours schedule below" 
                                else 
                                    "Enable quiet hours from the main Settings screen",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Time Settings Cards (only show if enabled)
            if (quietHoursEnabled) {
                // Time Settings Row (side by side)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Start Time Card
                        TimeSettingCard(
                            title = "Start Time",
                            hour = startHour,
                            minute = startMinute,
                            onTimeSelected = { hour, minute ->
                                startHour = hour
                                startMinute = minute
                            },
                            icon = "🌆",
                            modifier = Modifier.weight(1f)
                        )
                        
                        // End Time Card
                        TimeSettingCard(
                            title = "End Time",
                            hour = endHour,
                            minute = endMinute,
                            onTimeSelected = { hour, minute ->
                                endHour = hour
                                endMinute = minute
                            },
                            icon = "🌅",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Current Status Card
                item {
                    val isCurrentlyInQuietHours = settingsRepository.isCurrentlyInQuietHours()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrentlyInQuietHours) 
                                MaterialTheme.colorScheme.errorContainer 
                            else 
                                MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isCurrentlyInQuietHours) "🌙" else "☀️",
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "Current Status",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (isCurrentlyInQuietHours) 
                                    "Quiet hours are currently active - glyph animations are disabled" 
                                else 
                                    "Outside quiet hours - glyph animations are enabled",
                                style = MaterialTheme.typography.bodyMedium,
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Quiet hours: ${formatTime(context, startHour, startMinute)} - ${formatTime(context, endHour, endMinute)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = "How it works",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "During quiet hours, all glyph animations will be automatically disabled. This includes:\n\n• PowerPeek animations\n• Glow Gate effects\n• Low battery alerts\n• Glyph Guard alerts\n• Charging animations",
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 20.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "The service runs in the background and will automatically re-enable glyphs when quiet hours end.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeSettingCard(
    title: String,
    hour: Int,
    minute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    icon: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showTimePicker by remember { mutableStateOf(false) }

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
                .padding(20.dp)
                .clickable {
                    HapticUtils.triggerLightFeedback(haptic, context)
                    showTimePicker = true
                },
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

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatTime(context, hour, minute),
                    style = MaterialTheme.typography.bodyLarge,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = icon,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            onTimeSelected = { selectedHour, selectedMinute ->
                onTimeSelected(selectedHour, selectedMinute)
                showTimePicker = false
            },
            onDismiss = {
                showTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    
    // Follow system clock format (12h or 24h)
    val is24Hour = DateFormat.is24HourFormat(context)
    
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🕐 Select Time",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Choose your quiet hours time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = if (MaterialTheme.colorScheme.surface == Color(0xFF1E1E1E)) {
                            Color(0xFF2D1B3D) // Dark purple dial for dark theme
                        } else {
                            Color(0xFFF5F0FF) // Light purple dial for light theme
                        },
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = Color(0xFF674FA3), // Purple numbers
                        selectorColor = Color(0xFF674FA3), // GlyphSharge purple
                        containerColor = MaterialTheme.colorScheme.surface,
                        periodSelectorBorderColor = Color(0xFF674FA3), // GlyphSharge purple
                        periodSelectorSelectedContainerColor = Color(0xFF674FA3), // GlyphSharge purple
                        periodSelectorUnselectedContainerColor = Color.Transparent,
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContentColor = Color(0xFF674FA3), // Purple AM/PM text
                        timeSelectorSelectedContainerColor = Color(0xFF674FA3), // GlyphSharge purple
                        timeSelectorUnselectedContainerColor = if (MaterialTheme.colorScheme.surface == Color(0xFF1E1E1E)) {
                            Color(0xFF4A3D5A) // Dark purple outline for dark theme
                        } else {
                            Color(0xFFE8E1F5) // Light purple outline for light theme
                        },
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContentColor = Color(0xFF674FA3) // Purple time text
                    )
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { 
                        HapticUtils.triggerLightFeedback(haptic, context)
                        onDismiss() 
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "✕ Cancel",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Button(
                    onClick = { 
                        HapticUtils.triggerMediumFeedback(haptic, context)
                        onTimeSelected(timePickerState.hour, timePickerState.minute)
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF674FA3), // GlyphSharge purple
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "✓ Set Time",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Formats time according to system settings (12h or 24h format)
 */
private fun formatTime(context: Context, hour: Int, minute: Int): String {
    val is24Hour = DateFormat.is24HourFormat(context)
    
    return if (is24Hour) {
        // 24-hour format
        String.format("%02d:%02d", hour, minute)
    } else {
        // 12-hour format
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val amPm = if (hour < 12) "AM" else "PM"
        String.format("%d:%02d %s", displayHour, minute, amPm)
    }
} 