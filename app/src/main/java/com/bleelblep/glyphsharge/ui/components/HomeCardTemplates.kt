package com.bleelblep.glyphsharge.ui.components

// Android imports
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.PowerManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes

// Compose imports
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.DialogProperties

// Project imports
import com.bleelblep.glyphsharge.R
import com.bleelblep.glyphsharge.di.GlyphComponent
import com.bleelblep.glyphsharge.glyph.*
import com.bleelblep.glyphsharge.glyph.GlyphAnimationManager
import com.bleelblep.glyphsharge.ui.theme.*
import com.bleelblep.glyphsharge.ui.utils.HapticUtils
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.*
import java.io.*

// Constant brand colours for status tinting
private val ACTIVE_GREEN = Color(0xFF00C853)  // Material A700 Green
private val INACTIVE_RED = Color(0xFFFF5252)  // Material A200 Red

/**
 * Home-specific card templates for the GlyphZen app
 * These templates are optimized for the main homepage layout and interactions
 */

/**
 * Toggle card template for switches and boolean controls
 */
@Composable
fun ToggleCard(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    statusText: (Boolean) -> String = { if (it) "Enabled" else "Disabled" },
    customContent: (@Composable ColumnScope.() -> Unit)? = null,
    @DrawableRes illustrationRes: Int? = null
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    ContentCard(
        title = title,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Toggle section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = statusText(checked),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            Switch(
                checked = checked,
                onCheckedChange = { newValue ->
                    HapticUtils.triggerLightFeedback(haptic, context)
                    onCheckedChange(newValue)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }

        // Custom content section
        if (customContent != null) {
            Spacer(modifier = Modifier.weight(1f))
            customContent()
        }

        // Illustration section
        if (illustrationRes != null) {
            if (customContent == null) {
                Spacer(modifier = Modifier.weight(1f))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = illustrationRes),
                    contentDescription = "Illustration for $title",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

/**
 * Feature card template for action-based cards with icons
 */
@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Int = 32,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    iconTint: Color? = null
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val themeState = LocalThemeState.current
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "pressScale"
    )

    ContentCard(
        modifier = modifier
            .scale(pressScale),
        onClick = {
            isPressed = true
            HapticUtils.triggerLightFeedback(haptic, context)
            onClick()
            // Reset pressed state after animation
            kotlinx.coroutines.GlobalScope.launch {
                kotlinx.coroutines.delay(150)
                isPressed = false
            }
        },
        contentPadding = contentPadding
    ) {
        Icon(
            painter = icon,
            contentDescription = title,
            tint = iconTint ?: when {
                title.contains("Breathing", ignoreCase = true) && themeState.themeStyle == AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                title.contains("Guard", ignoreCase = true) && themeState.themeStyle == AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                title.contains("Peek", ignoreCase = true) && themeState.themeStyle == AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                title.contains("Pulse", ignoreCase = true) && themeState.themeStyle == AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                title.contains("Battery", ignoreCase = true) && themeState.themeStyle == AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                title.contains("Information", ignoreCase = true) && themeState.themeStyle == AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                else -> MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(iconSize.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Square feature card template for grid layouts
 */
@Composable
fun SquareFeatureCard(
    title: String,
    description: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Int = 40,
    isServiceActive: Boolean = true,
    skipConfirmation: Boolean = false,
    iconTint: Color? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val glyphAnimationManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            GlyphComponent::class.java
        ).glyphAnimationManager()
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale",
        finishedListener = { finalValue ->
            if (finalValue == 0.95f && isPressed && isServiceActive) {
                coroutineScope.launch {
                    kotlinx.coroutines.delay(150)
                    if (skipConfirmation) {
                        onClick()
                    } else {
                        showDialog = true
                    }
                    isPressed = false
                }
            }
        }
    )

    val alpha by animateFloatAsState(
        targetValue = if (isServiceActive) 1f else 0.3f,
        animationSpec = tween(300),
        label = "alpha"
    )

    // Handle press animation and dialog timing
    LaunchedEffect(isPressed) {
        if (isPressed && !isServiceActive) {
            // Brief animation then trigger onClick for toast
            delay(150)
            isPressed = false
            onClick()
        }
    }

    val resolvedTint = iconTint ?: if (isServiceActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant

    FeatureCard(
        title = title,
        description = description,
        icon = icon,
        onClick = {
            if (!isPressed) { // Prevent multiple rapid clicks
                isPressed = true
            }
        },
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(alpha),
        iconSize = iconSize,
        contentPadding = PaddingValues(16.dp),
        iconTint = resolvedTint
    )

    // Simple confirmation dialog
    if (!skipConfirmation && showDialog && isServiceActive) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticUtils.triggerMediumFeedback(haptic, context)
                        onClick()
                        showDialog = false
                    }
                ) {
                    Text("Start")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    HapticUtils.triggerLightFeedback(haptic, context)
                    showDialog = false 
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Wide feature card template for full-width actions
 */
@Composable
fun WideFeatureCard(
    title: String,
    description: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Int = 140,
    iconSize: Int = 32,
    isServiceActive: Boolean = true,
    skipConfirmation: Boolean = false,
    iconTint: Color? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale",
        finishedListener = { finalValue ->
            if (finalValue == 0.95f && isPressed && isServiceActive) {
                if (skipConfirmation) {
                    onClick()
                    isPressed = false
                } else {
                    showDialog = true
                    isPressed = false
                }
            }
        }
    )

    val alpha by animateFloatAsState(
        targetValue = if (isServiceActive) 1f else 0.3f,
        animationSpec = tween(300),
        label = "alpha"
    )

    LaunchedEffect(isPressed) {
        if (isPressed && !isServiceActive) {
            delay(150)
            isPressed = false
            onClick()
        }
    }

    val resolvedTint = iconTint ?: if (isServiceActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant

    FeatureCard(
        title = title,
        description = description,
        icon = icon,
        onClick = {
            if (!isPressed) {
                isPressed = true
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(alpha),
        iconSize = iconSize,
        contentPadding = PaddingValues(16.dp),
        iconTint = resolvedTint
    )

    if (!skipConfirmation && showDialog && isServiceActive) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticUtils.triggerMediumFeedback(haptic, context)
                        onClick()
                        showDialog = false
                    }
                ) {
                    Text("Open")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    HapticUtils.triggerLightFeedback(haptic, context)
                    showDialog = false 
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Section header for grouping cards
 */
@Composable
fun HomeSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .padding(start = 8.dp, bottom = 8.dp, top = 8.dp)
    )
}

/**
 * Feature grid layout for organizing multiple feature cards
 */
@Composable
fun FeatureGrid(
    modifier: Modifier = Modifier,
    spacing: Int = 16,
    content: @Composable RowScope.() -> Unit
) {
    val themeState = LocalThemeState.current
    
    // Use Material 3's 8dp grid system for expressive theme
    val enhancedSpacing = if (themeState.themeStyle == AppThemeStyle.EXPRESSIVE) {
        // Material 3 spacing: 8dp, 16dp, 24dp, 32dp
        when (spacing) {
            8 -> 8
            16 -> 16
            24 -> 24
            32 -> 32
            else -> 16 // Default to 16dp for expressive theme
        }
    } else {
        spacing
    }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(enhancedSpacing.dp),
        content = content
    )
}

/**
 * Main glyph control card template (specialized for the main toggle)
 * Inspired by Nothing OS interface design with Material You theming
 * Features a morphing button with repositioned text elements
 */
@Composable
fun GlyphControlCard(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes illustrationRes: Int? = null
) {
    val themeState = LocalThemeState.current
    
    // Use Material 3 surface container colors for expressive theme
    val backgroundColor = if (themeState.themeStyle == AppThemeStyle.EXPRESSIVE) {
        MaterialTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Service status text - top left
            Text(
                text = "Service is ${if (enabled) "Active" else "Inactive"}",
                style = MaterialTheme.typography.headlineSmall,
                color = textColor,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.TopStart)
            )

            // Glyph lights label - bottom left
            Text(
                text = "GLYPH LIGHTS",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.8f),
                letterSpacing = 0.5.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                modifier = Modifier.align(Alignment.BottomStart)
            )

            // Morphing toggle button - top right
            MorphingToggleButton(
                checked = enabled,
                onCheckedChange = onEnabledChange,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}

/**
 * Reusable morphing toggle button component
 * Features smooth shape transitions and consistent theming
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MorphingToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabledIcon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Enabled",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    },
    disabledIcon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Disabled",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val themeState = LocalThemeState.current

    val width by animateDpAsState(
        targetValue = if (checked) 60.dp else 88.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "width"
    )

    val height by animateDpAsState(
        targetValue = if (checked) 60.dp else 40.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "height"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (checked) 30.dp else 12.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "cornerRadius"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (checked) {
            if (themeState.themeStyle == AppThemeStyle.EXPRESSIVE) {
                MaterialTheme.colorScheme.primary
            } else {
                NothingRed // Using Nothing Red color
            }
        } else {
            if (themeState.themeStyle == AppThemeStyle.EXPRESSIVE) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                Color(0xFF9E9E9E) // Static gray color
            }
        },
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )

    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "pressScale"
    )

    val activeScale by animateFloatAsState(
        targetValue = if (checked) 1.07f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "activeScale"
    )

    val totalScale = pressScale * activeScale

    Box(
        modifier = modifier
            .size(width, height)
            .scale(totalScale)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                try {
                    isPressed = true
                    HapticUtils.triggerLightFeedback(haptic, context)
                    onCheckedChange(!checked)
                    scope.launch {
                        delay(120)
                        isPressed = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = checked,
            transitionSpec = {
                scaleIn(tween(200)) + fadeIn() with fadeOut()
            },
            label = "iconTransition"
        ) { isChecked ->
            if (isChecked) {
                enabledIcon()
            } else {
                disabledIcon()
            }
        }
    }
}

/**
 * Three-state morphing toggle button for font family selection
 * Features smooth shape transitions, distinct colors, and typography preview
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ThreeStateFontMorphingButton(
    currentVariant: FontVariant,
    onVariantSelected: (FontVariant) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val themeState = LocalThemeState.current

    // Cycle through variants: HEADLINE -> NDOT -> SYSTEM -> HEADLINE
    val nextVariant = when (currentVariant) {
        FontVariant.HEADLINE -> FontVariant.NDOT
        FontVariant.NDOT -> FontVariant.SYSTEM
        FontVariant.SYSTEM -> FontVariant.HEADLINE
    }

    // Use exact standard morphing toggle dimensions - matches other settings cards
    val width by animateDpAsState(
        targetValue = when (currentVariant) {
            FontVariant.HEADLINE -> 88.dp  // Wide rectangle (unchecked state)
            FontVariant.NDOT -> 60.dp      // Square (checked state)
            FontVariant.SYSTEM -> 60.dp    // Circle (checked state)
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "width"
    )

    val height by animateDpAsState(
        targetValue = when (currentVariant) {
            FontVariant.HEADLINE -> 40.dp  // Standard rectangle height
            FontVariant.NDOT -> 60.dp      // Square height (checked state)
            FontVariant.SYSTEM -> 60.dp    // Circle height (checked state)
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "height"
    )

    // Exact standard morphing toggle corner radius
    val cornerRadius by animateDpAsState(
        targetValue = when (currentVariant) {
            FontVariant.HEADLINE -> 12.dp  // Standard rounded rectangle
            FontVariant.NDOT -> 12.dp      // Standard rounded rectangle
            FontVariant.SYSTEM -> 30.dp    // Full circle (checked state)
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "cornerRadius"
    )

    // Three distinct colors for each font variant
    val backgroundColor by animateColorAsState(
        targetValue = if (themeState.themeStyle == AppThemeStyle.EXPRESSIVE) {
            when (currentVariant) {
                FontVariant.HEADLINE -> MaterialTheme.colorScheme.surfaceContainerHigh
                FontVariant.NDOT -> MaterialTheme.colorScheme.secondary
                FontVariant.SYSTEM -> MaterialTheme.colorScheme.primary
            }
        } else {
            when (currentVariant) {
                FontVariant.HEADLINE -> Color(0xFF9E9E9E)              // Gray for NType Headline
                FontVariant.NDOT -> Color(0xFF7B1FA2)                  // Purple for NDot 57 Caps
                FontVariant.SYSTEM -> NothingRed                       // Red for System Default
            }
        },
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )

    val contentColor = Color.White

    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "pressScale"
    )

    val activeScale by animateFloatAsState(
        targetValue = when (currentVariant) {
            FontVariant.HEADLINE -> 1f      // Normal scale (unchecked state)
            FontVariant.NDOT -> 1.07f       // Enlarged scale (checked state)
            FontVariant.SYSTEM -> 1.07f     // Enlarged scale (checked state)
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "activeScale"
    )

    val totalScale = pressScale * activeScale

    Box(
        modifier = modifier
            .size(width, height)
            .scale(totalScale)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                try {
                    isPressed = true
                    HapticUtils.triggerLightFeedback(haptic, context)
                    onVariantSelected(nextVariant)
                    scope.launch {
                        delay(120)
                        isPressed = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = currentVariant,
            transitionSpec = {
                scaleIn(tween(200)) + fadeIn() with 
                scaleOut(tween(150)) + fadeOut()
            },
            label = "fontIconTransition"
        ) { variant ->
            Text(
                text = when (variant) {
                    FontVariant.HEADLINE -> "T"
                    FontVariant.NDOT -> "N"
                    FontVariant.SYSTEM -> "S"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = when (variant) {
                        FontVariant.HEADLINE -> FontFamily(Font(com.bleelblep.glyphsharge.R.font.ntype_82_headline))
                        FontVariant.NDOT -> FontFamily(Font(com.bleelblep.glyphsharge.R.font.ndot55caps))
                        FontVariant.SYSTEM -> FontFamily.Default
                    },
                    fontSize = when (variant) {
                        FontVariant.HEADLINE -> 18.sp
                        FontVariant.NDOT -> 18.sp
                        FontVariant.SYSTEM -> 20.sp
                    }
                ),
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Dialog states for the transition system
 */
enum class DialogState {
    CONFIRMATION,
    COUNTDOWN,
    ANIMATION_PROGRESS
}

/**
 * Manages Glyph Guard security alert configurations for USB disconnection detection
 */
sealed class GlyphGuardMode {
    abstract val name: String
    abstract val description: String
    abstract val blinkInterval: Long
    abstract val alertDuration: Long
    abstract val hasSound: Boolean
    
    object Stealth : GlyphGuardMode() {
        override val name = "Stealth"
        override val description = "Silent glyph alerts only, no sound - perfect for discreet monitoring"
        override val blinkInterval = 500L      // Slower blinking
        override val alertDuration = 20000L    // 20 seconds
        override val hasSound = false
    }
    
    object Standard : GlyphGuardMode() {
        override val name = "Standard"
        override val description = "Rapid glyph blinking with system notification sound"
        override val blinkInterval = 200L      // Fast blinking
        override val alertDuration = 30000L    // 30 seconds
        override val hasSound = true
    }
    
    object Intense : GlyphGuardMode() {
        override val name = "Intense"
        override val description = "Very rapid blinking with loud alarm sound - maximum security"
        override val blinkInterval = 100L      // Very fast blinking
        override val alertDuration = 45000L    // 45 seconds
        override val hasSound = true
    }
}

/**
 * Manages Glyph Guard alert state and timing calculations
 */
class GlyphGuardManager(
    private val mode: GlyphGuardMode,
    private val maxDuration: Long = mode.alertDuration
) {
    /**
     * Calculate alert progress based on elapsed time
     * @param elapsedTime Time elapsed in milliseconds
     * @return Progress from 0.0 to 1.0
     */
    fun calculateAlertProgress(elapsedTime: Long): Float {
        return (elapsedTime.toFloat() / maxDuration).coerceIn(0f, 1f)
    }
    
    /**
     * Determine if alert should be blinking based on current time
     */
    fun shouldBlink(elapsedTime: Long): Boolean {
        val blinkCycle = elapsedTime % (mode.blinkInterval * 2)
        return blinkCycle < mode.blinkInterval
    }
    
    /**
     * Get remaining alert time in seconds
     */
    fun getRemainingTime(elapsedTime: Long): Int {
        val remaining = maxDuration - elapsedTime
        return (remaining / 1000).toInt().coerceAtLeast(0)
    }
}

/**
 * State class for managing Glyph Guard alert progress
 */
data class GlyphGuardState(
    val mode: GlyphGuardMode,
    val isActive: Boolean = false,
    val alertProgress: Float = 0f,
    val remainingTime: Int = 0,
    val shouldBlink: Boolean = false
) {
    private val guardManager = GlyphGuardManager(mode)
    
    /**
     * Updates the guard state based on elapsed time
     * @param elapsedTime Time elapsed in milliseconds
     * @return Updated GlyphGuardState
     */
    fun updateFromElapsedTime(elapsedTime: Long): GlyphGuardState {
        val progress = guardManager.calculateAlertProgress(elapsedTime)
        val remaining = guardManager.getRemainingTime(elapsedTime)
        val blink = guardManager.shouldBlink(elapsedTime)
        
        return copy(
            alertProgress = progress,
            remainingTime = remaining,
            shouldBlink = blink
        )
    }

    /**
     * Gets the display text for the current alert status
     */
    fun getStatusText(): String = when {
        !isActive -> "Glyph Guard Ready"
        remainingTime > 0 -> "Alert Active - ${remainingTime}s remaining"
        else -> "Alert Complete"
    }
}

/**
 * Multi-stage confirmation dialog with instant state transitions
 * Shows confirmation -> countdown -> animation progress stages
 */
@Composable
fun FeatureConfirmationDialog(
    title: String,
    description: String,
    onConfirm: suspend (onProgressUpdate: (Float) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    initialDialogState: DialogState = DialogState.CONFIRMATION,
    modifier: Modifier = Modifier,
    customButtons: (@Composable () -> Unit)? = null,
    isTestMode: Boolean = false,
    glyphGuardMode: GlyphGuardMode = GlyphGuardMode.Standard
) {
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    // Get the appropriate description based on the Glyph Guard mode
    val modeDescription = glyphGuardMode.description

    var dialogState by remember { mutableStateOf(initialDialogState) }
    var countdownValue by remember { mutableStateOf(5) }
    var animationProgress by remember { mutableStateOf(0f) }
    var isAnimationComplete by remember { mutableStateOf(false) }
    var guardState by remember { 
        mutableStateOf(GlyphGuardState(
            mode = glyphGuardMode,
            isActive = false
        ))
    }
    var startTime by remember { mutableStateOf(0L) }
    
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val glyphAnimationManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            GlyphComponent::class.java
        ).glyphAnimationManager()
    }
    
    // Wakelock management
    val wakeLock = remember {
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "GlyphZen:AnimationWakelock")
    }

    // Handle countdown state
    LaunchedEffect(dialogState) {
        if (dialogState == DialogState.COUNTDOWN) {
            // Countdown from 5 to 1
            for (i in 5 downTo 1) {
                countdownValue = i
                delay(1000)
            }
            // Only move to animation progress if not in test mode
            if (!isTestMode) {
                dialogState = DialogState.ANIMATION_PROGRESS
                // Acquire wakelock before starting animation
                try {
                    if (!wakeLock.isHeld) {
                        wakeLock.acquire(10*60*1000L /*10 minutes*/)
                    }
                } catch (e: Exception) {
                    Log.e("FeatureConfirmationDialog", "Failed to acquire wakelock: ${e.message}")
                }
                // Start the Glyph Guard test/activation
                onConfirm { progress ->
                    animationProgress = progress
                    // Update guard state with the new progress
                    guardState = guardState.updateFromElapsedTime(System.currentTimeMillis() - startTime)
                    if (progress >= 1.0f) {
                        isAnimationComplete = true
                        coroutineScope.launch {
                            delay(1500) // Show completion for a moment
                            try {
                                if (wakeLock.isHeld) {
                                    wakeLock.release()
                                }
                            } catch (e: Exception) {
                                // Ignore errors
                            }
                            onDismiss()
                        }
                    }
                    Unit // Explicitly return Unit to fix compilation error
                }
            }
        }
    }

    // Update guard state based on elapsed time
    LaunchedEffect(dialogState) {
        if (dialogState == DialogState.ANIMATION_PROGRESS) {
            startTime = System.currentTimeMillis()
            while (!isAnimationComplete) {
                val elapsedTime = System.currentTimeMillis() - startTime
                guardState = guardState.updateFromElapsedTime(elapsedTime)
                delay(100) // Update every 100ms
            }
        }
    }

    // Single dialog container with crossfading content
    AlertDialog(
        onDismissRequest = {
            // Make all dialog states modal - no dismissal via outside tap or back button
        },
        title = {
            Text(
                text = when (dialogState) {
                    DialogState.CONFIRMATION -> "Start $title?"
                    DialogState.COUNTDOWN -> "Starting $title"
                    DialogState.ANIMATION_PROGRESS -> if (isAnimationComplete) "$title Complete!" else "Running $title"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Crossfade(
                targetState = dialogState,
                animationSpec = tween(200),
                label = "DialogContentTransition"
            ) { state ->
                when (state) {
                    DialogState.CONFIRMATION -> {
                        ConfirmationDialogBody(
                            description = modeDescription,
                            onContinue = {
                                dialogState = DialogState.COUNTDOWN
                            },
                            onCancel = onDismiss
                        )
                    }
                    
                    DialogState.COUNTDOWN -> {
                        CountdownDialogBody(
                            countdownValue = countdownValue
                        )
                    }
                    
                    DialogState.ANIMATION_PROGRESS -> {
                        GlyphGuardProgressDialogBody(
                            progress = animationProgress,
                            isComplete = isAnimationComplete,
                            guardState = guardState
                        )
                    }
                }
            }
        },
        confirmButton = {
            when (dialogState) {
                DialogState.CONFIRMATION -> {
                    if (isTestMode && customButtons != null) {
                        customButtons()
                    } else {
                        ConfirmationButtons(
                            onContinue = {
                                dialogState = DialogState.COUNTDOWN
                            },
                            onCancel = onDismiss
                        )
                    }
                }
                DialogState.COUNTDOWN -> {
                    Button(
                        onClick = {
                            HapticUtils.triggerLightFeedback(haptic, context)
                            try {
                                if (wakeLock.isHeld) {
                                    wakeLock.release()
                                }
                            } catch (e: Exception) {
                                // Ignore errors
                            }
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color(0xFF38393B)
                                AppThemeStyle.CLASSIC -> Color(0xFFFFDBD7)
                                AppThemeStyle.NEON -> Color(0xFF00FF00)
                                else -> MaterialTheme.colorScheme.errorContainer
                            },
                            contentColor = when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color.White
                                AppThemeStyle.CLASSIC -> Color(0xFF000000)
                                AppThemeStyle.NEON -> Color.Black
                                else -> Color(0xFF000000)
                            }
                        )
                    ) {
                        Text("Cancel")
                    }
                }
                DialogState.ANIMATION_PROGRESS -> {
                    if (!isAnimationComplete) {
                        Button(
                            onClick = {
                                HapticUtils.triggerMediumFeedback(haptic, context)
                                // Stop the animation
                                glyphAnimationManager.stopAnimations()
                                
                                try {
                                    if (wakeLock.isHeld) {
                                        wakeLock.release()
                                    }
                                } catch (e: Exception) {
                                    // Ignore errors
                                }
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (themeState.themeStyle) {
                                    AppThemeStyle.AMOLED -> Color(0xFF38393B)
                                    AppThemeStyle.CLASSIC -> Color(0xFFFFDBD7)
                                    AppThemeStyle.NEON -> Color(0xFF00FF00)
                                    else -> MaterialTheme.colorScheme.errorContainer
                                },
                                contentColor = when (themeState.themeStyle) {
                                    AppThemeStyle.AMOLED -> Color.White
                                    AppThemeStyle.CLASSIC -> Color(0xFF000000)
                                    AppThemeStyle.NEON -> Color.Black
                                    else -> Color(0xFF000000)
                                }
                            )
                        ) {
                            Text("Stop Animation")
                        }
                    }
                }
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface
    )

    // Cleanup wakelock on disposal
    DisposableEffect(Unit) {
        onDispose {
            try {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }
}

/**
 * Confirmation dialog body (content only)
 */
@Composable
private fun ConfirmationDialogBody(
    description: String,
    onContinue: () -> Unit,
    onCancel: () -> Unit
) {
    Text(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Confirmation dialog buttons
 */
@Composable
private fun ConfirmationButtons(
    onContinue: () -> Unit,
    onCancel: () -> Unit
) {
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Continue button - full width
        Button(
            onClick = { 
                HapticUtils.triggerMediumFeedback(haptic, context)
                onContinue() 
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = when (themeState.themeStyle) {
                    AppThemeStyle.AMOLED -> MaterialTheme.colorScheme.primaryContainer
                    AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                    else -> MaterialTheme.colorScheme.primaryContainer
                },
                contentColor = when (themeState.themeStyle) {
                    AppThemeStyle.AMOLED -> MaterialTheme.colorScheme.onPrimaryContainer
                    AppThemeStyle.CLASSIC -> Color.White
                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                }
            ),
            border = when (themeState.themeStyle) {
                AppThemeStyle.AMOLED -> BorderStroke(1.dp, Color.White)
                else -> null
            }
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        // Cancel button - full width
        Button(
            onClick = { 
                HapticUtils.triggerLightFeedback(haptic, context)
                onCancel() 
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = when (themeState.themeStyle) {
                    AppThemeStyle.AMOLED -> Color(0xFF38393B)
                    AppThemeStyle.CLASSIC -> Color(0xFFFFDBD7)
                    AppThemeStyle.NEON -> Color(0xFF00FF00)
                    else -> MaterialTheme.colorScheme.errorContainer
                },
                contentColor = when (themeState.themeStyle) {
                    AppThemeStyle.AMOLED -> Color.White
                    AppThemeStyle.CLASSIC -> Color(0xFF000000)
                    AppThemeStyle.NEON -> Color.Black
                    else -> Color(0xFF000000)
                }
            )
        ) {
            Text("Cancel")
        }
    }
}

/**
 * Countdown dialog body (content only)
 */
@Composable
private fun CountdownDialogBody(
    countdownValue: Int
) {
    val themeState = LocalThemeState.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Animation will begin in:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Large circular progress with timer inside
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            // Calculate progress based on remaining time (5 to 1)
            val progress by animateFloatAsState(
                targetValue = (5 - countdownValue) / 4f,
                animationSpec = tween(
                    durationMillis = 1000,
                    easing = LinearEasing
                ),
                label = "countdownProgress"
            )
            
            // Background circle
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = when (themeState.themeStyle) {
                    AppThemeStyle.Y2K -> MaterialTheme.colorScheme.primaryContainer
                    AppThemeStyle.NEON -> MaterialTheme.colorScheme.primaryContainer
                    AppThemeStyle.CLASSIC -> Color(0xFFE3DCEC)
                    else -> MaterialTheme.colorScheme.primaryContainer
                },
                strokeWidth = 8.dp
            )
            
            // Animated progress circle
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = when (themeState.themeStyle) {
                    AppThemeStyle.Y2K -> MaterialTheme.colorScheme.primary
                    AppThemeStyle.NEON -> MaterialTheme.colorScheme.primary
                    AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                    else -> MaterialTheme.colorScheme.primary
                },
                strokeWidth = 8.dp
            )
            
            // Timer text inside the circle
            Text(
                text = countdownValue.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = when (themeState.themeStyle) {
                    AppThemeStyle.Y2K -> MaterialTheme.colorScheme.primary
                    AppThemeStyle.NEON -> MaterialTheme.colorScheme.primary
                    AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                    else -> MaterialTheme.colorScheme.primary
                },
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Keeping device awake",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

/**
 * Glyph Guard progress dialog body (content only)
 */
@Composable
private fun GlyphGuardProgressDialogBody(
    progress: Float,
    isComplete: Boolean,
    guardState: GlyphGuardState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val themeState = LocalThemeState.current
    var chargingInfo by remember { mutableStateOf<ChargingInfo?>(null) }

    // Get charging information for USB disconnection monitoring
    LaunchedEffect(Unit) {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
        val intentFilter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
        val batteryIntent = context.registerReceiver(null, intentFilter)

        if (batteryIntent != null) {
            val batteryStatus = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1)
            val isCharging = batteryStatus == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                            batteryStatus == android.os.BatteryManager.BATTERY_STATUS_FULL
            val plugType = batteryIntent.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, -1)
            val connectionType = when (plugType) {
                android.os.BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                android.os.BatteryManager.BATTERY_PLUGGED_AC -> "AC Adapter"
                android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                else -> "Not Connected"
            }
            chargingInfo = ChargingInfo(isCharging, connectionType)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Glyph Guard status icon
        Text(
            text = if (isComplete) "🛡️✅" else if (guardState.shouldBlink) "🛡️⚡" else "🛡️",
            style = MaterialTheme.typography.headlineLarge
        )

        // Current charging status
        chargingInfo?.let { info ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (info.isCharging) 
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else 
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (info.isCharging) "🔌 Connected" else "⚠️ Disconnected",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (info.isCharging) 
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = info.connectionType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Alert mode information
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Alert Mode: ${guardState.mode.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (guardState.mode.hasSound) "🔊 Sound Enabled" else "🔇 Silent Mode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Progress indicator
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = when (themeState.themeStyle) {
                AppThemeStyle.AMOLED -> Color.White
                AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                AppThemeStyle.NEON -> Color(0xFF00FF00)
                else -> MaterialTheme.colorScheme.primary
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // Guard status text
        Text(
            text = when {
                isComplete -> "Glyph Guard Test Complete!"
                guardState.remainingTime > 0 -> "Testing for ${guardState.remainingTime}s..."
                else -> "Initializing Glyph Guard..."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // Test status
        if (!isComplete) {
            Text(
                text = "Simulating USB disconnect alert...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Data class to hold charging connection information
 */
private data class ChargingInfo(
    val isCharging: Boolean,
    val connectionType: String
)

/**
 * PowerPeek-specific card that uses the PowerPeek confirmation dialog
 */
@Composable
fun PowerPeekCard(
    title: String,
    description: String,
    icon: Painter,
    onTestPowerPeek: () -> Unit,
    onEnablePowerPeek: () -> Unit,
    onDisablePowerPeek: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Int = 40,
    isServiceActive: Boolean = true,
    settingsRepository: SettingsRepository,
    autoDemo: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    // Track current state for immediate UI update
    var powerPeekEnabled by remember { mutableStateOf(settingsRepository.isPowerPeekEnabled()) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale",
        finishedListener = { finalValue ->
            // When scale animation completes and we're pressed down (scale = 0.95f)
            if (finalValue == 0.95f && isPressed && isServiceActive) {
                // Animation finished, add small pause to let user see the press state
                coroutineScope.launch {
                    kotlinx.coroutines.delay(150) // Brief pause after animation completes
                    showDialog = true
                    isPressed = false
                }
            }
        }
    )

    val alpha by animateFloatAsState(
        targetValue = if (isServiceActive) 1f else 0.3f,
        animationSpec = tween(300),
        label = "alpha"
    )

    // Handle press animation and dialog timing
    LaunchedEffect(isPressed) {
        if (isPressed && !isServiceActive) {
            // Brief animation then trigger onClick for toast
            delay(150)
            isPressed = false
            // Show service disabled toast
            // This will be handled by the parent component
        }
    }

    // Auto demo handling
    LaunchedEffect(autoDemo) {
        if (autoDemo && isServiceActive) {
            delay(600)
            showDialog = true
            delay(1500)
            showDialog = false
        }
    }

    // Card with status indicator overlay
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(alpha)
    ) {
        FeatureCard(
            title = title,
            description = description,
            icon = icon,
            onClick = {
                HapticUtils.triggerLightFeedback(haptic, context)
                if (!isPressed) { // Prevent multiple rapid clicks
                    isPressed = true
                }
            },
            modifier = Modifier.fillMaxSize(),
            iconSize = iconSize,
            contentPadding = PaddingValues(16.dp),
            // Use theme-aware colors so the icon adapts automatically on theme changes
            iconTint = when {
                !isServiceActive -> Color(0xFF674FA3) // Purple when master service OFF
                powerPeekEnabled -> ACTIVE_GREEN      // Green when feature ON
                else -> INACTIVE_RED                  // Red when feature OFF
            }
        )
    }

    // PowerPeek confirmation dialog
    if (showDialog && isServiceActive) {
        PowerPeekConfirmationDialog(
            onTestPowerPeek = {
                onTestPowerPeek()
                showDialog = false
            },
            onEnablePowerPeek = {
                onEnablePowerPeek()
                // Update local status immediately
                powerPeekEnabled = true
                showDialog = false
            },
            onDisablePowerPeek = {
                onDisablePowerPeek()
                powerPeekEnabled = false
                showDialog = false
            },
            onDismiss = { 
                showDialog = false 
            },
            settingsRepository = settingsRepository
        )
    }
}

/**
 * Glyph Guard-specific card with multiple action buttons
 */
@Composable
fun GlyphGuardCard(
    title: String,
    description: String,
    icon: Painter,
    onTest: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Int = 40,
    isServiceActive: Boolean = true,
    glyphGuardMode: GlyphGuardMode = GlyphGuardMode.Standard,
    settingsRepository: SettingsRepository,
    autoDemo: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var glyphGuardEnabled by remember { mutableStateOf(settingsRepository.isGlyphGuardEnabled()) }
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale",
        finishedListener = { finalValue ->
            // When scale animation completes and we're pressed down (scale = 0.95f)
            if (finalValue == 0.95f && isPressed && isServiceActive) {
                // Animation finished, add small pause to let user see the press state
                coroutineScope.launch {
                    kotlinx.coroutines.delay(150) // Brief pause after animation completes
                    showDialog = true
                    isPressed = false
                }
            }
        }
    )

    val alpha by animateFloatAsState(
        targetValue = if (isServiceActive) 1f else 0.3f,
        animationSpec = tween(300),
        label = "alpha"
    )

    // Handle press animation and dialog timing
    LaunchedEffect(isPressed) {
        if (isPressed && !isServiceActive) {
            // Brief animation then trigger onClick for toast
            delay(150)
            isPressed = false
            // Show service disabled toast
            // This will be handled by the parent component
        }
    }

    // Card with status indicator overlay
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(alpha)
    ) {
        FeatureCard(
            title = title,
            description = description,
            icon = icon,
            onClick = {
                HapticUtils.triggerLightFeedback(haptic, context)
                if (!isPressed) { // Prevent multiple rapid clicks
                    isPressed = true
                }
            },
            modifier = Modifier.fillMaxSize(),
            iconSize = iconSize,
            contentPadding = PaddingValues(16.dp),
            // Theme-aware tinting for Glyph Guard status
            iconTint = when {
                !isServiceActive -> Color(0xFF674FA3) // Purple when master service OFF
                glyphGuardEnabled -> ACTIVE_GREEN      // Green when feature ON
                else -> INACTIVE_RED                   // Red when feature OFF
            }
        )
    }

    // Glyph Guard confirmation dialog
    if (showDialog && isServiceActive) {
        GlyphGuardConfirmationDialog(
            onTest = {
                onTest()
                showDialog = false
            },
            onStart = {
                onStart()
                glyphGuardEnabled = true
                showDialog = false
            },
            onStop = {
                onStop()
                glyphGuardEnabled = false
                showDialog = false
            },
            onDismiss = { 
                showDialog = false 
            },
            glyphGuardMode = glyphGuardMode,
            settingsRepository = settingsRepository
        )
    }

    // trigger demo automatically
    LaunchedEffect(autoDemo) {
        if (autoDemo && isServiceActive) {
            delay(600)
            showDialog = true
            delay(1500)
            showDialog = false
        }
    }
}

/**
 * Glyph Guard confirmation dialog with test, start, and stop options - styled like PowerPeek
 */
@Composable
private fun GlyphGuardConfirmationDialog(
    onTest: () -> Unit,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onDismiss: () -> Unit,
    glyphGuardMode: GlyphGuardMode = GlyphGuardMode.Standard,
    settingsRepository: SettingsRepository
) {
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = { /* Prevent dismissal */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🛡️ Glyph Guard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "USB theft protection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Feature description
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) {
                                MaterialTheme.colorScheme.surfaceContainer
                            } else {
                                Color(0xFFF8F5FF)
                            }
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                Text(
                            text = "🔒 How it works:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Instantly alerts when USB is disconnected\n• Rapid glyph blinking + loud alarm sound\n• Configurable alert duration and intensity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary Action - Test Glyph Guard
                ElevatedButton(
                    onClick = {
                        HapticUtils.triggerMediumFeedback(haptic, context)
                        onTest()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                            AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🧪 Test Glyph Guard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Secondary Actions Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Configure Settings Button
                    Button(
                        onClick = { 
                            HapticUtils.triggerLightFeedback(haptic, context)
                            showSettingsDialog = true 
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color(0xFF2D4A3E)
                                AppThemeStyle.CLASSIC -> Color(0xFF8D7BA5)
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            },
                            contentColor = when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color.White
                                AppThemeStyle.CLASSIC -> Color.White
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "⚙️ Settings",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    // Cancel Button
                    OutlinedButton(
                        onClick = { 
                            HapticUtils.triggerLightFeedback(haptic, context)
                            onDismiss() 
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "✕ Cancel",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp)
    )
    
    // Glyph Guard Settings Dialog
    if (showSettingsDialog) {
        GlyphGuardSettingsDialog(
            onEnable = {
                onStart()
                showSettingsDialog = false
                onDismiss()
            },
            onDisable = {
                onStop()
                showSettingsDialog = false
                onDismiss()
            },
            onDismiss = { showSettingsDialog = false },
            settingsRepository = settingsRepository
        )
    }
}

/**
 * Helper function to get display name for selected sound
 */
private fun getSoundDisplayName(context: Context, settingsRepository: SettingsRepository): String {
    val customUri = settingsRepository.getGlyphGuardCustomRingtoneUri()
    return if (customUri != null) {
        try {
            val ringtone = RingtoneManager.getRingtone(context, Uri.parse(customUri))
            ringtone?.getTitle(context) ?: "Custom ringtone"
        } catch (e: Exception) {
            "Custom ringtone"
        }
    } else {
        settingsRepository.getGlyphGuardSoundType().lowercase().replaceFirstChar { it.uppercase() }
    }
}

/**
 * Glyph Guard Status Card - similar to PowerPeek's battery info card
 */
@Composable
private fun GlyphGuardStatusCard(
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val themeState = LocalThemeState.current
    val currentDuration = remember { settingsRepository.getGlyphGuardDuration() / 1000 } // Convert to seconds
    
    Card(
        modifier = modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
            containerColor = when (themeState.themeStyle) {
                AppThemeStyle.AMOLED -> Color(0xFF0D1F17)
                AppThemeStyle.CLASSIC -> Color(0xFFF0E8FF)
                else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🛡️ Glyph Guard Status",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer, // Currently always shows as disabled since there's no service state
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "❌ Ready to Enable",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Current Settings
                    Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                    text = "Alert Duration: ${currentDuration}s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                    text = if (settingsRepository.isGlyphGuardSoundEnabled()) {
                        "Sound: ${getSoundDisplayName(LocalContext.current, settingsRepository)}"
                    } else {
                        "Sound: Disabled"
                    },
                            style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Divider
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 1.dp
            )
            
            // Description
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔒 Configure settings and enable to protect your USB connection",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Glyph Guard Settings Dialog with duration configuration - styled like PowerPeek
 */
@Composable
private fun GlyphGuardSettingsDialog(
    onDismiss: () -> Unit,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val themeState = LocalThemeState.current
    val currentDuration = settingsRepository.getGlyphGuardDuration() / 1000 // seconds
    var alertDuration by remember { mutableStateOf(currentDuration.toFloat()) }
    var soundEnabled by remember { mutableStateOf(settingsRepository.isGlyphGuardSoundEnabled()) }
    var selectedRingtoneUri by remember { mutableStateOf(settingsRepository.getGlyphGuardCustomRingtoneUri()) }
    var selectedRingtoneName by remember { mutableStateOf("System Default") }
    var showRingtonePicker by remember { mutableStateOf(false) }
    val currentMode = settingsRepository.getGlyphGuardAlertMode()
    var selectedMode by remember { mutableStateOf<GlyphGuardMode>(currentMode) }
    
    // Get sound name for display
    val soundName = remember(selectedRingtoneUri) {
        selectedRingtoneUri?.let { uriString ->
            try {
                val uri = Uri.parse(uriString)
                
                // Try to get ringtone title first (works for system sounds)
                val ringtone = RingtoneManager.getRingtone(context, uri)
                val ringtoneTitle = ringtone?.getTitle(context)
                
                if (!ringtoneTitle.isNullOrEmpty()) {
                    ringtoneTitle
                } else {
                    // For custom files, try to get filename from URI
                    uri.lastPathSegment?.let { segment ->
                        // Remove file extension and decode
                        segment.substringBeforeLast('.').replace("%20", " ")
                    } ?: "Custom audio file"
                }
            } catch (e: Exception) {
                Log.w("GlyphGuardComponents", "Error getting sound name for URI: $uriString", e)
                "Custom audio file"
            }
        } ?: "System Default"
    }

    // Ringtone picker launcher
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == ComponentActivity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            selectedRingtoneUri = uri?.toString()
            settingsRepository.saveGlyphGuardCustomRingtoneUri(selectedRingtoneUri)
            Log.d("GlyphGuardComponents", "Selected ringtone URI: $selectedRingtoneUri")
        }
    }
    
    // Custom file picker
    val customFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                Log.d("GlyphGuardComponents", "Processing custom file: $uri")
                
                // Copy the file to internal storage to avoid permission issues
                val internalUri = copyFileToInternalStorage(context, uri, "glyphguard_custom_audio")
                
                if (internalUri != null) {
                    selectedRingtoneUri = internalUri.toString()
                    settingsRepository.saveGlyphGuardCustomRingtoneUri(selectedRingtoneUri)
                    Log.d("GlyphGuardComponents", "Successfully copied custom file to internal storage: $internalUri")
                } else {
                    // Fallback: try to use original URI with persistable permission
                    try {
                        context.contentResolver.takePersistableUriPermission(
                            uri, 
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                        selectedRingtoneUri = uri.toString()
                        settingsRepository.saveGlyphGuardCustomRingtoneUri(selectedRingtoneUri)
                        Log.d("GlyphGuardComponents", "Fallback: using original URI with persistable permission")
                    } catch (e: Exception) {
                        Log.e("GlyphGuardComponents", "Failed to copy file and take persistable permission", e)
                        // Last resort: save original URI anyway
                        selectedRingtoneUri = uri.toString()
                        settingsRepository.saveGlyphGuardCustomRingtoneUri(selectedRingtoneUri)
                    }
                }
            } catch (e: Exception) {
                Log.e("GlyphGuardComponents", "Error processing custom file: $uri", e)
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Configure",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Customize your USB protection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                // Alert Duration Control Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                            containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) {
                                MaterialTheme.colorScheme.surfaceContainer
                            } else {
                                Color(0xFFF8F5FF)
                            }
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text = "⏰ Alert Duration",
                            style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = when (themeState.themeStyle) {
                                    AppThemeStyle.AMOLED -> Color(0xFF2D2D2D)
                                    AppThemeStyle.CLASSIC -> Color(0xFFE8DEF8)
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                        Text(
                                    text = "${alertDuration.toInt()}s",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color.White
                                AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                                    }
                                )
                            }
                        }
                        
                        Slider(
                            value = alertDuration,
                            onValueChange = { 
                                HapticUtils.triggerLightFeedback(haptic, context)
                                alertDuration = it 
                            },
                            valueRange = 5f..60f, // 5 seconds to 60 seconds
                            steps = 54, // 1-second increments
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = when (themeState.themeStyle) {
                                    AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                                    AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                                text = "Quick",
                            style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Long",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // TODO: Alert mode selection temporarily disabled for further testing
                /*
                // Alert Mode Selection Card (Disabled)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) {
                                MaterialTheme.colorScheme.surfaceContainer
                            } else {
                                Color(0xFFF8F5FF)
                            }
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    // Content removed
                }
                */
                
                // Sound Type Selection Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) {
                                MaterialTheme.colorScheme.surfaceContainer
                            } else {
                                Color(0xFFF8F5FF)
                            }
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Sound enabled toggle
                    Row(
                    modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔊 Sound Alerts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = { soundEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = when (themeState.themeStyle) {
                                        AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                                        AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    checkedTrackColor = when (themeState.themeStyle) {
                                        AppThemeStyle.AMOLED -> Color(0xFF4CAF50).copy(alpha = 0.5f)
                                        AppThemeStyle.CLASSIC -> Color(0xFF674FA3).copy(alpha = 0.5f)
                                        else -> MaterialTheme.colorScheme.primaryContainer
                                    }
                                )
                            )
                        }
                        
                        // Sound selection (only show when sound is enabled)
                        if (soundEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Selected Sound:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    color = when (themeState.themeStyle) {
                                        AppThemeStyle.AMOLED -> Color(0xFF2D2D2D)
                                        AppThemeStyle.CLASSIC -> Color(0xFFE8DEF8)
                                        else -> MaterialTheme.colorScheme.primaryContainer
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = soundName,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (themeState.themeStyle) {
                                            AppThemeStyle.AMOLED -> Color.White
                                            AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        
                        // Sound selection buttons (only show when sound is enabled)
                        if (soundEnabled) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // System sounds button
                                Button(
                                    onClick = {
                                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Glyph Guard Sound")
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                            
                                            // Set current selection
                                            selectedRingtoneUri?.let { uriString ->
                                                try {
                                                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(uriString))
                                                } catch (e: Exception) {
                                                    // If current URI is invalid, don't set it
                                                }
                                            }
                                        }
                                        ringtonePickerLauncher.launch(intent)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when (themeState.themeStyle) {
                                            AppThemeStyle.AMOLED -> Color(0xFF2D2D2D)
                                            AppThemeStyle.CLASSIC -> Color(0xFFE8E1F5)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        contentColor = when (themeState.themeStyle) {
                                            AppThemeStyle.AMOLED -> Color.White
                                            AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "🎵 System",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                // Custom file button
                                Button(
                                    onClick = {
                                        customFilePickerLauncher.launch("audio/*")
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when (themeState.themeStyle) {
                                            AppThemeStyle.AMOLED -> Color(0xFF2D2D2D)
                                            AppThemeStyle.CLASSIC -> Color(0xFFE8E1F5)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        contentColor = when (themeState.themeStyle) {
                                            AppThemeStyle.AMOLED -> Color.White
                                            AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "📁 Custom",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary Enable Button
                var isSaving by remember { mutableStateOf(false) }
                val currentlyEnabled = remember { settingsRepository.isGlyphGuardEnabled() }

                ElevatedButton(
                    onClick = {
                        HapticUtils.triggerMediumFeedback(haptic, context)
                        isSaving = true
                        val newDuration = (alertDuration * 1000).toLong()
                        settingsRepository.saveGlyphGuardDuration(newDuration)
                        settingsRepository.saveGlyphGuardCustomRingtoneUri(selectedRingtoneUri)
                        settingsRepository.saveGlyphGuardSoundEnabled(soundEnabled)
                        // Alert mode temporarily locked; skip saving until fully implemented

                        val soundText = if (soundEnabled) "with ${soundName.lowercase()} sound" else "silent mode"
                        android.widget.Toast.makeText(
                            context,
                            "🛡️ Glyph Guard ${if (currentlyEnabled) "updated" else "enabled"}: ${alertDuration.toInt()}s $soundText",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        
                        onEnable()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                            AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp
                    ),
                    shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = if (currentlyEnabled) "💾 Save Settings" else "🛡️ Enable Glyph Guard",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                // Secondary Actions Row - Disable and Cancel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                    // Disable Button
                    Button(
                        onClick = {
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            android.widget.Toast.makeText(
                                context,
                                "🛡️ Glyph Guard disabled",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            onDisable()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NothingRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "✖️ Disable",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Cancel Button
                        OutlinedButton(
                        onClick = { 
                            HapticUtils.triggerLightFeedback(haptic, context)
                            onDismiss() 
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "✕ Cancel",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    )
}

/**
 * Information Card - displays information about PowerPeek and Glyph Guard features
 */
@Composable
fun InformationCard(
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    // Use FeatureCard directly to avoid the disabled appearance from WideFeatureCard's isServiceActive logic
    FeatureCard(
        title = "Information",
        description = "Discover helpful guides and tips for all app features.",
        icon = painterResource(id = R.drawable.resource__),
        onClick = { 
            HapticUtils.triggerLightFeedback(haptic, context)
            showDialog = true
        },
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        iconSize = 32,
        contentPadding = PaddingValues(16.dp)
    )
    
    if (showDialog) {
        InformationSelectionDialog(
            onDismiss = { showDialog = false }
        )
    }
}

/**
 * Information Selection Dialog - matches the 3-button layout of PowerPeek and Glyph Guard dialogs
 */
@Composable
private fun InformationSelectionDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var showPowerPeekInfo by remember { mutableStateOf(false) }
    var showGlyphGuardInfo by remember { mutableStateOf(false) }
    var showGlowGateInfo by remember { mutableStateOf(false) }
    var showLowBatteryInfo by remember { mutableStateOf(false) }
    var showBatteryStoryInfo by remember { mutableStateOf(false) }
    var showQuietHoursInfo by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ℹ️ Information",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Learn about app features",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Feature description
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> Color(0xFFF8F5FF)
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📚 Choose what to learn:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• PowerPeek: Battery monitoring with shake detection\n• Glyph Guard: USB theft protection system\n• Glow Gate: Unlock animation with glyphs\n• Low Battery Alert: Get notified when battery is low\n• Battery Story: Track charging patterns and health\n• Quiet Hours: Automatically disable glyphs during sleep",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary Action - PowerPeek Information
                ElevatedButton(
                    onClick = {
                        HapticUtils.triggerMediumFeedback(haptic, context)
                        showPowerPeekInfo = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                            AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "⚡ Power Peek Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Secondary Actions Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Glyph Guard Information Button
                    Button(
                        onClick = {
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            showGlyphGuardInfo = true
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color(0xFF7B1FA2)
                                AppThemeStyle.CLASSIC -> Color(0xFF7B1FA2)
                                else -> MaterialTheme.colorScheme.secondary
                            },
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "🛡️ Guard",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Glow Gate Information Button
                    Button(
                        onClick = {
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            showGlowGateInfo = true
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color(0xFF2196F3)
                                AppThemeStyle.CLASSIC -> Color(0xFF2196F3)
                                else -> MaterialTheme.colorScheme.tertiary
                            },
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "✨ Gate",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Secondary Actions Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Low Battery Alert Information Button
                    Button(
                        onClick = {
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            showLowBatteryInfo = true
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color(0xFFFF9800)
                                AppThemeStyle.CLASSIC -> Color(0xFFFF9800)
                                else -> Color(0xFFFF9800)
                            },
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "🔋 Alert",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Battery Story Information Button
                    Button(
                        onClick = {
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            showBatteryStoryInfo = true
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                                AppThemeStyle.CLASSIC -> Color(0xFF4CAF50)
                                else -> Color(0xFF4CAF50)
                            },
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "📊 Story",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Secondary Actions Row 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quiet Hours Information Button
                    Button(
                        onClick = {
                            HapticUtils.triggerMediumFeedback(haptic, context)
                            showQuietHoursInfo = true
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color(0xFF9C27B0)
                                AppThemeStyle.CLASSIC -> Color(0xFF9C27B0)
                                else -> Color(0xFF9C27B0)
                            },
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "🌙 Quiet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Cancel Button
                    OutlinedButton(
                        onClick = { 
                            HapticUtils.triggerLightFeedback(haptic, context)
                            onDismiss() 
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "✕ Cancel",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    )
    
    // PowerPeek Information Dialog
    if (showPowerPeekInfo) {
        PowerPeekInformationDialog(
            onDismiss = { 
                showPowerPeekInfo = false
                onDismiss()
            }
        )
    }
    
    // Glyph Guard Information Dialog
    if (showGlyphGuardInfo) {
        GlyphGuardInformationDialog(
            onDismiss = { 
                showGlyphGuardInfo = false
                onDismiss()
            }
        )
    }

    // Glow Gate Information Dialog
    if (showGlowGateInfo) {
        GlowGateInformationDialog(
            onDismiss = { 
                showGlowGateInfo = false
                onDismiss()
            }
        )
    }

    // Low Battery Alert Information Dialog
    if (showLowBatteryInfo) {
        LowBatteryAlertInformationDialog(
            onDismiss = { 
                showLowBatteryInfo = false
                onDismiss()
            }
        )
    }

    // Battery Story Information Dialog
    if (showBatteryStoryInfo) {
        BatteryStoryInformationDialog(
            onDismiss = { 
                showBatteryStoryInfo = false
                onDismiss()
            }
        )
    }

    // Quiet Hours Information Dialog
    if (showQuietHoursInfo) {
        QuietHoursInformationDialog(
            onDismiss = { 
                showQuietHoursInfo = false
                onDismiss()
            }
        )
    }
}

/**
 * PowerPeek Information Dialog - detailed information about PowerPeek
 */
@Composable
private fun PowerPeekInformationDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚡ Power Peek",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Battery monitoring with shake detection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> Color(0xFFF8F5FF)
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🔋 What is Power Peek?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Power Peek displays your battery percentage on the Nothing Phone's glyph interface when you shake the device. It's perfect for checking battery status without turning on the screen.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Text(
                            text = "✨ Key Features:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Shake-to-activate battery display\n• Customizable sensitivity settings\n• Adjustable display duration\n• Works even when screen is off\n• Beautiful glyph animations\n• Battery-efficient background operation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Text(
                            text = "⚙️ How to use:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "1. Enable Power Peek from the main screen\n2. Adjust sensitivity and duration in settings\n3. Shake your phone to see battery level\n4. Glyphs will light up showing percentage",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            ElevatedButton(
                onClick = { 
                    HapticUtils.triggerMediumFeedback(haptic, context)
                    onDismiss() 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = when (themeState.themeStyle) {
                        AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                        AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                        else -> MaterialTheme.colorScheme.primary
                    },
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "✓ Got it!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    )
}

/**
 * Glyph Guard Information Dialog - detailed information about Glyph Guard
 */
@Composable
private fun GlyphGuardInformationDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🛡️ Glyph Guard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "USB theft protection system",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> Color(0xFFF8F5FF)
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🔒 What is Glyph Guard?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Glyph Guard protects your Nothing Phone from theft when charging. It instantly triggers visual and audio alerts if someone unplugs your USB cable without permission.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Text(
                            text = "🚨 Security Features:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Instant USB disconnect detection\n• Rapid glyph blinking alerts\n• Configurable sound alarms\n• Custom alert duration (5-60 seconds)\n• Works on lockscreen and screen-off\n• Battery optimization protection\n• Silent mode option available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Text(
                            text = "🎯 Perfect for:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Cafes and public charging stations\n• Hotels and airport lounges\n• Office desks and shared spaces\n• Anywhere you need to leave your phone charging\n• Peace of mind in crowded areas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Text(
                            text = "⚙️ How to use:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "1. Test the feature first to see how it works\n2. Configure alert duration and sound preferences\n3. Enable Glyph Guard when you start charging\n4. Your phone is now protected!\n5. Disable when you want to unplug safely",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            ElevatedButton(
                onClick = { 
                    HapticUtils.triggerMediumFeedback(haptic, context)
                    onDismiss() 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = when (themeState.themeStyle) {
                        AppThemeStyle.AMOLED -> Color(0xFF7B1FA2)
                        AppThemeStyle.CLASSIC -> Color(0xFF7B1FA2)
                        else -> MaterialTheme.colorScheme.secondary
                    },
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "✓ Got it!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    )
}

/**
 * Glow Gate Information Dialog - detailed information about Glow Gate
 */
@Composable
private fun GlowGateInformationDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "✨ Glow Gate",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Unlock animation with glyphs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> Color(0xFFF8F5FF)
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🔓 What is Glow Gate?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Glow Gate transforms your Nothing Phone's unlock experience with beautiful glyph animations. Every time you unlock your device, the glyphs light up in your chosen pattern, creating a magical moment.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Text(
                            text = "✨ Animation Features:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• 10 stunning animation patterns to choose from\n• Customizable animation duration\n• Optional sound effects with timing control\n• Works with fingerprint, face unlock, and PIN\n• Respects quiet hours automatically\n• Battery-optimized background operation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Text(
                            text = "🎨 Available Animations:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• C1 Sequential: Classic Nothing animation\n• Wave: Flowing wave pattern\n• Beedah: Unique sound-inspired pattern\n• Pulse: Gentle breathing effect\n• Padlock Sweep: Security-themed animation\n• Spiral: Hypnotic spiral pattern\n• Heartbeat: Rhythmic pulse animation\n• Matrix Rain: Digital rain effect\n• Fireworks: Celebratory burst pattern\n• DNA Helix: Scientific double helix",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Text(
                            text = "⚙️ How to use:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "1. Enable Glow Gate from the main screen\n2. Choose your favorite animation pattern\n3. Optionally add sound effects and timing\n4. Unlock your phone to see the magic!\n5. Adjust settings anytime in the configuration",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            ElevatedButton(
                onClick = { 
                    HapticUtils.triggerMediumFeedback(haptic, context)
                    onDismiss() 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = when (themeState.themeStyle) {
                        AppThemeStyle.AMOLED -> Color(0xFF2196F3)
                        AppThemeStyle.CLASSIC -> Color(0xFF2196F3)
                        else -> MaterialTheme.colorScheme.tertiary
                    },
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "✓ Got it!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    )
}

/**
 * Low Battery Alert Information Dialog - detailed information about Low Battery Alert
 */
@Composable
private fun LowBatteryAlertInformationDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🔋 Low Battery Alert",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Get notified when battery is low",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> Color(0xFFF8F5FF)
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "⚠️ What is Low Battery Alert?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Low Battery Alert keeps you informed about your battery status using the Nothing Phone's glyph interface. When your battery drops below a set threshold, the glyphs will flash with your chosen animation pattern.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Text(
                            text = "🔔 Alert Features:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Customizable battery threshold (5-50%)\n• 10 different animation patterns\n• Configurable alert duration (2-15 seconds)\n• Optional sound effects with timing control\n• Works even when screen is off\n• Respects quiet hours automatically\n• Battery-efficient background monitoring",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Text(
                            text = "🎯 Perfect for:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Never miss low battery warnings\n• Visual alerts that work in any lighting\n• Customizable to your preferences\n• Works during meetings or quiet times\n• Battery health awareness\n• Peace of mind about device status",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Text(
                            text = "⚙️ How to use:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "1. Enable Low Battery Alert from the main screen\n2. Set your preferred battery threshold\n3. Choose an animation pattern and duration\n4. Optionally add sound effects\n5. Test the alert to see how it works\n6. Your phone will now alert you when battery is low!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            ElevatedButton(
                onClick = { 
                    HapticUtils.triggerMediumFeedback(haptic, context)
                    onDismiss() 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = when (themeState.themeStyle) {
                        AppThemeStyle.AMOLED -> Color(0xFFFF9800)
                        AppThemeStyle.CLASSIC -> Color(0xFFFF9800)
                        else -> Color(0xFFFF9800)
                    },
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "✓ Got it!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    )
}

/**
 * Battery Story Information Dialog - detailed information about Battery Story
 */
@Composable
private fun BatteryStoryInformationDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "📊 Battery Story",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Track charging patterns and battery health",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> Color(0xFFF8F5FF)
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📈 What is Battery Story?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Battery Story provides detailed insights into your device's charging patterns and battery health. Track your charging sessions, analyze battery usage trends, and monitor your battery's overall health over time.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Text(
                            text = "📊 Tracking Features:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Automatic charging session tracking\n• Detailed session history and statistics\n• Battery health scoring system\n• Charging pattern analysis\n• Time-based usage insights\n• Battery efficiency metrics\n• Historical trend visualization",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Text(
                            text = "🔍 What you can learn:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• How long your charging sessions typically last\n• Your most common charging times\n• Battery health trends over time\n• Charging efficiency patterns\n• Optimal charging habits\n• When to consider battery replacement\n• Usage patterns that affect battery life",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Text(
                            text = "⚙️ How to use:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "1. Enable Battery Story from the main screen\n2. Use your phone normally - tracking happens automatically\n3. Open Battery Story to view your charging history\n4. Analyze patterns and battery health scores\n5. Use insights to optimize your charging habits\n6. Monitor long-term battery health trends",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            ElevatedButton(
                onClick = { 
                    HapticUtils.triggerMediumFeedback(haptic, context)
                    onDismiss() 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = when (themeState.themeStyle) {
                        AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                        AppThemeStyle.CLASSIC -> Color(0xFF4CAF50)
                        else -> Color(0xFF4CAF50)
                    },
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "✓ Got it!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    )
}

/**
 * Quiet Hours Information Dialog - detailed information about Quiet Hours
 */
@Composable
private fun QuietHoursInformationDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🌙 Quiet Hours",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Automatically disable glyphs during sleep",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> Color(0xFFF8F5FF)
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🌃 What is Quiet Hours?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Quiet Hours automatically disables all glyph animations during your specified sleep time. This ensures a peaceful night's rest without any unexpected light shows from your phone's glyph interface.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Text(
                            text = "🌙 Sleep Features:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Customizable start and end times\n• Automatic glyph animation blocking\n• Affects all glyph features during quiet hours\n• Respects your sleep schedule\n• No manual intervention required\n• Battery optimization during sleep\n• Peaceful charging without light shows",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Text(
                            text = "🎯 Perfect for:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Nighttime charging without disturbances\n• Shared bedrooms and living spaces\n• Light-sensitive sleep environments\n• Consistent sleep schedules\n• Battery charging during sleep\n• Peaceful overnight device usage\n• Respecting others' sleep needs",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                        
                        Text(
                            text = "⚙️ How to use:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "1. Enable Quiet Hours from the main screen\n2. Set your preferred start and end times\n3. Configure in Settings > Quiet Hours\n4. All glyph animations will be blocked during quiet hours\n5. Your phone will respect your sleep schedule automatically\n6. Adjust times anytime to match your routine",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            ElevatedButton(
                onClick = { 
                    HapticUtils.triggerMediumFeedback(haptic, context)
                    onDismiss() 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = when (themeState.themeStyle) {
                        AppThemeStyle.AMOLED -> Color(0xFF9C27B0)
                        AppThemeStyle.CLASSIC -> Color(0xFF9C27B0)
                        else -> Color(0xFF9C27B0)
                    },
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "✓ Got it!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    )
}

// New square card for Battery Story feature
@Composable
fun BatteryStoryCard(
    title: String = "Battery Story",
    description: String = "View charging history",
    icon: Painter,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Int = 32,
    isServiceActive: Boolean = true,
    settingsRepository: SettingsRepository
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var isBatteryStoryEnabled by remember { mutableStateOf(settingsRepository.isBatteryStoryEnabled()) }

    var isPressed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale",
        finishedListener = { finalValue ->
            if (finalValue == 0.95f && isPressed) {
                // Wait briefly so user can perceive the press state
                coroutineScope.launch {
                    kotlinx.coroutines.delay(150)
                    if (isBatteryStoryEnabled) {
                        onOpen()
                    } else {
                        Toast.makeText(context, "Please enable Battery Story first", Toast.LENGTH_SHORT).show()
                    }
                    isPressed = false
                }
            }
        }
    )

    val alpha by animateFloatAsState(
        targetValue = if (isServiceActive && isBatteryStoryEnabled) 1f else 0.3f,
        animationSpec = tween(300),
        label = "alpha"
    )

    // Calculate the resolved tint - use purple when service inactive, green when enabled
    val resolvedTint = when {
        !isServiceActive -> Color(0xFF674FA3) // Purple when master service OFF
        isBatteryStoryEnabled -> ACTIVE_GREEN // Green when feature ON
        else -> null // Use default tint when feature OFF
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(alpha)
    ) {
        FeatureCard(
            title = title,
            description = description,
            icon = icon,
            onClick = {
                if (!isPressed) isPressed = true
            },
            modifier = Modifier.fillMaxSize(),
            iconSize = iconSize,
            contentPadding = PaddingValues(16.dp),
            iconTint = resolvedTint
        )

        // Enable / Disable toggle in the corner
        MorphingToggleButton(
            checked = isBatteryStoryEnabled,
            onCheckedChange = { enabled ->
                isBatteryStoryEnabled = enabled
                settingsRepository.saveBatteryStoryEnabled(enabled)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-12).dp, y = 12.dp)
        )
    }
}

    // Placeholder square card for the upcoming Glow Gate feature
@Composable
fun PulseLockCard(
    title: String,
    description: String,
    icon: Painter,
    onTestPulseLock: () -> Unit,
    onEnablePulseLock: () -> Unit,
    onDisablePulseLock: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Int = 32,
    isServiceActive: Boolean = true,
    settingsRepository: SettingsRepository,
    autoDemo: Boolean = false // automatically show dialog once when rendered
) {
    var isPressed by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var pulseLockEnabled by remember { mutableStateOf(settingsRepository.isPulseLockEnabled()) }
    val coroutineScope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "scale",
        finishedListener = { finalValue ->
            if (finalValue == 0.95f && isPressed && isServiceActive) {
                coroutineScope.launch {
                    kotlinx.coroutines.delay(150)
                    showDialog = true
                    isPressed = false
                }
            }
        }
    )

    val alpha by animateFloatAsState(
        targetValue = if (isServiceActive) 1f else 0.3f,
        animationSpec = tween(300),
        label = "alpha"
    )

    // Calculate the resolved tint like the other cards do
    val resolvedTint = when {
        !isServiceActive -> Color(0xFF674FA3) // Purple when master service OFF
        pulseLockEnabled -> ACTIVE_GREEN      // Green when feature ON
        else -> INACTIVE_RED                  // Red when feature OFF
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .alpha(alpha)
    ) {
        FeatureCard(
            title = title,
            description = description,
            icon = icon,
            onClick = { if (!isPressed) isPressed = true },
            modifier = Modifier.fillMaxSize(),
            iconSize = iconSize,
            contentPadding = PaddingValues(16.dp),
            iconTint = resolvedTint
        )
    }

    if (showDialog && isServiceActive) {
        com.bleelblep.glyphsharge.ui.components.PulseLockConfirmationDialog(
            onTestPulseLock = onTestPulseLock,
            onEnablePulseLock = { onEnablePulseLock(); pulseLockEnabled = true },
            onDisablePulseLock = { onDisablePulseLock(); pulseLockEnabled = false },
            onDismiss = { showDialog = false },
            settingsRepository = settingsRepository
        )
    }

    // Automatically trigger dialog for onboarding demo
    LaunchedEffect(autoDemo) {
        if (autoDemo && isServiceActive) {
            delay(600)
            showDialog = true
            // Auto close after 1.5s
            delay(1500)
            showDialog = false
        }
    }
}

// ---------------- Low-Battery Alert Card (wide) ----------------

@Composable
fun LowBatteryAlertCard(
    onTestAlert: () -> Unit,
    title: String = "Low Battery Alert",
    description: String = "Get notified with glyphs when your battery runs low.",
    icon: Painter = rememberVectorPainter(Icons.Default.BatteryAlert),
    modifier: Modifier = Modifier,
    iconSize: Int = 32,
    isServiceActive: Boolean = true,
    settingsRepository: SettingsRepository
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var enabled by remember { mutableStateOf(settingsRepository.isLowBatteryEnabled()) }

    val alpha by animateFloatAsState(
        targetValue = if (isServiceActive) 1f else 0.3f,
        animationSpec = tween(300),
        label = "lowBatteryAlpha"
    )

    val resolvedTint = when {
        !isServiceActive -> Color(0xFF674FA3) // Purple when master service OFF
        enabled -> ACTIVE_GREEN              // Green when feature ON
        else -> INACTIVE_RED                 // Red when feature OFF
    }

    Box(modifier = modifier.alpha(alpha)) {
        FeatureCard(
            title = title,
            description = description,
            icon = icon,
            onClick = { if (isServiceActive) showDialog = true else Toast.makeText(context, "Please enable the Glyph service first", Toast.LENGTH_SHORT).show() },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            iconSize = iconSize,
            contentPadding = PaddingValues(16.dp),
            iconTint = resolvedTint
        )
    }

    // Toggle in corner
    Box(modifier = modifier.fillMaxWidth().height(0.dp)) { // overlay on same place if needed
        MorphingToggleButton(
            checked = enabled,
            onCheckedChange = { e ->
                enabled = e
                settingsRepository.saveLowBatteryEnabled(e)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-12).dp, y = 12.dp)
        )
    }

    // Confirmation dialog
    if (showDialog && isServiceActive) {
        LowBatteryAlertConfirmationDialog(
            onTestAlert = { onTestAlert(); showDialog = false },
            onEnableAlert = { showDialog = false; showSettingsDialog = true },
            onDisableAlert = { 
                enabled = false
                settingsRepository.saveLowBatteryEnabled(false)
                showDialog = false 
            },
            onDismiss = { showDialog = false },
            onConfirm = { config ->
                settingsRepository.saveLowBatteryEnabled(config.isEnabled)
                settingsRepository.saveLowBatteryThreshold(config.threshold)
                settingsRepository.saveLowBatteryAnimationId(config.animationId)
                // settingsRepository.saveLowBatteryDuration(config.duration) // Duration is now fixed at 5 seconds
                settingsRepository.saveLowBatteryAudioEnabled(config.audioEnabled)
                config.audioUri?.let { settingsRepository.saveLowBatteryAudioUri(it) }
                settingsRepository.saveLowBatteryAudioOffset(config.audioOffset)
                
                // Update enabled state - this fixes the green/red icon
                enabled = config.isEnabled
                showDialog = false
            },
            settingsRepository = settingsRepository
        )
    }

    // Settings dialog
    if (showSettingsDialog) {
        LowBatteryAlertEnableDialog(
            onConfirm = { config ->
                settingsRepository.saveLowBatteryEnabled(config.isEnabled)
                settingsRepository.saveLowBatteryThreshold(config.threshold)
                settingsRepository.saveLowBatteryAnimationId(config.animationId)
                // settingsRepository.saveLowBatteryDuration(config.duration) // Duration is now fixed at 5 seconds
                settingsRepository.saveLowBatteryAudioEnabled(config.audioEnabled)
                config.audioUri?.let { settingsRepository.saveLowBatteryAudioUri(it) }
                settingsRepository.saveLowBatteryAudioOffset(config.audioOffset)
                
                // Refresh enabled state
                enabled = config.isEnabled
                showSettingsDialog = false
            },
            onDismiss = {
                showSettingsDialog = false
                // Refresh enabled state in case user toggled the feature inside settings
                enabled = settingsRepository.isLowBatteryEnabled()
            },
            onDisable = {
                enabled = false
                settingsRepository.saveLowBatteryEnabled(false)
                showSettingsDialog = false
            },
            settingsRepository = settingsRepository
        )
    }
}

@Composable
private fun LowBatteryConfigDialog(
    onDismiss: () -> Unit,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val accentColor = when (themeState.themeStyle) {
        AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
        else -> Color(0xFF674FA3)
    }
    var threshold by remember { mutableStateOf(settingsRepository.getLowBatteryThreshold().toFloat()) }
    // Sound alert preferences
    var soundEnabled by remember { mutableStateOf(settingsRepository.isLowBatteryAudioEnabled()) }
    var soundUri by remember { mutableStateOf(settingsRepository.getLowBatteryAudioUri()) }
    var soundOffset by remember { mutableStateOf(maxOf(0f, settingsRepository.getLowBatteryAudioOffset().toFloat())) }

    val contextSound = LocalContext.current

    // Display name for selected sound
    val soundName = remember(soundUri) {
        soundUri?.let { uriString ->
            try {
                val uri = Uri.parse(uriString)
                val ringtone = RingtoneManager.getRingtone(contextSound, uri)
                val title = ringtone?.getTitle(contextSound)
                title?.takeIf { it.isNotEmpty() } ?: uri.lastPathSegment?.substringBeforeLast('.') ?: "Custom audio file"
            } catch (e: Exception) {
                "Custom audio file"
            }
        } ?: "No sound selected"
    }

    // Launchers for picking system ringtone or custom file
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == ComponentActivity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            soundUri = uri?.toString()
            settingsRepository.saveLowBatteryAudioUri(soundUri)
        }
    }

    val customFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val internalUri = copyFileToInternalStorage(contextSound, uri, "lowbattery_custom_audio")
                soundUri = (internalUri ?: uri).toString()
                settingsRepository.saveLowBatteryAudioUri(soundUri)
            } catch (_: Exception) {}
        }
    }

    // Animation choice and flash duration
    var selectedAnim by remember { mutableStateOf(PulseLockAnimations.getById(settingsRepository.getLowBatteryAnimationId())) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Configure",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Customize low battery alert",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Threshold Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) {
                                MaterialTheme.colorScheme.surfaceContainer
                            } else {
                                Color(0xFFF8F5FF)
                            }
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔋 Battery Threshold",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                color = when (themeState.themeStyle) {
                                    AppThemeStyle.AMOLED -> Color(0xFF2D2D2D)
                                    AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        Color(0xFFE8DEF8)
                                    }
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${threshold.toInt()}%",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when (themeState.themeStyle) {
                                        AppThemeStyle.AMOLED -> Color.White
                                        AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                                    }
                                )
                            }
                        }

                        Slider(
                            value = threshold,
                            onValueChange = { threshold = it.coerceIn(5f, 50f) },
                            valueRange = 5f..50f,
                            steps = 45,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = accentColor
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("5%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("50%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Animation Picker Card
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
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Get GlyphAnimationManager via dependency injection
                        val glyphAnimationManager = remember {
                            EntryPointAccessors.fromApplication(
                                context.applicationContext,
                                GlyphComponent::class.java
                            ).glyphAnimationManager()
                        }
                        val coroutineScope = rememberCoroutineScope()
                        
                        Text("🎞️ Animation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            PulseLockAnimations.list.forEach { anim ->
                                FilterChip(selected = anim == selectedAnim, onClick = {
                                    HapticUtils.triggerLightFeedback(haptic, context)
                                    selectedAnim = anim
                                    settingsRepository.saveLowBatteryAnimationId(anim.id)
                                }, label = { Text(anim.displayName) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer))
                            }
                        }
                        
                        // Test button for selected animation
                        Button(
                            onClick = {
                                HapticUtils.triggerMediumFeedback(haptic, context)
                                // Launch animation test in background
                                coroutineScope.launch {
                                    try {
                                        when (selectedAnim.id) {
                                            "SPIRAL" -> glyphAnimationManager.runSpiralAnimation()
                                            "HEARTBEAT" -> glyphAnimationManager.runHeartbeatAnimation()
                                            "MATRIX" -> glyphAnimationManager.runMatrixRainAnimation()
                                            "FIREWORKS" -> glyphAnimationManager.runFireworksAnimation()
                                            "DNA" -> glyphAnimationManager.runDNAHelixAnimation()
                                            else -> glyphAnimationManager.playLowBatteryAnimation(selectedAnim.id)
                                        }
                                    } catch (e: Exception) {
                                        Log.e("HomeCardTemplates", "Error testing animation: ${e.message}")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (themeState.themeStyle) {
                                    AppThemeStyle.AMOLED -> Color(0xFF2D2D2D)
                                    AppThemeStyle.CLASSIC -> Color(0xFFE8E1F5)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                contentColor = when (themeState.themeStyle) {
                                    AppThemeStyle.AMOLED -> Color.White
                                    AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🧪 Test \"${selectedAnim.displayName}\"",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }



                // Sound Alerts Card (reuse PulseLock style)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) {
                                MaterialTheme.colorScheme.surfaceContainer
                            } else {
                                Color(0xFFF8F5FF)
                            }
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🔊 Sound Alerts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = {
                                    HapticUtils.triggerLightFeedback(haptic, context)
                                    soundEnabled = it
                                    settingsRepository.saveLowBatteryAudioEnabled(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = accentColor,
                                    checkedTrackColor = accentColor.copy(alpha = 0.5f)
                                )
                            )
                        }

                        if (soundEnabled) {
                            // Current sound name
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Selected Sound:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = soundName,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL)
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alert Sound")
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                                            soundUri?.let { uriStr -> putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(uriStr)) }
                                        }
                                        ringtonePickerLauncher.launch(intent)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when (themeState.themeStyle) {
                                            AppThemeStyle.AMOLED -> Color(0xFF2D2D2D)
                                            AppThemeStyle.CLASSIC -> Color(0xFFE8E1F5)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        contentColor = when (themeState.themeStyle) {
                                            AppThemeStyle.AMOLED -> Color.White
                                            AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("🎵 System", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = { customFilePickerLauncher.launch("audio/*") },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when (themeState.themeStyle) {
                                            AppThemeStyle.AMOLED -> Color(0xFF2D2D2D)
                                            AppThemeStyle.CLASSIC -> Color(0xFFE8E1F5)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        contentColor = when (themeState.themeStyle) {
                                            AppThemeStyle.AMOLED -> Color.White
                                            AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("📁 Custom", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }

                            // Offset slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Audio Offset:")
                                Text("${soundOffset.toInt()}ms")
                            }
                            Slider(
                                value = soundOffset,
                                onValueChange = {
                                    HapticUtils.triggerLightFeedback(haptic, context)
                                    soundOffset = it
                                    settingsRepository.saveLowBatteryAudioOffset(it.toLong())
                                },
                                valueRange = 0f..1500f,
                                steps = 29,
                                modifier = Modifier.fillMaxWidth(),
                                colors = SliderDefaults.colors(thumbColor = accentColor)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            val primaryColor = accentColor

            val currentlyEnabled = remember { settingsRepository.isLowBatteryEnabled() }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                var isSaving by remember { mutableStateOf(false) }

                ElevatedButton(
                    onClick = {
                        isSaving = true
                        // Always save settings
                        settingsRepository.saveLowBatteryThreshold(threshold.toInt())
                        settingsRepository.saveLowBatteryAudioEnabled(soundEnabled)
                        settingsRepository.saveLowBatteryAudioUri(soundUri)
                        settingsRepository.saveLowBatteryAudioOffset(soundOffset.toLong())
                        settingsRepository.saveLowBatteryAnimationId(selectedAnim.id)

                        // Enable if currently disabled
                        if (!currentlyEnabled) settingsRepository.saveLowBatteryEnabled(true)

                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.elevatedButtonColors(containerColor = primaryColor, contentColor = Color.White),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp, pressedElevation = 12.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            text = if (currentlyEnabled) "💾 Save Settings" else "⚡ Enable Alert",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            settingsRepository.saveLowBatteryEnabled(false)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NothingRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("✖️ Disable", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("✕ Cancel", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    )
}

@Composable
private fun LowBatteryConfirmationDialog(
    onTest: () -> Unit,
    onSettings: () -> Unit,
    onDismiss: () -> Unit,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val themeState = LocalThemeState.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    var showSettingsDialogInternal by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { /* Prevent outside dismiss */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        ),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🔋 Low-Battery Alert",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Flash glyphs below threshold",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // How-it-works / summary card (mirrors Pulse-Lock style)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF1A1A1A)
                            AppThemeStyle.CLASSIC -> if (themeState.isDarkTheme) {
                                MaterialTheme.colorScheme.surfaceContainer
                            } else {
                                Color(0xFFF8F5FF)
                            }
                            else -> MaterialTheme.colorScheme.surfaceContainer
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "✨ How it works:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "• Triggers once each discharge cycle when level ≤ threshold\n" +
                                    "• Plays selected animation for 5 seconds\n" +
                                    "• Optional sound with adjustable delay",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Primary action: Test Alert
                ElevatedButton(
                    onClick = {
                        HapticUtils.triggerMediumFeedback(haptic, context)
                        onTest()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = when (themeState.themeStyle) {
                            AppThemeStyle.AMOLED -> Color(0xFF4CAF50)
                            AppThemeStyle.CLASSIC -> Color(0xFF674FA3)
                            else -> MaterialTheme.colorScheme.primary
                        },
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp, pressedElevation = 12.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("🧪 Test Alert", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                // Secondary row: Settings & Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { 
                            HapticUtils.triggerLightFeedback(haptic, context)
                            showSettingsDialogInternal = true 
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color(0xFF2D4A3E)
                                AppThemeStyle.CLASSIC -> Color(0xFF8D7BA5)
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            },
                            contentColor = when (themeState.themeStyle) {
                                AppThemeStyle.AMOLED -> Color.White
                                AppThemeStyle.CLASSIC -> Color.White
                                else -> MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("⚙️ Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                    ) {
                        Text("✕ Cancel", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        dismissButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    )

    // In case caller didn't show settings, handle here (mirrors Pulse-Lock logic)
    if (showSettingsDialogInternal) {
        LowBatteryConfigDialog(
            onDismiss = { showSettingsDialogInternal = false },
            settingsRepository = settingsRepository
        )
    }
}

/**
 * Copy a file from external URI to internal storage to avoid permission issues
 */
private fun copyFileToInternalStorage(context: android.content.Context, sourceUri: Uri, fileName: String): Uri? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
        if (inputStream == null) {
            Log.e("GlyphGuardComponents", "Could not open input stream for URI: $sourceUri")
            return null
        }
        
        // Get file extension from original URI
        val originalFileName = sourceUri.lastPathSegment ?: "audio"
        val extension = when {
            originalFileName.contains(".mp3", ignoreCase = true) -> ".mp3"
            originalFileName.contains(".wav", ignoreCase = true) -> ".wav"
            originalFileName.contains(".m4a", ignoreCase = true) -> ".m4a"
            originalFileName.contains(".aac", ignoreCase = true) -> ".aac"
            originalFileName.contains(".ogg", ignoreCase = true) -> ".ogg"
            else -> ".mp3" // default
        }
        
        // Create file in internal storage
        val internalFile = File(context.filesDir, "$fileName$extension")
        
        // Copy the file
        inputStream.use { input ->
            FileOutputStream(internalFile).use { output ->
                input.copyTo(output)
            }
        }
        
        Log.d("GlyphGuardComponents", "Successfully copied file to: ${internalFile.absolutePath}")
        
        // Return file:// URI for internal storage
        Uri.fromFile(internalFile)
        
    } catch (e: Exception) {
        Log.e("GlyphGuardComponents", "Failed to copy file to internal storage", e)
        null
    }
}

 
