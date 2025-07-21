package com.bleelblep.glyphsharge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.WindowInsets
import com.bleelblep.glyphsharge.ui.components.SquareFeatureCard
import com.bleelblep.glyphsharge.ui.components.FeatureGrid
import com.bleelblep.glyphsharge.ui.components.ActionCard
import com.bleelblep.glyphsharge.R
import androidx.compose.ui.res.painterResource
import com.bleelblep.glyphsharge.ui.theme.LocalThemeState
import com.bleelblep.glyphsharge.ui.theme.ThemeState
import com.bleelblep.glyphsharge.ui.components.FeatureCard
import androidx.compose.ui.graphics.painter.Painter
import com.bleelblep.glyphsharge.ui.theme.AppThemeStyle
import com.bleelblep.glyphsharge.ui.components.ContentCard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import com.bleelblep.glyphsharge.ui.utils.HapticUtils
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import com.bleelblep.glyphsharge.ui.theme.NothingRed
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.sp
import androidx.compose.material3.rememberTopAppBarState

/**
 * Theme-specific feature card that bypasses service checks and dialogs
 * Provides immediate execution for theme switching
 */
@Composable
private fun ThemeFeatureCard(
    title: String,
    description: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Int = 40
) {
    val themeState = LocalThemeState.current
    FeatureCard(
        title = title,
        description = description,
        icon = icon,
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        iconSize = iconSize,
        contentPadding = PaddingValues(16.dp),
        iconTint = when (themeState.themeStyle) {
            AppThemeStyle.Y2K -> MaterialTheme.colorScheme.primary
            AppThemeStyle.NEON -> MaterialTheme.colorScheme.primary
            AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
            else -> MaterialTheme.colorScheme.primary
        }
    )
}

/**
 * Theme reset card that bypasses dialogs and directly resets theme
 */
