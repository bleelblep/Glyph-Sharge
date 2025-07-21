package com.bleelblep.glyphsharge.ui.screens

import android.content.pm.PackageManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bleelblep.glyphsharge.ui.components.*
import com.bleelblep.glyphsharge.ui.theme.*
import com.bleelblep.glyphsharge.ui.utils.HapticUtils
import kotlin.math.roundToInt
import androidx.compose.ui.input.nestedscroll.nestedScroll

/**
 * Settings screen for the GlyphZen app.
 * Allows users to modify app preferences and view app information.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onHiddenSettingsAccess: (() -> Unit)? = null,
    onThemeSettingsClick: () -> Unit = {},
    onFontSettingsClick: () -> Unit = {},
    onVibrationSettingsClick: () -> Unit = {},
    onQuietHoursSettingsClick: () -> Unit = {},
    settingsRepository: SettingsRepository
) {
    val fontState = LocalFontState.current
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val backgroundColorMain = MaterialTheme.colorScheme.background

    // Create scroll state for the LazyColumn
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scrollState = rememberLazyListState()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Settings",
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
        containerColor = backgroundColorMain,
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        LazyColumn(
            state = scrollState, // Use the scroll state
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColorMain)
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            // Optimize for smooth scrolling
            userScrollEnabled = true
        ) {
            // Typography Settings Card with Drag Navigation
            item {
                var offsetX by remember { mutableStateOf(0f) }
                val configuration = LocalConfiguration.current
                val density = LocalDensity.current
                
                // Memoize expensive screen width calculation
                val dragThreshold = remember(configuration.screenWidthDp) {
                    with(density) { configuration.screenWidthDp.dp.toPx() / 2f }
                }

                // Track if threshold was met
                var thresholdMet by remember { mutableStateOf(false) }

                // Animate the offset back to center when released
                val animatedOffsetX by animateFloatAsState(
                    targetValue = offsetX,
                    animationSpec = if (thresholdMet) {
                        // Fast, non-bouncy animation when threshold is met
                        tween(
                            durationMillis = 200,
                            easing = FastOutLinearInEasing
                        )
                    } else {
                        // Spring animation when threshold is not met
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    },
                    label = "fontDragOffset",
                    finishedListener = { finalValue ->
                        // Only trigger navigation after animation completes and we're at center
                        if (thresholdMet && finalValue == 0f) {
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            onFontSettingsClick()
                            thresholdMet = false
                        }
                    }
                )

                // Calculate progress based on drag distance (half screen width)
                val dragProgress = (kotlin.math.abs(animatedOffsetX) / dragThreshold).coerceIn(0f, 1f)

                // Animate background color
                val backgroundColor by animateColorAsState(
                    targetValue = lerp(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer,
                        dragProgress
                    ),
                    animationSpec = tween(300),
                    label = "fontBackgroundColor"
                )

                // Animate elevation
                val animatedElevation by animateDpAsState(
                    targetValue = (1 + dragProgress * 8).dp,
                    animationSpec = tween(300),
                    label = "fontElevation"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { _ ->
                                    // Light feedback on drag start
                                    HapticUtils.triggerLightFeedback(haptic, context)
                                },
                                onDragEnd = {
                                    // Check if threshold was met
                                    thresholdMet = kotlin.math.abs(offsetX) >= dragThreshold
                                    // Always snap back to center
                                    offsetX = 0f
                                }
                            ) { _, dragAmount ->
                                offsetX += dragAmount.x
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Typography",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = fontState.getFontDescription() + if (!fontState.useCustomFonts) 
                                    " • Custom sizing" 
                                else if (fontState.fontSizeSettings != FontSizeSettings())
                                    " • Custom sizing"
                                else "",
                                style = MaterialTheme.typography.bodyMedium,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Three-state font morphing toggle - positioned consistently with other cards
                        if (fontState.useCustomFonts) {
                            ThreeStateFontMorphingButton(
                                currentVariant = fontState.currentVariant,
                                onVariantSelected = { variant ->
                                    fontState.setFontVariant(variant)
                                }
                            )
                        }
                    }
                }
            }

            // Theme Settings Card  
            item {
                var offsetX by remember { mutableStateOf(0f) }
                val configuration = LocalConfiguration.current
                val density = LocalDensity.current
                
                // Calculate half screen width as threshold
                val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
                val dragThreshold = screenWidthPx / 2f

                // Track if threshold was met
                var thresholdMet by remember { mutableStateOf(false) }

                // Animate the offset back to center when released
                val animatedOffsetX by animateFloatAsState(
                    targetValue = offsetX,
                    animationSpec = if (thresholdMet) {
                        // Fast, non-bouncy animation when threshold is met
                        tween(
                            durationMillis = 200,
                            easing = FastOutLinearInEasing
                        )
                    } else {
                        // Spring animation when threshold is not met
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    },
                    label = "themeDragOffset",
                    finishedListener = { finalValue ->
                        // Only trigger navigation after animation completes and we're at center
                        if (thresholdMet && finalValue == 0f) {
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            onThemeSettingsClick()
                            thresholdMet = false
                        }
                    }
                )

                // Calculate progress based on drag distance (half screen width)
                val dragProgress = (kotlin.math.abs(animatedOffsetX) / dragThreshold).coerceIn(0f, 1f)

                // Animate background color
                val backgroundColor by animateColorAsState(
                    targetValue = lerp(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer,
                        dragProgress
                    ),
                    animationSpec = tween(300),
                    label = "themeBackgroundColor"
                )

                // Animate elevation
                val animatedElevation by animateDpAsState(
                    targetValue = (1 + dragProgress * 8).dp,
                    animationSpec = tween(300),
                    label = "themeElevation"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { _ ->
                                    // Light feedback on drag start
                                    HapticUtils.triggerLightFeedback(haptic, context)
                                },
                                onDragEnd = {
                                    // Check if threshold was met
                                    thresholdMet = kotlin.math.abs(offsetX) >= dragThreshold
                                    // Always snap back to center
                                    offsetX = 0f
                                }
                            ) { _, dragAmount ->
                                offsetX += dragAmount.x
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Theme",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (themeState.isDarkTheme) "Dark Mode" else "Light Mode",
                                style = MaterialTheme.typography.bodyMedium,
                                letterSpacing = 0.5.sp
                            )
                        }

                        MorphingToggleButton(
                            checked = themeState.isDarkTheme,
                            onCheckedChange = {
                                themeState.toggleTheme()
                            },
                            enabledIcon = {
                                Text(
                                    text = "🌑",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            disabledIcon = {
                                Text(
                                    text = "☀️",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        )
                    }
                }
            }

            // Quiet Hours Settings Card (draggable with toggle)
            item {
                var offsetX by remember { mutableStateOf(0f) }
                val configuration = LocalConfiguration.current
                val density = LocalDensity.current
                
                // Get quiet hours state
                var quietHoursEnabled by remember { 
                    mutableStateOf(settingsRepository.isQuietHoursEnabled()) 
                }
                
                // Calculate half screen width as threshold
                val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
                val dragThreshold = screenWidthPx / 2f

                // Track if threshold was met
                var thresholdMet by remember { mutableStateOf(false) }

                // Animate the offset back to center when released
                val animatedOffsetX by animateFloatAsState(
                    targetValue = offsetX,
                    animationSpec = if (thresholdMet) {
                        // Fast, non-bouncy animation when threshold is met
                        tween(
                            durationMillis = 200,
                            easing = FastOutLinearInEasing
                        )
                    } else {
                        // Spring animation when threshold is not met
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    },
                    label = "quietHoursSettingsDragOffset",
                    finishedListener = { finalValue ->
                        // Only trigger navigation after animation completes and we're at center
                        if (thresholdMet && finalValue == 0f) {
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            onQuietHoursSettingsClick()
                            thresholdMet = false
                        }
                    }
                )

                // Calculate progress based on drag distance (half screen width)
                val dragProgress = (kotlin.math.abs(animatedOffsetX) / dragThreshold).coerceIn(0f, 1f)

                // Animate background color
                val backgroundColor by animateColorAsState(
                    targetValue = lerp(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer,
                        dragProgress
                    ),
                    animationSpec = tween(300),
                    label = "quietHoursSettingsBackgroundColor"
                )

                // Animate elevation
                val animatedElevation by animateDpAsState(
                    targetValue = (1 + dragProgress * 8).dp,
                    animationSpec = tween(300),
                    label = "quietHoursSettingsElevation"
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { _ ->
                                    // Light feedback on drag start
                                    HapticUtils.triggerLightFeedback(haptic, context)
                                },
                                onDragEnd = {
                                    // Check if threshold was met
                                    thresholdMet = kotlin.math.abs(offsetX) >= dragThreshold
                                    // Always snap back to center
                                    offsetX = 0f
                                }
                            ) { _, dragAmount ->
                                offsetX += dragAmount.x
                            }
                        },
                    colors = CardDefaults.cardColors(containerColor = backgroundColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Quiet Hours",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = if (quietHoursEnabled) "Glyphs will be quiet during set hours" else "Choose your Glyph lights schedule",
                                style = MaterialTheme.typography.bodyMedium,
                                letterSpacing = 0.5.sp
                            )
                        }

                        MorphingToggleButton(
                            checked = quietHoursEnabled,
                            onCheckedChange = { enabled ->
                                quietHoursEnabled = enabled
                                settingsRepository.saveQuietHoursEnabled(enabled)
                            },
                            enabledIcon = {
                                Text(
                                    text = "🔇",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            disabledIcon = {
                                Text(
                                    text = "💡",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        )
                    }
                }
            }

            // Haptic/Vibration Settings Card
            if (false) { // showVibrationCard is removed, so this card is always shown
                item {
                    HapticSettingsCard(
                        settingsRepository = settingsRepository,
                        onNavigateToFullSettings = onVibrationSettingsClick
                    )
                }
            }

            // App Info Section Header
            item {
                HomeSectionHeader(
                    title = "App Information",
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            // App Info Card
            item {
                BetaAttributeCard()
            }

            // About Card with Hidden Settings Access
            item {
                HiddenSettingsDragCard(
                    onHiddenSettingsAccess = onHiddenSettingsAccess
                )
            }
        }
    }
}

/**
 * About card with hidden drag interaction for accessing developer settings
 * Requires horizontal drag with specific distance to trigger hidden menu
 */
