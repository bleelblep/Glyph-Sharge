package com.bleelblep.glyphsharge.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.*
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.bleelblep.glyphsharge.ui.theme.*
import com.bleelblep.glyphsharge.ui.utils.HapticUtils
import kotlin.math.roundToInt
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.unit.IntOffset

// Fixed colors for consistent theming instead of Material You dynamic colors
private val GlyphPurple = Color(0xFF7B1FA2)
private val GlyphPurpleContainer = Color(0xFFE8E1F5)
private val GlyphGray = Color(0xFF9E9E9E)
private val GlyphGrayContainer = Color(0xFFF5F5F5)
private val GlyphRed = Color(0xFFd71921)
private val GlyphRedContainer = Color(0xFFFFE5E6)

/**
 * Three-state morphing toggle for font family selection
 * Each state has a distinct shape, color, and visual representation
 */
@Composable
fun ThreeStateFontToggle(
    currentVariant: FontVariant,
    onVariantSelected: (FontVariant) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    val variants = remember {
        listOf(FontVariant.HEADLINE, FontVariant.NDOT, FontVariant.SYSTEM)
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        variants.forEach { variant ->
            ThreeStateFontToggleItem(
                variant = variant,
                isSelected = variant == currentVariant,
                onClick = {
                    HapticUtils.triggerLightFeedback(haptic, context)
                    onVariantSelected(variant)
                }
            )
        }
    }
}

/**
 * Individual toggle item with morphing design
 */
@Composable
private fun ThreeStateFontToggleItem(
    variant: FontVariant,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedShape by animateIntAsState(
        targetValue = if (isSelected) 1 else 0,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "shapeAnimation"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> when (variant) {
                FontVariant.HEADLINE -> GlyphPurple
                FontVariant.NDOT -> GlyphPurple
                FontVariant.SYSTEM -> GlyphRed
            }
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300),
        label = "backgroundColorAnimation"
    )
    
    val contentColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color.White // White text on colored backgrounds
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(300),
        label = "contentColorAnimation"
    )
    
    val animatedSize by animateDpAsState(
        targetValue = if (isSelected) 52.dp else 44.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "sizeAnimation"
    )
    
    val shape = remember(variant, animatedShape) {
        when (variant) {
            FontVariant.HEADLINE -> if (animatedShape == 1) RoundedCornerShape(16.dp) else CircleShape
            FontVariant.NDOT -> if (animatedShape == 1) RoundedCornerShape(4.dp) else CircleShape
            FontVariant.SYSTEM -> CircleShape
        }
    }
    
    Box(
        modifier = modifier
            .size(animatedSize)
            .background(
                color = backgroundColor,
                shape = shape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        val text = remember(variant) {
            when (variant) {
                FontVariant.HEADLINE -> "T"
                FontVariant.NDOT -> "N"
                FontVariant.SYSTEM -> "S"
            }
        }
        
        val fontFamily = remember(variant) {
            when (variant) {
                FontVariant.HEADLINE -> FontFamily(Font(com.bleelblep.glyphsharge.R.font.ntype_82_headline))
                FontVariant.NDOT -> FontFamily(Font(com.bleelblep.glyphsharge.R.font.ndot55caps))
                FontVariant.SYSTEM -> FontFamily.Default
            }
        }
        
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = fontFamily,
                fontSize = if (isSelected) 18.sp else 16.sp
            ),
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

/**
 * Simple Font Family Selector with clean button-based selection
 */
@Composable
fun SimpleFontSelector(
    currentVariant: FontVariant,
    onVariantSelected: (FontVariant) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Font Family",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Select your preferred typography style",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Font options as simple buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SimpleFontButton(
                    variant = FontVariant.HEADLINE,
                    title = "NType Headline",
                    description = "Modern Nothing display font",
                    preview = "Typography",
                    isSelected = currentVariant == FontVariant.HEADLINE,
                    onClick = { 
                        HapticUtils.triggerLightFeedback(haptic, context)
                        onVariantSelected(FontVariant.HEADLINE) 
                    }
                )
                
                SimpleFontButton(
                    variant = FontVariant.NDOT,
                    title = "NDot 57 Caps",
                    description = "Distinctive caps-only typeface",
                    preview = "TYPOGRAPHY",
                    isSelected = currentVariant == FontVariant.NDOT,
                    onClick = { 
                        HapticUtils.triggerLightFeedback(haptic, context)
                        onVariantSelected(FontVariant.NDOT) 
                    }
                )
                
                SimpleFontButton(
                    variant = FontVariant.SYSTEM,
                    title = "System Default",
                    description = "Your device's default font",
                    preview = "Typography",
                    isSelected = currentVariant == FontVariant.SYSTEM,
                    onClick = { 
                        HapticUtils.triggerLightFeedback(haptic, context)
                        onVariantSelected(FontVariant.SYSTEM) 
                    }
                )
            }
        }
    }
}

