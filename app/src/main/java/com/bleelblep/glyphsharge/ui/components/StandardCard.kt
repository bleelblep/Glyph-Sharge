package com.bleelblep.glyphsharge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import com.bleelblep.glyphsharge.ui.utils.HapticUtils
import com.bleelblep.glyphsharge.ui.theme.LocalThemeState
import com.bleelblep.glyphsharge.ui.theme.AppThemeStyle

/**
 * Standardized card template for GlyphZen app
 * Provides consistent styling and layout patterns
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    description: String? = null,
    icon: ImageVector? = null,
    actionText: String? = null,
    onCardClick: (() -> Unit)? = null,
    onActionClick: (() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    elevation: Dp = 1.dp,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val themeState = LocalThemeState.current
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "pressScale"
    )
    
    // Enhanced colors for expressive theme
    val enhancedColors = if (themeState.themeStyle == AppThemeStyle.EXPRESSIVE) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    } else {
        colors
    }
    
    // Enhanced shape for expressive theme - use cut corners for dramatic effect
    val enhancedShape = if (themeState.themeStyle == AppThemeStyle.EXPRESSIVE) {
        CutCornerShape(
            topStartPercent = 0,
            topEndPercent = 15,
            bottomStartPercent = 15,
            bottomEndPercent = 0
        )
    } else {
        MaterialTheme.shapes.large
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(pressScale),
        onClick = {
            if (onCardClick != null) {
                try {
                    isPressed = true
                    HapticUtils.triggerLightFeedback(haptic, context)
                    onCardClick()
                    // Reset pressed state after animation
                    GlobalScope.launch {
                        delay(150)
                        isPressed = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        },
        enabled = onCardClick != null,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = enhancedShape,
        colors = enhancedColors
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header section with icon and title
            if (icon != null || title != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    title?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Subtitle
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Description
            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action button
            if (actionText != null && onActionClick != null) {
                Button(
                    onClick = {
                        try {
                            HapticUtils.triggerLightFeedback(haptic, context)
                            onActionClick()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = actionText)
                }
            }

            // Custom content
            content?.let {
                it()
            }
        }
    }
}

/**
 * Simple card variant with minimal content
 */
@Composable
fun SimpleCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    StandardCard(
        modifier = modifier,
        title = title,
        description = description,
        onCardClick = onClick,
        content = content
    )
}

/**
 * Icon card variant with prominent icon display
 */
@Composable
fun IconCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    actionText: String? = null,
    onCardClick: (() -> Unit)? = null,
    onActionClick: (() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    StandardCard(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        icon = icon,
        actionText = actionText,
        onCardClick = onCardClick,
        onActionClick = onActionClick,
        content = content
    )
}

/**
 * Action card variant with prominent action button
 */
@Composable
fun ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    actionText: String,
    onActionClick: () -> Unit,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    StandardCard(
        modifier = modifier,
        title = title,
        description = description,
        actionText = actionText,
        onActionClick = onActionClick,
        content = content
    )
}

/**
 * Content card variant for custom content layouts
 */
@Composable
fun ContentCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    StandardCard(
        modifier = modifier,
        title = title,
        contentPadding = contentPadding,
        onCardClick = onClick,
        content = content
    )
} 