@Composable
private fun ThemeResetCard(
    title: String,
    description: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Int = 140,
    iconSize: Int = 32
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val themeState = LocalThemeState.current
    val coroutineScope = rememberCoroutineScope()
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )

    ContentCard(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        onClick = {
            isPressed = true
            HapticUtils.triggerLightFeedback(haptic, context)
            onClick()
            // Reset pressed state after animation
            coroutineScope.launch {
                delay(150)
                isPressed = false
            }
        }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = icon,
                contentDescription = title,
                tint = when (themeState.themeStyle) {
                    AppThemeStyle.Y2K -> MaterialTheme.colorScheme.primary
                    AppThemeStyle.NEON -> MaterialTheme.colorScheme.primary
                    AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                    else -> MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(iconSize.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scrollState = rememberLazyListState()
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Theme",
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
            // Theme Mode Section Header
            item {
                            Text(
                    text = "Theme Mode",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 8.dp)
                )
            }

            // Light/Dark Theme Cards Grid
            item {
                FeatureGrid {
                    // Light Theme Card
                    ThemeFeatureCard(
                        title = "Light Theme",
                        description = "Clean and bright interface for daytime use",
                        icon = painterResource(id = R.drawable.light_mode_24px),
                        onClick = { 
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            themeState.setDarkTheme(false) 
                        },
                        modifier = Modifier.weight(1f),
                        iconSize = 40
                    )

                    // Dark Theme Card
                    ThemeFeatureCard(
                        title = "Dark Theme",
                        description = "Easy on the eyes for low-light environments",
                        icon = painterResource(id = R.drawable.dark_mode_24px),
                        onClick = { 
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            themeState.setDarkTheme(true) 
                        },
                        modifier = Modifier.weight(1f),
                        iconSize = 40
                    )
                }
            }

            // Theme Style Section Header
            item {
                Text(
                    text = "Theme Styles",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 8.dp)
                )
            }

            // Theme Style Cards Grid - Row 1
            item {
                FeatureGrid {
                    // Y2K Theme Card
                    ThemeFeatureCard(
                        title = "Y2K",
                        description = "Chrome and cyber aesthetics with futuristic colors",
                        icon = painterResource(id = R.drawable.palette_24px),
                        onClick = { 
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            themeState.setThemeStyle(AppThemeStyle.Y2K) 
                        },
                        modifier = Modifier.weight(1f),
                        iconSize = 40
                    )

                    // Neon Theme Card
                    ThemeFeatureCard(
                        title = "Neon",
                        description = "Electric colors inspired by cyberpunk aesthetics",
                        icon = painterResource(id = R.drawable.invert_colors_24px),
                        onClick = { 
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            themeState.setThemeStyle(AppThemeStyle.NEON) 
                        },
                        modifier = Modifier.weight(1f),
                        iconSize = 40
                    )
                }
            }

            // Theme Style Cards Grid - Row 2
            item {
                FeatureGrid {
                    // AMOLED Theme Card
                    ThemeFeatureCard(
                        title = "NOTHING",
                        description = "True black OLED theme with Nothing accents",
                        icon = painterResource(id = R.drawable.dark_mode_24px),
                        onClick = { 
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            themeState.setThemeStyle(AppThemeStyle.AMOLED) 
                        },
                        modifier = Modifier.weight(1f),
                        iconSize = 40
                    )

                    // Pastel Theme Card
                    ThemeFeatureCard(
                        title = "Pastel",
                        description = "Soft and soothing colors for a gentle experience",
                        icon = painterResource(id = R.drawable.light_mode_24px),
                        onClick = { 
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            themeState.setThemeStyle(AppThemeStyle.PASTEL) 
                        },
                        modifier = Modifier.weight(1f),
                        iconSize = 40
                    )
                }
            }

            // Theme Style Cards Grid - Row 3
            item {
                FeatureGrid {
                    // Classic Theme Card
                    ThemeFeatureCard(
                        title = "Classic",
                        description = "Original Nothing Phone colors and design",
                        icon = painterResource(id = R.drawable.palette_24px),
                        onClick = { 
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            themeState.setThemeStyle(AppThemeStyle.CLASSIC) 
                        },
                        modifier = Modifier.weight(1f),
                        iconSize = 40
                    )

                    // Expressive Theme Card
                    ThemeFeatureCard(
                        title = "Expressive",
                        description = "Material 3 expressive shapes & colors",
                        icon = painterResource(id = R.drawable.extension_24px),
                        onClick = { 
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            themeState.setThemeStyle(AppThemeStyle.EXPRESSIVE) 
                        },
                        modifier = Modifier.weight(1f),
                        iconSize = 40
                    )
                }
            }

            // Feature Card Section
            item {
                ThemeResetCard(
                    title = "Reset to Default",
                    description = "Restore the original Classic theme with light mode settings to get back to the stock appearance.",
                    icon = painterResource(id = R.drawable.palette_24px),
                    onClick = { 
                        HapticUtils.triggerMediumFeedback(haptic, context)
                        // Reset to default Classic theme and light mode
                        themeState.setThemeStyle(AppThemeStyle.CLASSIC)
                        themeState.setDarkTheme(false) // Always set light mode
                    }
                )
            }

            // Additional Feature Cards Section Header
            item {
                Text(
                    text = "Additional Options",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 8.dp)
                )
            }

            // Additional Feature Cards Grid
            item {
                FeatureGrid {
                    // First Additional Card
                    SquareFeatureCard(
                        title = "Color Scheme",
                        description = "Advanced color customization options for power users",
                        icon = painterResource(id = R.drawable.palette_24px),
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        iconSize = 40
                    )

                    // Second Additional Card
                    SquareFeatureCard(
                        title = "Accent Colors",
                        description = "Fine-tune accent and highlight colors throughout the app",
                        icon = painterResource(id = R.drawable.invert_colors_24px),
                        onClick = { },
                        modifier = Modifier.weight(1f),
                        iconSize = 40
                    )
                }
            }
        }
    }
} 