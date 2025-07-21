package com.bleelblep.glyphsharge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bleelblep.glyphsharge.data.model.ChargingSession
import com.bleelblep.glyphsharge.data.model.HealthStatus
import com.bleelblep.glyphsharge.ui.components.FeatureGrid
import com.bleelblep.glyphsharge.ui.components.HomeSectionHeader
import com.bleelblep.glyphsharge.ui.components.SquareFeatureCard
import com.bleelblep.glyphsharge.ui.components.TransparentTopAppBar
import com.bleelblep.glyphsharge.ui.components.rememberEmojiPainter
import java.util.*
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.bleelblep.glyphsharge.ui.viewmodel.BatteryStoryViewModel
import java.text.SimpleDateFormat
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.collectAsState
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.ui.platform.LocalContext
import com.bleelblep.glyphsharge.ui.components.WideFeatureCard
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import com.bleelblep.glyphsharge.ui.theme.LocalThemeState
import com.bleelblep.glyphsharge.ui.theme.AppThemeStyle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.WindowInsets
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalHapticFeedback
import com.bleelblep.glyphsharge.ui.utils.HapticUtils
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.rememberTopAppBarState

/**
 * Battery Story – shows recent charging sessions using the same square card grid
 * used on the Home screen (Power Peek, Glyph Guard, etc.).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryStoryScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BatteryStoryViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val dischargeState = remember {
        queryBatteryState(context)
    }

    // Sorting state - true for descending (newest first), false for ascending (oldest first)
    var sortDescending by remember { mutableStateOf(true) }

    // Refresh battery state periodically
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000) // Refresh every 5 seconds
            queryBatteryState(context)
        }
    }

    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val themeState = LocalThemeState.current

    // Merge overlapping or identical sessions first, then filter small deltas
    val mergedSessions = remember(sessions, sortDescending) {
        if (sessions.isEmpty()) emptyList() else {
            val sorted = sessions.sortedBy { it.startTimestamp }
            val result = mutableListOf<ChargingSession>()
            var current = sorted.first()
            for (i in 1 until sorted.size) {
                val next = sorted[i]
                // overlap if next starts before current ends (or current still open)
                val currentEnd = if (current.endTimestamp == 0L) current.startTimestamp else current.endTimestamp
                if (next.startTimestamp <= currentEnd) {
                    // merge: earliest start, latest end, average temps weighted
                    val combinedSample = current.sampleCount + next.sampleCount
                    val avgTemp = (current.avgTemperatureC * current.sampleCount + next.avgTemperatureC * next.sampleCount) / combinedSample
                    current = current.copy(
                        endTimestamp = maxOf(current.endTimestamp, next.endTimestamp),
                        endPercentage = next.endPercentage,
                        avgTemperatureC = avgTemp,
                        sampleCount = combinedSample
                    )
                } else {
                    result.add(current)
                    current = next
                }
            }
            result.add(current)
            
            // Apply final sorting based on user preference
            if (sortDescending) {
                result.sortedByDescending { it.startTimestamp }
            } else {
                result.sortedBy { it.startTimestamp }
            }
        }
    }

    val displayedSessions = mergedSessions.filter { it.chargeDelta >= 1 }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Battery Story",
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
                actions = {
                    var showHealthInfo by remember { mutableStateOf(false) }
                    var showConfirm by remember { mutableStateOf(false) }
                    IconButton(onClick = { 
                        HapticUtils.triggerLightFeedback(haptic, context)
                        showConfirm = true 
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear sessions")
                    }

                    IconButton(onClick = { 
                        HapticUtils.triggerLightFeedback(haptic, context)
                        showHealthInfo = true 
                    }) {
                        Icon(Icons.Default.Info, contentDescription = "Battery Health Info")
                    }

                    if (showHealthInfo) {
                        HealthInfoDialog(onDismiss = { showHealthInfo = false })
                    }

                    if (showConfirm) {
                        AlertDialog(
                            onDismissRequest = { showConfirm = false },
                            title = { Text("Clear history?") },
                            text = { Text("This will permanently delete all charging sessions.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    HapticUtils.triggerMediumFeedback(haptic, context)
                                    showConfirm = false
                                    // Clear via viewModel
                                    coroutineScope.launch { viewModel.clearSessions() }
                                }) { Text("Delete") }
                            },
                            dismissButton = {
                                TextButton(onClick = { 
                                    HapticUtils.triggerLightFeedback(haptic, context)
                                    showConfirm = false 
                                }) { Text("Cancel") }
                            }
                        )
                    }
                },
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
            modifier = modifier
                .fillMaxSize()
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
            // Current discharge card at top
            item {
                DischargeCard(state = dischargeState)
            }

            // Section header with sort toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HomeSectionHeader(
                        title = "Sessions",
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Sort toggle button
                    OutlinedButton(
                        onClick = { 
                            HapticUtils.triggerLightFeedback(haptic, context)
                            sortDescending = !sortDescending 
                        },
                        modifier = Modifier.height(36.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                                AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                                AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (sortDescending) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (sortDescending) "Newest" else "Oldest",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Render sessions in rows of two like home screen grid
            if (displayedSessions.isEmpty()) {
                item {
                    Text(
                        text = "No charging sessions yet",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                itemsIndexed(displayedSessions.chunked(2)) { _, pair ->
                    FeatureGrid {
                        pair.forEach { session ->
                            SessionSquareCard(session = session, viewModel = viewModel, modifier = Modifier.weight(1f))
                        }
                        // if odd number, add spacer to keep grid alignment
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Single session rendered as a SquareFeatureCard (re-using the Home screen behaviour).
 */