/**
 * Simple font selection button with improved contrast
 */
@Composable
private fun SimpleFontButton(
    variant: FontVariant,
    title: String,
    description: String,
    preview: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            when (variant) {
                FontVariant.HEADLINE -> GlyphPurpleContainer
                FontVariant.NDOT -> GlyphPurpleContainer
                FontVariant.SYSTEM -> GlyphRedContainer
            }
        } else {
            Color.Transparent
        },
        animationSpec = tween(300),
        label = "fontButtonBackground"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            when (variant) {
                FontVariant.HEADLINE -> GlyphPurple
                FontVariant.NDOT -> GlyphPurple
                FontVariant.SYSTEM -> GlyphRed
            }
        } else {
            MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(300),
        label = "fontButtonBorder"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Font info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Font preview
            Text(
                text = preview,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = when (variant) {
                        FontVariant.HEADLINE -> FontFamily(Font(com.bleelblep.glyphsharge.R.font.ntype_82_headline))
                        FontVariant.NDOT -> FontFamily(Font(com.bleelblep.glyphsharge.R.font.ndot55caps))
                        FontVariant.SYSTEM -> FontFamily.Default
                    }
                ),
                color = if (isSelected) {
                    when (variant) {
                        FontVariant.HEADLINE -> GlyphPurple
                        FontVariant.NDOT -> GlyphPurple
                        FontVariant.SYSTEM -> GlyphRed
                    }
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = when (variant) {
                        FontVariant.HEADLINE -> GlyphPurple
                        FontVariant.NDOT -> GlyphPurple
                        FontVariant.SYSTEM -> GlyphRed
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Draggable Font Family Selector with reordering capability
 */
@Composable
fun DraggableFontSelector(
    currentVariant: FontVariant,
    onVariantSelected: (FontVariant) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    
    // Font variants with their display info
    val fontVariants = remember {
        listOf(
            FontVariant.HEADLINE to Triple("NType Headline", "Modern Nothing display font", "Aa"),
            FontVariant.NDOT to Triple("NDot 57 Caps", "Distinctive caps-only typeface", "NN"),
            FontVariant.SYSTEM to Triple("System Default", "Your device's default font", "Ab")
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Detailed Font Selection",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Drag to reorder • Tap to select",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Font options
            fontVariants.forEach { (variant, details) ->
                DraggableFontItem(
                    variant = variant,
                    title = details.first,
                    description = details.second,
                    preview = details.third,
                    isSelected = variant == currentVariant,
                    onClick = { onVariantSelected(variant) }
                )
                
                if (variant != fontVariants.last().first) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Individual draggable font item
 */
@Composable
private fun DraggableFontItem(
    variant: FontVariant,
    title: String,
    description: String,
    preview: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val animatedElevation by animateDpAsState(
        targetValue = if (isDragging) 8.dp else if (isSelected) 4.dp else 1.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "fontItemElevation"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> GlyphPurpleContainer
            isDragging -> GlyphPurpleContainer.copy(alpha = 0.7f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(300),
        label = "fontItemBackground"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
            .pointerInput(variant) {
                detectDragGestures(
                    onDragStart = { 
                        isDragging = true
                        HapticUtils.triggerLightFeedback(haptic, context)
                    },
                    onDragEnd = {
                        isDragging = false
                        dragOffset = Offset.Zero
                    }
                ) { change, dragAmount ->
                    dragOffset += dragAmount
                }
            }
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag handle
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag to reorder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Font info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Font preview
            Text(
                text = preview,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = when (variant) {
                        FontVariant.HEADLINE -> FontFamily(Font(com.bleelblep.glyphsharge.R.font.ntype_82_headline))
                        FontVariant.NDOT -> FontFamily(Font(com.bleelblep.glyphsharge.R.font.ndot55caps))
                        FontVariant.SYSTEM -> FontFamily.Default
                    }
                ),
                color = if (isSelected) 
                    GlyphPurple 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = GlyphPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Font Size Controls with sliders for each category
 */
@Composable
fun FontSizeControls(
    fontSizeSettings: FontSizeSettings,
    onSizeChanged: (FontCategory, Float) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Font Sizes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                TextButton(onClick = { 
                    HapticUtils.triggerLightFeedback(haptic, context)
                    onReset() 
                }) {
                    Text("Reset All")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Size controls for each category
            FontSizeSlider(
                label = "Display Text",
                description = "Large headlines and display text",
                value = fontSizeSettings.displayScale,
                onValueChange = { onSizeChanged(FontCategory.DISPLAY, it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            FontSizeSlider(
                label = "Titles",
                description = "Section titles and headings",
                value = fontSizeSettings.titleScale,
                onValueChange = { onSizeChanged(FontCategory.TITLE, it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            FontSizeSlider(
                label = "Body Text",
                description = "Main content and paragraphs",
                value = fontSizeSettings.bodyScale,
                onValueChange = { onSizeChanged(FontCategory.BODY, it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            FontSizeSlider(
                label = "Labels",
                description = "Small text and labels",
                value = fontSizeSettings.labelScale,
                onValueChange = { onSizeChanged(FontCategory.LABEL, it) }
            )
        }
    }
}

/**
 * Individual font size slider
 */
@Composable
private fun FontSizeSlider(
    label: String,
    description: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${(value * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = GlyphPurple
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = value,
            onValueChange = { newValue ->
                HapticUtils.triggerLightFeedback(haptic, context)
                onValueChange(newValue)
            },
            valueRange = 0.5f..2.0f,
            steps = 29, // 0.5 to 2.0 in 0.05 increments
            colors = SliderDefaults.colors(
                thumbColor = GlyphPurple,
                activeTrackColor = GlyphPurple,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}

/**
 * Custom Fonts Toggle Card
 */
@Composable
fun CustomFontsToggle(
    useCustomFonts: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Custom Fonts",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = if (useCustomFonts) 
                            "Using Nothing Design fonts" 
                        else 
                            "Using system default fonts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = useCustomFonts,
                    onCheckedChange = { 
                        HapticUtils.triggerLightFeedback(haptic, context)
                        onToggle() 
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GlyphPurple,
                        checkedTrackColor = GlyphPurpleContainer
                    )
                )
            }

            if (!useCustomFonts) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = GlyphPurpleContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = GlyphPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "Font family selection is disabled when using system fonts",
                            style = MaterialTheme.typography.bodySmall,
                            color = GlyphPurple
                        )
                    }
                }
            }
        }
    }
}

/**
 * Font Preview Card showing current settings
 */
@Composable
fun FontPreview(
    fontState: FontState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Preview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Display text preview
            Text(
                text = "Display Large",
                style = MaterialTheme.typography.displayLarge,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Title preview
            Text(
                text = "Headline Medium",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Body text preview
            Text(
                text = "This is body text showing how your content will look with the current font settings. It demonstrates readability and styling.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Label preview
            Text(
                text = "Label Medium • Settings Applied",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Current settings summary
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = GlyphPurpleContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Current Settings",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = GlyphPurple
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = buildString {
                            append("Font: ${fontState.getFontDescription()}")
                            if (fontState.useCustomFonts && fontState.fontSizeSettings != FontSizeSettings()) {
                                append(" • Custom sizing applied")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
} 