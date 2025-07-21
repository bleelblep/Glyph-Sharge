package com.bleelblep.glyphsharge.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import com.bleelblep.glyphsharge.R
import com.bleelblep.glyphsharge.ui.theme.LocalFontState
import com.bleelblep.glyphsharge.ui.utils.HapticUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * LED Calibration screen with Zone Test and Pattern Test
 * Bypasses service state checks and dialogs for direct testing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LEDCalibrationScreen(
    onBackClick: () -> Unit,
    onTestAllZones: () -> Unit,
    onTestCustomPattern: () -> Unit,
    onTestChannel: (Int) -> Unit,
    onTestC1Segment: (Int) -> Unit,
    onTestFinalState: () -> Unit,
    onTestC14C15Isolated: () -> Unit,
    onTestD1Sequential: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

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
                        text = "LED Calibration",
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LED Calibration Tools",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Direct Hardware Testing",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Test individual LED zones and patterns directly without confirmation dialogs. Service state is bypassed for immediate testing.",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Test Cards Section Header
            item {
                HomeSectionHeader(title = "Testing Tools")
            }

            // Zone Test and Pattern Test Side by Side
            item {
                FeatureGrid {
                    // Zone Test Card - Direct execution
                    CalibrationFeatureCard(
                        title = "Zone Test",
                        description = "Test each LED zone individually",
                        icon = painterResource(id = R.drawable.graph_6_24px),
                        onClick = onTestAllZones,
                        modifier = Modifier.weight(1f),
                        iconSize = 40
                    )

                    // Pattern Test Card - Direct execution
                    CalibrationFeatureCard(
                        title = "Pattern Test",
                        description = "Test custom LED patterns",
                        icon = painterResource(id = R.drawable.extension_24px),
                        onClick = onTestCustomPattern,
                        modifier = Modifier.weight(1f),
                        iconSize = 40
                    )
                }
            }

            // Individual Channel Testing
            item {
                IndividualChannelCard(onTestChannel = onTestChannel)
            }

            // C1 Individual LED Testing
            item {
                C1IndividualCard(onTestC1Segment = onTestC1Segment)
            }

            // D1 Sequential Testing
            item {
                D1SequentialCard(onTestD1Sequential = onTestD1Sequential)
            }

            // Final State Debug Testing
            item {
                FinalStateDebugCard(
                    onTestFinalState = onTestFinalState,
                    onTestC14C15Isolated = onTestC14C15Isolated
                )
            }
        }
    }
}

/**
 * Calibration-specific feature card that bypasses service checks and dialogs
 * Provides immediate execution for LED testing
 */
@Composable
fun CalibrationFeatureCard(
    title: String,
    description: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Int = 40
) {
    FeatureCard(
        title = title,
        description = description,
        icon = icon,
        onClick = onClick, // Direct execution without any checks or dialogs
        modifier = modifier.aspectRatio(1f),
        iconSize = iconSize,
        contentPadding = PaddingValues(16.dp)
    )
}

/**
 * Static card for individual Glyph channel testing
 * Contains 8 zone-based testing buttons
 */
@Composable
private fun IndividualChannelCard(
    onTestChannel: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Individual Zone Testing",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Test each Nothing phone Glyph zone functionally",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Grid of zone buttons (8 zones arranged in rows)
            val zoneNames = listOf(
                "Camera Zone", "Top Strip", "C1 Ring", "Other C",
                "E Segment", "Bottom Ring", "Top Zones", "All Zones"
            )

            // Arrange in rows of 4, 4
            for (rowIndex in 0 until 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val startIndex = rowIndex * 4
                    val endIndex = minOf(startIndex + 4, zoneNames.size)

                    for (i in startIndex until endIndex) {
                        ChannelButton(
                            channelIndex = i + 1,
                            channelName = zoneNames[i],
                            onTestChannel = onTestChannel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (rowIndex < 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Individual channel testing button with morphing press animation
 */
@Composable
private fun ChannelButton(
    channelIndex: Int,
    channelName: String,
    onTestChannel: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val fontState = LocalFontState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    OutlinedButton(
        onClick = {
            isPressed = true
            HapticUtils.triggerLightFeedback(haptic, context)
            onTestChannel(channelIndex)
            GlobalScope.launch {
                delay(150)
                isPressed = false
            }
        },
        modifier = modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = if (isPressed) 0.95f else 1f
                scaleY = if (isPressed) 0.95f else 1f
            },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isPressed) 
                Color(0xFF8B0000) // Dark red when pressed
            else 
                Color(0xFFE53E3E), // glyphzenred background
            contentColor = Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isPressed)
                Color(0xFF8B0000) // Dark red border when pressed
            else
                Color(0xFFE53E3E) // glyphzenred border
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Z$channelIndex",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = fontState.getTitleFont()
                ),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = channelName,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                color = Color.White
            )
        }
    }
}

/**
 * Static card for individual C1 LED testing
 * Contains 16 buttons for each C1 segment (C1_1 to C1_16)
 */
@Composable
private fun C1IndividualCard(
    onTestC1Segment: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "C1 Individual LED Testing",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Test each C1 ring LED individually (Phone 2 only)",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Grid of C1 buttons (16 LEDs arranged in rows of 4)
            for (rowIndex in 0 until 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val startIndex = rowIndex * 4 + 1 // 1-based indexing
                    val endIndex = minOf(startIndex + 4, 17) // Up to C1_16

                    for (i in startIndex until endIndex) {
                        C1Button(
                            c1Index = i,
                            onTestC1Segment = onTestC1Segment,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (rowIndex < 3) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Individual C1 LED testing button with morphing press animation
 */
@Composable
private fun C1Button(
    c1Index: Int,
    onTestC1Segment: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val fontState = LocalFontState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    OutlinedButton(
        onClick = {
            isPressed = true
            HapticUtils.triggerLightFeedback(haptic, context)
            onTestC1Segment(c1Index)
            GlobalScope.launch {
                delay(150)
                isPressed = false
            }
        },
        modifier = modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = if (isPressed) 0.95f else 1f
                scaleY = if (isPressed) 0.95f else 1f
            },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isPressed) 
                Color(0xFF4A90E2) // Darker blue when pressed
            else 
                Color(0xFF007AFF), // Blue background for C1
            contentColor = Color.White
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isPressed)
                Color(0xFF4A90E2) // Darker blue border when pressed
            else
                Color(0xFF007AFF) // Blue border
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "C1_$c1Index",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = fontState.getTitleFont()
                ),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "LED $c1Index",
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                color = Color.White
            )
        }
    }
}

/**
 * Static card for D1 sequential testing
 */
@Composable
private fun D1SequentialCard(
    onTestD1Sequential: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "D1 Sequential Testing",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Test D1 sequential animation pattern",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    HapticUtils.triggerMediumFeedback(haptic, context)
                    onTestD1Sequential()
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50), // Green color for D1
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Run D1 Sequential Animation",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * Static card for final state debug testing
 */
@Composable
private fun FinalStateDebugCard(
    onTestFinalState: () -> Unit,
    onTestC14C15Isolated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "C14 & C15 Debug Testing",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Test C14 & C15 behavior in different scenarios",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    HapticUtils.triggerMediumFeedback(haptic, context)
                    onTestFinalState()
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Final State (C14/C15 Excluded)",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    HapticUtils.triggerMediumFeedback(haptic, context)
                    onTestC14C15Isolated()
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "C14 & C15 Only (Isolated)",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
} 