@Composable
private fun SessionSquareCard(
    session: ChargingSession,
    viewModel: BatteryStoryViewModel,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val themeState = LocalThemeState.current

    // Title shows emoji + delta %, description shows start-end time and duration
    val title = "⬆️ ${session.chargeDelta}%"
    val description = if (session.endTimestamp > 0) {
        "${formatter.format(Date(session.startTimestamp))} - ${formatter.format(Date(session.endTimestamp))} • ${session.formattedDuration()}"
    } else {
        "${formatter.format(Date(session.startTimestamp))} - Ongoing"
    }

    SquareFeatureCard(
        title = title,
        description = description,
        icon = rememberEmojiPainter(session.health.emoji, fontSizeDp = 36f),
        iconTint = Color.Unspecified,
        onClick = { showDialog = true },
        modifier = modifier,
        iconSize = 40,
        isServiceActive = true,
        skipConfirmation = true
    )

    if (showDialog) {
        SessionDetailDialog(
            session = session,
            onDismiss = { showDialog = false },
            viewModel = viewModel
        )
    }
}

@Composable
private fun SessionDetailDialog(
    session: ChargingSession,
    onDismiss: () -> Unit,
    viewModel: BatteryStoryViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    var showConfirmDelete by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("MMM dd yyyy, h:mm a", Locale.getDefault()) }
    val endTime = if (session.endTimestamp > 0) {
        formatter.format(Date(session.endTimestamp))
    } else {
        "Ongoing"
    }
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = "📝 Session Details",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (themeState.themeStyle) {
                        AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                        AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) MaterialTheme.colorScheme.surfaceContainer else Color(0xFFF8F5FF)
                        else -> MaterialTheme.colorScheme.surfaceContainer
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailRow("🕐 Start", formatter.format(Date(session.startTimestamp)))
                    DetailRow("⏰ End", endTime)
                    DetailRow("⚡ Charge Gain", "${session.chargeDelta}%")
                    DetailRow("🌡️ Avg Temp", "${session.avgTemperatureC.roundToInt()}°C")
                    DetailRow("⏳ Duration", session.formattedDuration())
                    DetailRow("👍 Health", when (session.health) {
                        HealthStatus.EXCELLENT -> "Excellent 🟢 ${session.healthScore}"
                        HealthStatus.GOOD -> "Good 😊 ${session.healthScore}"
                        HealthStatus.FAIR -> "Fair 😐 ${session.healthScore}"
                        HealthStatus.POOR -> "Poor 😟 ${session.healthScore}"
                        HealthStatus.CRITICAL -> "Critical 🔴 ${session.healthScore}"
                    })

                    // Expandable Legend
                    var expandLegend by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandLegend = !expandLegend },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Emoji legend", style = MaterialTheme.typography.titleMedium)
                        Icon(if (expandLegend) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                    }
                    if (expandLegend) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🟢 80-100  Excellent", style = MaterialTheme.typography.bodyLarge)
                            Text("😊 60-79   Good", style = MaterialTheme.typography.bodyLarge)
                            Text("😐 40-59   Fair", style = MaterialTheme.typography.bodyLarge)
                            Text("😟 20-39   Poor", style = MaterialTheme.typography.bodyLarge)
                            Text("🔴 0-19   Critical", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { 
                        HapticUtils.triggerLightFeedback(haptic, context)
                        showConfirmDelete = true 
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.SemiBold)
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
                ) { Text("Close", fontWeight = FontWeight.Medium) }
            }
        },
        dismissButton = {}
    )

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text("Delete session?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            },
            text = { Text("This will permanently delete this charging session.") },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ElevatedButton(
                        onClick = {
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            coroutineScope.launch { viewModel.deleteSession(session) }
                            showConfirmDelete = false
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = Color.White
                        )
                    ) { Text("Delete", fontWeight = FontWeight.Bold) }
                    OutlinedButton(
                        onClick = { 
                            HapticUtils.triggerLightFeedback(haptic, context)
                            showConfirmDelete = false 
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Cancel") }
                }
            },
            dismissButton = {}
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

// No demo data now – screen shows empty state until a real session is recorded

// Composable for current discharge / not-charging card
@Composable
private fun DischargeCard(state: DischargeState) {
    var showDialog by remember { mutableStateOf(false) }
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val descriptionText = if (state.isCharging) {
        "${state.percentage}% • ${state.connection} • ${state.temperature.roundToInt()}°C"
    } else {
        "${state.percentage}% • ${state.temperature.roundToInt()}°C"
    }

    WideFeatureCard(
        title = "Battery",
        description = descriptionText,
        icon = rememberEmojiPainter("⬇️", fontSizeDp = 36f),
        onClick = { showDialog = true },
        height = 140,
        iconSize = 40,
        isServiceActive = true,
        skipConfirmation = true,
        iconTint = Color.Unspecified,
        modifier = Modifier.fillMaxWidth()
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "🔋 Current Battery",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) MaterialTheme.colorScheme.surfaceContainer else Color(0xFFF8F5FF)
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        DetailRow("Status", if (state.isCharging) "Charging" else "Not Charging")
                        if (state.isCharging) {
                            DetailRow("Connection", state.connection)
                        }
                        DetailRow("Temperature", "${state.temperature.roundToInt()}°C")
                        DetailRow("Health", "${state.healthTierString}")
                        DetailRow("Cycle Count", state.cycleCount?.toString() ?: "Not available")
                    }
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = { 
                        HapticUtils.triggerLightFeedback(haptic, context)
                        showDialog = false 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Close", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {}
        )
    }
}

