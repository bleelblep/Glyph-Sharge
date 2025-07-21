package com.bleelblep.glyphsharge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.bleelblep.glyphsharge.ui.components.*
import com.bleelblep.glyphsharge.ui.theme.*
import com.bleelblep.glyphsharge.ui.utils.HapticUtils
import androidx.compose.material3.rememberTopAppBarState

/**
 * Font Settings Screen with comprehensive customization options
 * Updated to follow consistent UI patterns throughout the app
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontSettingsScreen(
    fontState: FontState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scrollState = rememberLazyListState()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Typography",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 42.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        HapticUtils.triggerLightFeedback(haptic, context)
                        onNavigateBack() 
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
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            userScrollEnabled = true
        ) {
            // Custom Fonts Toggle Card
            item {
                ToggleCard(
                    title = "Custom Fonts",
                    checked = fontState.useCustomFonts,
                    onCheckedChange = {
                        HapticUtils.triggerLightFeedback(haptic, context)
                        fontState.toggleCustomFonts()
                    },
                    statusText = { if (it) "Enabled" else "System Default" }
                )
            }

            // Font Family Selection (only show if custom fonts enabled)
            if (fontState.useCustomFonts) {
                item {
                    HomeSectionHeader(
                        title = "Font Family",
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                    )
                }
                
                item {
                    SimpleFontSelector(
                        currentVariant = fontState.currentVariant,
                        onVariantSelected = { variant ->
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            fontState.setFontVariant(variant)
                        }
                    )
                }
            }

            // Font Size Section Header
            item {
                HomeSectionHeader(
                    title = "Font Size Settings",
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            // Font Size Controls
            item {
                FontSizeControls(
                    fontSizeSettings = fontState.fontSizeSettings,
                    onSizeChanged = { category, scale ->
                        HapticUtils.triggerLightFeedback(haptic, context)
                        fontState.updateFontSize(category, scale)
                    },
                    onReset = {
                        HapticUtils.triggerMediumFeedback(haptic, context)
                        fontState.resetFontSizes()
                    }
                )
            }

            // Preview Section Header
            item {
                HomeSectionHeader(
                    title = "Preview",
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            // Preview Section
            item {
                FontPreview(fontState = fontState)
            }
        }
    }
} 