@Composable
private fun HiddenSettingsDragCard(
    onHiddenSettingsAccess: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val themeState = LocalThemeState.current
    
    // Calculate half screen width as threshold
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val dragThreshold = screenWidthPx / 2f

    // Track if threshold was met
    var thresholdMet by remember { mutableStateOf(false) }

    // Animate the offset back to center when released
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = if (thresholdMet) {
            // Fast, non-bouncy animation when threshold is met
            tween(
                durationMillis = 200,
                easing = FastOutLinearInEasing
            )
        } else {
            // Spring animation when threshold is not met
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "hiddenDragOffset",
        finishedListener = { finalValue ->
            // Only trigger callback after animation completes and we're at center
            if (thresholdMet && finalValue == 0f && onHiddenSettingsAccess != null) {
                HapticUtils.triggerMediumFeedback(haptic, context)
                onHiddenSettingsAccess()
                thresholdMet = false
            }
        }
    )

    // Calculate progress based on drag distance (half screen width)
    val dragProgress = (kotlin.math.abs(animatedOffsetX) / dragThreshold).coerceIn(0f, 1f)

    // Animate background color
    val backgroundColor by animateColorAsState(
        targetValue = lerp(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primaryContainer,
            dragProgress
        ),
        animationSpec = tween(300),
        label = "backgroundColor"
    )

    // Animate elevation
    val animatedElevation by animateDpAsState(
        targetValue = (1 + dragProgress * 8).dp,
        animationSpec = tween(300),
        label = "elevation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { _ ->
                        // Light feedback on drag start
                        HapticUtils.triggerLightFeedback(haptic, context)
                    },
                    onDragEnd = {
                        // Check if threshold was met
                        thresholdMet = kotlin.math.abs(offsetX) >= dragThreshold
                        // Always snap back to center
                        offsetX = 0f
                    }
                ) { _, dragAmount ->
                    offsetX += dragAmount.x
                }
            },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = when (themeState.themeStyle) {
                        AppThemeStyle.Y2K -> MaterialTheme.colorScheme.primary
                        AppThemeStyle.NEON -> MaterialTheme.colorScheme.primary
                        AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                // Show drag progress
               // if (dragProgress > 0.3f) {
               //     Spacer(modifier = Modifier.weight(1f))
               //     Text(
               //         text = "${(dragProgress * 100).toInt()}%",
               //         style = MaterialTheme.typography.titleMedium,
               //         color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
               //     )
               // }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "From live battery glyphs to USB-theft alarms, charging history and unlock light-shows—unlock the full power of the NOTHING glyphs.",
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }
    }
}

// ------------------------------------------------------------
// Custom attribute card that toggles its supporting text when tapped
// ------------------------------------------------------------

@Composable
private fun BetaAttributeCard(modifier: Modifier = Modifier) {
    val themeState = LocalThemeState.current
    val context = LocalContext.current
    var toggled by rememberSaveable { mutableStateOf(false) }
    
    // Get version information dynamically
    val versionInfo = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.longVersionCode})"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown Version"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { toggled = !toggled },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header row with icon and title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = when (themeState.themeStyle) {
                        AppThemeStyle.Y2K, AppThemeStyle.NEON -> MaterialTheme.colorScheme.primary
                        AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Glyph Sharge",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary attribute label
            Text(
                text = "v$versionInfo",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Supporting text that toggles on tap
            AnimatedContent(targetState = toggled, label = "betaToggle") { isAlt ->
                val body = if (isAlt) {
                    "Special thanks to beedah for the countless installs and quick beta testing."
                } else {
                    "🧪 You're using a public release build. Expect occasional quirks and bugs."
                }
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Requires Android 14+",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
        }
    }
} 