private fun queryBatteryState(context: android.content.Context): DischargeState {
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val pct = if (level >= 0 && scale > 0) level * 100 / scale else 0

    val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
    val tempC = tempTenths / 10f

    val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

    val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
    val conn = when (plugged) {
        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
        BatteryManager.BATTERY_PLUGGED_AC -> "AC"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
        else -> "--"
    }

    // Get battery cycle count (Android 14+ API level 34+)
    val cycleCount = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        intent?.getIntExtra("android.os.extra.CYCLE_COUNT", -1).takeIf { it != -1 }
    } else {
        null
    }

    // Simple live health score (temp only)
    val tempPenalty = if (tempC > 35) ((tempC - 35) * 2).toInt() else 0
    var score = 100 - tempPenalty
    score = score.coerceIn(0, 100)

    val tierString = when (score) {
        in 80..100 -> "Excellent 🟢 $score"
        in 60..79 -> "Good 😊 $score"
        in 40..59 -> "Fair 😐 $score"
        in 20..39 -> "Poor 😟 $score"
        else -> "Critical 🔴 $score"
    }

    return DischargeState(pct, tempC, isCharging, conn, score, tierString, cycleCount)
}

// Data class representing current battery state summary
private data class DischargeState(
    val percentage: Int,
    val temperature: Float,
    val isCharging: Boolean,
    val connection: String,
    val healthScore: Int,
    val healthTierString: String,
    val cycleCount: Int? = null
)

// ---- Shared Battery Health explanation dialog ----

@Composable
fun HealthInfoDialog(onDismiss: () -> Unit) {
    val themeState = LocalThemeState.current

    AlertDialog(
        onDismissRequest = { /*block back*/ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🧐 Battery Health",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "How the score works",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) MaterialTheme.colorScheme.surfaceContainer else Color(0xFFF8F5FF)
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📊 Formula:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("100 – temperature penalty – speed penalty – battery-age penalty", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)

                        Text("📕 Details:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("• +2 penalty for each °C above 35 °C\n• Penalty when %/min < 0.7\n• Older batteries (capacity < 95 %) subtract a few points", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
                    }
                }

                // Emoji legend
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) MaterialTheme.colorScheme.surfaceContainer else Color(0xFFF8F5FF)
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🟢 80-100  Excellent – Cool & efficient", style = MaterialTheme.typography.bodyMedium)
                        Text("😊 60-79   Good – Minor penalties", style = MaterialTheme.typography.bodyMedium)
                        Text("😐 40-59   Fair – Getting warm", style = MaterialTheme.typography.bodyMedium)
                        Text("😟 20-39   Poor – Hot or slow", style = MaterialTheme.typography.bodyMedium)
                        Text("🔴 0-19   Critical – Stop charging!", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF2D4A3E)
                            AppThemeStyle.CLASSIC -> Color(0xFF8D7BA5)
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        },
                        contentColor = when (themeState.themeStyle) {
                            AppThemeStyle.CLASSIC -> Color.White
                            AppThemeStyle.AMOLED -> Color.White
                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Got it", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        dismissButton = {}
    )
} 