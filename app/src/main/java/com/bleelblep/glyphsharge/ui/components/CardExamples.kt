package com.bleelblep.glyphsharge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import com.bleelblep.glyphsharge.ui.utils.HapticUtils

/**
 * Examples demonstrating how to use the standardized card templates
 * with MotionLayout-style animations using Compose animation APIs
 *
 * Features two modes:
 * - Standard Animations: Traditional Material Design patterns
 * - Expressive Motion: Advanced fluid motion and playful interactions
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardExamplesScreen(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(CardExampleMode.CURRENT) }
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
                        text = "Card Examples",
                        style = MaterialTheme.typography.headlineLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        HapticUtils.triggerLightFeedback(haptic, context)
                        /* Handled by parent */ 
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
    ) { innerPadding ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mode selection buttons
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModeSelectionButton(
                        text = "Standard Animations",
                        isSelected = selectedMode == CardExampleMode.CURRENT,
                        onClick = { selectedMode = CardExampleMode.CURRENT },
                        modifier = Modifier.weight(1f)
                    )
                    ModeSelectionButton(
                        text = "Expressive Motion",
                        isSelected = selectedMode == CardExampleMode.EXPRESSIVE,
                        onClick = { selectedMode = CardExampleMode.EXPRESSIVE },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Show different examples based on selected mode
            when (selectedMode) {
                CardExampleMode.CURRENT -> {
                    // Standard animation examples
                    item { StateTransitionCard() }
                    item { DragInteractionCard() }
                    item { CustomAttributeAnimationCard() }
                    item { ExpandableMotionCard() }
                    item { ProgressAnimationCard() }
                    item { MultiStateCard() }
                    item { ParallaxMotionCard() }
                    item { SpringAnimationCard() }

                    // Additional Standard Examples
                    item { FadeInCard() }
                    item { SlideAnimationCard() }
                    item { RotationCard() }
                    item { PulseAnimationCard() }
                    item { StaggeredAnimationCard() }
                    item { HeightAnimationCard() }
                    item { CircularProgressCard() }
                }

                CardExampleMode.EXPRESSIVE -> {
                    // Expressive motion examples
                    item { ExpressiveWavyCard() }
                    item { DynamicShapeCard() }
                    item { FluidMotionCard() }
                    item { MorphingButtonCard() }
                    item { ExpressiveProgressCard() }
                    item { GestureBasedCard() }
                    item { DynamicColorCard() }
                    item { ExpressiveNavigationCard() }

                    // Additional Expressive Examples
                    item { PhysicsBasedCard() }
                    item { ResponsiveShapeCard() }
                    item { PlayfulMicroInteractionCard() }
                    item { AdaptiveColorCard() }
                    item { FluidContainerCard() }
                    item { ElasticButtonCard() }
                    item { ContinuousMotionCard() }
                    item { ExpressiveTypographyCard() }
                    item { ExpressiveCircularProgressCard() }
                }
            }
        }
    }
}

enum class CardExampleMode {
    CURRENT, // Standard Material Design animations
    EXPRESSIVE // Advanced expressive motion patterns
}

@Composable
private fun ModeSelectionButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(300),
        label = "buttonBackground"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.onPrimary
        else
            MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300),
        label = "buttonContent"
    )

    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(24.dp),
        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) else null
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1
        )
    }
}

/**
 * MotionLayout-style state transition card
 * Demonstrates state-based transitions like ConstraintSets
 */
@Composable
private fun StateTransitionCard() {
    var isExpanded by remember { mutableStateOf(false) }

    // Animate between two states (like ConstraintSets)
    val animatedHeight by animateDpAsState(
        targetValue = if (isExpanded) 200.dp else 120.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "cardHeight"
    )

    val animatedPadding by animateDpAsState(
        targetValue = if (isExpanded) 24.dp else 16.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "cardPadding"
    )

    ContentCard(
        title = "State Transition Card",
        modifier = Modifier
            .fillMaxWidth()
            .height(animatedHeight),
        contentPadding = PaddingValues(animatedPadding),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column {
            Text(
                text = "Click to transition between states",
                style = MaterialTheme.typography.bodyMedium
            )

            // Content that appears when expanded
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "This demonstrates MotionLayout-style state transitions using Compose animations.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))
                LinearWavyProgressIndicator(
                    progress = 0.7f,
                    modifier = Modifier.fillMaxWidth(),
                    amplitude = 0.9f,
                    wavelength = 28.dp
                )
            }
        }
    }
}

/**
 * Touch-based drag interaction card
 * Demonstrates OnSwipe equivalent functionality
 */
@Composable
private fun DragInteractionCard() {
    var offsetX by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    // Animate the offset back to center when released
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "offsetX"
    )

    // Calculate progress based on drag distance (like MotionLayout progress)
    val dragProgress = (kotlin.math.abs(animatedOffsetX) / 200f).coerceIn(0f, 1f)

    // Animate background color based on drag progress
    val backgroundColor by animateColorAsState(
        targetValue = lerp(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primaryContainer,
            dragProgress
        ),
        label = "backgroundColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // Snap back to center (like MotionLayout constraint)
                        offsetX = 0f
                    }
                ) { _, dragAmount ->
                    offsetX += dragAmount.x
                }
            },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = (4 + dragProgress * 8).dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Drag Interaction Card",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Drag me horizontally! Progress: ${(dragProgress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Custom attribute animation card
 * Demonstrates CustomAttribute equivalent animations
 */
@Composable
private fun CustomAttributeAnimationCard() {
    var isAnimating by remember { mutableStateOf(false) }

    // Multiple custom attributes being animated
    val animatedScale by animateFloatAsState(
        targetValue = if (isAnimating) 1.1f else 1f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "scale"
    )

    val animatedRotation by animateFloatAsState(
        targetValue = if (isAnimating) 5f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "rotation"
    )

    val animatedColor by animateColorAsState(
        targetValue = if (isAnimating)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 500),
        label = "backgroundColor"
    )

    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isAnimating) 3.dp else 1.dp,
        animationSpec = tween(durationMillis = 500),
        label = "borderWidth"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                rotationZ = animatedRotation
            }
            .clickable { isAnimating = !isAnimating },
        colors = CardDefaults.cardColors(containerColor = animatedColor),
        border = BorderStroke(
            width = animatedBorderWidth,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.scale(animatedScale)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Custom Attribute Animation",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Click to animate multiple properties",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Expandable card with complex motion
 */
@Composable
private fun ExpandableMotionCard() {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expandable Motion Card",
                    style = MaterialTheme.typography.titleLarge
                )

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    modifier = Modifier.graphicsLayer {
                        rotationZ = if (isExpanded) 180f else 0f
                    }
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "This content appears with smooth motion animations.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Action 1")
                    }
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Action 2")
                    }
                }
            }
        }
    }
}

/**
 * Progress-based animation card
 */
@Composable
private fun ProgressAnimationCard() {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(50)
            progress = (progress + 0.02f) % 1f
        }
    }

    val animatedElevation by animateFloatAsState(
        targetValue = 4f + (progress * 8f),
        animationSpec = tween(100),
        label = "elevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Progress Animation Card",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Progress: ${(progress * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodyMedium
            )

            LinearWavyProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                amplitude = 0.7f,
                wavelength = 30.dp,
                waveSpeed = 20.dp
            )
        }
    }
}

/**
 * Multi-state card (like multiple ConstraintSets)
 */
@Composable
private fun MultiStateCard() {
    var currentState by remember { mutableStateOf(0) }
    val states = listOf("Compact", "Medium", "Expanded")

    val animatedHeight by animateDpAsState(
        targetValue = when (currentState) {
            0 -> 80.dp
            1 -> 120.dp
            else -> 180.dp
        },
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "height"
    )

    val animatedPadding by animateDpAsState(
        targetValue = when (currentState) {
            0 -> 8.dp
            1 -> 16.dp
            else -> 24.dp
        },
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "padding"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(animatedHeight)
            .clickable {
                currentState = (currentState + 1) % states.size
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(animatedPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Multi-State Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Current: ${states[currentState]}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (currentState >= 2) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Additional content in expanded state",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

/**
 * Parallax motion card
 */
@Composable
private fun ParallaxMotionCard() {
    var offsetY by remember { mutableStateOf(0f) }

    // Animate the offset for smooth reset behavior
    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "parallaxOffset"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // Reset to center position when drag ends
                        offsetY = 0f
                    }
                ) { _, dragAmount ->
                    offsetY = (offsetY + dragAmount.y * 0.5f).coerceIn(-50f, 50f)
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background layer with parallax effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                    .offset { IntOffset(0, (animatedOffsetY * 0.3f).roundToInt()) }
            )

            // Foreground content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .offset { IntOffset(0, animatedOffsetY.roundToInt()) },
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Parallax Motion Card",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Drag vertically - releases back to center",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Spring-based animation card
 */
@Composable
private fun SpringAnimationCard() {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isPressed = true },
                    onDragEnd = { isPressed = false }
                ) { _, _ -> }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Remove default ripple since we have custom animation
            ) {
                // Toggle pressed state for a brief moment on click
                isPressed = true
                // Reset after a short delay to create spring effect
                GlobalScope.launch {
                    kotlinx.coroutines.delay(150)
                    isPressed = false
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Spring Animation Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Click or drag for spring effect",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Expressive Motion Examples
 * Showcasing dynamic shapes, fluid motion, and enhanced interactions
 */

/**
 * Expressive wavy card with dynamic shape animation
 */
@Composable
private fun ExpressiveWavyCard() {
    var isWaving by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "wavyTransition")

    val waveProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { isWaving = !isWaving },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Expressive Wavy Card",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = if (isWaving) "Wavy progress active!" else "Tap to toggle wavy motion",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isWaving) {
                LinearWavyProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    amplitude = 1.2f,
                    wavelength = 25.dp,
                    waveSpeed = 30.dp,
                    strokeWidth = 6.dp
                )
            } else {
                LinearWavyProgressIndicator(
                    progress = 0.6f,
                    modifier = Modifier.fillMaxWidth(),
                    amplitude = 0.5f,
                    wavelength = 40.dp,
                    strokeWidth = 4.dp
                )
            }
        }
    }
}

/**
 * Dynamic shape card with morphing borders
 */
@Composable
private fun DynamicShapeCard() {
    var isMorphed by remember { mutableStateOf(false) }

    val cornerRadius by animateDpAsState(
        targetValue = if (isMorphed) 32.dp else 12.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cornerRadius"
    )

    val elevation by animateDpAsState(
        targetValue = if (isMorphed) 16.dp else 4.dp,
        animationSpec = tween(500),
        label = "elevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { isMorphed = !isMorphed },
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.scale(if (isMorphed) 1.3f else 1f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Dynamic Shape Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Tap to morph the shape",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Fluid motion card with gesture-based continuous animation
 */
@Composable
private fun FluidMotionCard() {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isPressed by remember { mutableStateOf(false) }

    val animatedOffset by animateOffsetAsState(
        targetValue = if (isPressed) offset else Offset.Zero,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fluidOffset"
    )

    // Calculate dynamic background color based on motion with enhanced visual feedback
    val baseColor = MaterialTheme.colorScheme.primaryContainer
    val motionIntensity = (kotlin.math.abs(animatedOffset.x) + kotlin.math.abs(animatedOffset.y)) / 80f
    val dynamicAlpha = 0.4f + motionIntensity
    val dynamicBackgroundColor = baseColor.copy(alpha = dynamicAlpha.coerceIn(0.4f, 0.9f))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clipToBounds()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { _: Offset -> isPressed = true },
                    onDragEnd = { isPressed = false }
                ) { _: PointerInputChange, dragAmount: Offset ->
                    offset += dragAmount * 0.3f
                    offset = Offset(
                        x = offset.x.coerceIn(-50f, 50f),
                        y = offset.y.coerceIn(-30f, 30f)
                    )
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = dynamicBackgroundColor
        )
    ) {
        // Single Box that fills the entire card and moves with the animation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dynamicBackgroundColor)
                .offset {
                    IntOffset(
                        animatedOffset.x.roundToInt(),
                        animatedOffset.y.roundToInt()
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Fluid Motion Card",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Drag for continuous fluid motion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Morphing button card with shape transformations
 */
@Composable
private fun MorphingButtonCard() {
    var buttonState by remember { mutableStateOf(ButtonMorphState.ROUNDED) }

    val buttonWidth by animateDpAsState(
        targetValue = when (buttonState) {
            ButtonMorphState.ROUNDED -> 140.dp
            ButtonMorphState.WIDE -> 200.dp
            ButtonMorphState.CIRCULAR -> 56.dp
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonWidth"
    )

    val buttonHeight by animateDpAsState(
        targetValue = when (buttonState) {
            ButtonMorphState.CIRCULAR -> 56.dp
            else -> 40.dp
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonHeight"
    )

    val cornerRadius by animateDpAsState(
        targetValue = when (buttonState) {
            ButtonMorphState.ROUNDED -> 20.dp
            ButtonMorphState.WIDE -> 8.dp
            ButtonMorphState.CIRCULAR -> 28.dp
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cornerRadius"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Morphing Button Card",
                style = MaterialTheme.typography.titleLarge
            )

            Button(
                onClick = {
                    buttonState = when (buttonState) {
                        ButtonMorphState.ROUNDED -> ButtonMorphState.WIDE
                        ButtonMorphState.WIDE -> ButtonMorphState.CIRCULAR
                        ButtonMorphState.CIRCULAR -> ButtonMorphState.ROUNDED
                    }
                },
                modifier = Modifier
                    .width(buttonWidth)
                    .height(buttonHeight),
                shape = RoundedCornerShape(cornerRadius)
            ) {
                when (buttonState) {
                    ButtonMorphState.CIRCULAR -> {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add"
                        )
                    }

                    else -> {
                        Text(
                            text = when (buttonState) {
                                ButtonMorphState.ROUNDED -> "Tap Me"
                                ButtonMorphState.WIDE -> "Morphing Button"
                                else -> ""
                            },
                            maxLines = 1
                        )
                    }
                }
            }

            Text(
                text = "Button morphs between shapes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private enum class ButtonMorphState {
    ROUNDED, WIDE, CIRCULAR
}

/**
 * Expressive progress card with multiple wavy indicators
 */
@Composable
private fun ExpressiveProgressCard() {
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(50)
            progress = (progress + 0.015f) % 1f
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Expressive Progress Card",
                style = MaterialTheme.typography.titleLarge
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Different wavy progress styles
                LinearWavyProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                    amplitude = 0.6f,
                    wavelength = 35.dp,
                    strokeWidth = 3.dp
                )

                LinearWavyProgressIndicator(
                    progress = progress * 0.8f,
                    modifier = Modifier.fillMaxWidth(),
                    amplitude = 1.0f,
                    wavelength = 20.dp,
                    strokeWidth = 4.dp
                )

                LinearWavyProgressIndicator(
                    progress = progress * 1.2f % 1f,
                    modifier = Modifier.fillMaxWidth(),
                    amplitude = 0.4f,
                    wavelength = 50.dp,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

/**
 * Gesture-based card with multi-touch interactions
 */
@Composable
private fun GestureBasedCard() {
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "gestureScale"
    )

    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "gestureRotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
                rotationZ = animatedRotation
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        scale = 1f
                        rotation = 0f
                    }
                ) { _, dragAmount ->
                    // Scale based on vertical drag
                    scale = (1f + dragAmount.y / 500f).coerceIn(0.8f, 1.4f)
                    // Rotation based on horizontal drag
                    rotation = (dragAmount.x / 10f).coerceIn(-15f, 15f)
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Gesture-Based Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Drag to scale and rotate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Dynamic color card with expressive color transitions
 */
@Composable
private fun DynamicColorCard() {
    var colorIndex by remember { mutableStateOf(0) }

    val colors = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    )

    val animatedColor by animateColorAsState(
        targetValue = colors[colorIndex],
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "dynamicColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable {
                colorIndex = (colorIndex + 1) % colors.size
            },
        colors = CardDefaults.cardColors(containerColor = animatedColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Dynamic Color Card",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap to cycle through intensities",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Expressive navigation card with smooth transitions
 */
@Composable
private fun ExpressiveNavigationCard() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Search", "Profile")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Expressive Navigation",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.1f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "tabScale"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { selectedTab = index }
                            .scale(scale)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = when (index) {
                                0 -> Icons.Default.Home
                                1 -> Icons.Default.Search
                                else -> Icons.Default.Person
                            },
                            contentDescription = tab,
                            tint = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = tab,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// Helper function for offset animation
@Composable
private fun animateOffsetAsState(
    targetValue: Offset,
    animationSpec: AnimationSpec<Offset> = spring(),
    label: String = "OffsetAnimation"
): State<Offset> {
    return animateValueAsState(
        targetValue = targetValue,
        typeConverter = Offset.VectorConverter,
        animationSpec = animationSpec,
        label = label
    )
}

/**
 * Additional Standard Animation Examples
 * Demonstrating traditional Material Design animation patterns
 */

/**
 * Fade in animation card
 */
@Composable
private fun FadeInCard() {
    var isVisible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.3f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "fadeAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .alpha(alpha)
            .clickable { isVisible = !isVisible },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Fade Animation Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Tap to toggle fade effect",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Slide animation card
 */
@Composable
private fun SlideAnimationCard() {
    var isSlid by remember { mutableStateOf(false) }

    val slideOffset by animateDpAsState(
        targetValue = if (isSlid) 20.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "slideOffset"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .offset(x = slideOffset)
            .clickable { isSlid = !isSlid },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Slide Animation Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Tap to slide horizontally",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Rotation animation card
 */
@Composable
private fun RotationCard() {
    var rotationAngle by remember { mutableStateOf(0f) }

    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable {
                rotationAngle = if (rotationAngle == 0f) 180f else 0f
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.graphicsLayer { rotationZ = animatedRotation }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rotation Animation Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Tap to rotate icon",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Pulse animation card
 */
@Composable
private fun PulseAnimationCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .scale(pulseScale),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pulse Animation Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Continuous pulse effect",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Staggered animation card
 */
@Composable
private fun StaggeredAnimationCard() {
    var isAnimated by remember { mutableStateOf(false) }

    val icons = listOf(Icons.Default.Star, Icons.Default.Add, Icons.Default.Delete)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { isAnimated = !isAnimated },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Staggered Animation Card",
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                icons.forEachIndexed { index, icon ->
                    val scale by animateFloatAsState(
                        targetValue = if (isAnimated) 1.3f else 1f,
                        animationSpec = tween(
                            durationMillis = 300,
                            delayMillis = index * 100,
                            easing = FastOutSlowInEasing
                        ),
                        label = "staggeredScale$index"
                    )

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.scale(scale)
                    )
                }
            }

            Text(
                text = "Tap for staggered animation",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Height animation card
 */
@Composable
private fun HeightAnimationCard() {
    var isExpanded by remember { mutableStateOf(false) }

    val animatedHeight by animateDpAsState(
        targetValue = if (isExpanded) 160.dp else 100.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "height"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(animatedHeight)
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = if (isExpanded) Arrangement.SpaceBetween else Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Height Animation Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (isExpanded) {
                Text(
                    text = "This content appears when the card expands with smooth height animation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Circular progress card
 */
@Composable
private fun CircularProgressCard() {
    var progress by remember { mutableStateOf(0f) }
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(50)
            progress = (progress + 0.01f) % 1f
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Circular Progress Card",
                    style = MaterialTheme.typography.titleMedium
                )
                
                // Single circular progress with wavy line
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Standard Material 3 CircularProgressIndicator
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(120.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 6.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    
                    // Center content
                    Text(
                        text = if (isPressed) "✨" else "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Text(
                text = "Tap for expressive feedback",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Expressive circular progress card with wavy progress line
 */
@Composable
private fun ExpressiveCircularProgressCard() {
    var progress by remember { mutableStateOf(0f) }
    var isPressed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(50)
            progress = (progress + 0.015f) % 1f
        }
    }

    // Scale animation on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { 
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Expressive Circular Progress",
                style = MaterialTheme.typography.titleMedium
            )
            
            // Single circular progress with wavy line
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Standard Material 3 CircularProgressIndicator
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(120.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 6.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                
                // Center content
                Text(
                    text = if (isPressed) "✨" else "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Text(
                text = "Tap for expressive feedback",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Additional Material 3 Expressive Examples
 * Showcasing advanced fluid motion, physics-based animations, and playful interactions
 */

/**
 * Physics-based card with realistic motion
 */
@Composable
private fun PhysicsBasedCard() {
    var velocity by remember { mutableStateOf(0f) }
    var position by remember { mutableStateOf(0f) }

    val animatedPosition by animateFloatAsState(
        targetValue = position,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "physicsPosition"
    )

    // Calculate dynamic background color based on motion
    val baseColor = MaterialTheme.colorScheme.primaryContainer
    val dynamicAlpha = 0.2f + kotlin.math.abs(animatedPosition) / 100f
    val dynamicBackgroundColor = baseColor.copy(alpha = dynamicAlpha.coerceIn(0.2f, 0.7f))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clipToBounds()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        position = 0f
                    }
                ) { _: PointerInputChange, dragAmount: Offset ->
                    velocity = dragAmount.y
                    position = (position + velocity * 0.1f).coerceIn(-30f, 30f)
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = dynamicBackgroundColor
        )
    ) {
        // Single Box that fills the entire card and moves with the animation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(dynamicBackgroundColor)
                .offset { IntOffset(0, animatedPosition.roundToInt()) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Physics-Based Card",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Drag vertically for realistic physics motion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Responsive shape card that adapts to interactions
 */
@Composable
private fun ResponsiveShapeCard() {
    var pressProgress by remember { mutableStateOf(0f) }

    val cornerRadius by animateDpAsState(
        targetValue = (12 + pressProgress * 20).dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "responsiveCorner"
    )

    val elevation by animateDpAsState(
        targetValue = (4 + pressProgress * 12).dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "responsiveElevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { _: Offset -> pressProgress = 1f },
                    onDragEnd = { pressProgress = 0f }
                ) { _: PointerInputChange, _: Offset -> }
            }
            .clickable {
                pressProgress = if (pressProgress == 0f) 1f else 0f
            },
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.scale(1f + pressProgress * 0.2f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Responsive Shape Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Press or drag to reshape",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Playful micro-interaction card
 */
@Composable
private fun PlayfulMicroInteractionCard() {
    var bounceState by remember { mutableStateOf(false) }

    val bounceScale by animateFloatAsState(
        targetValue = if (bounceState) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        finishedListener = { if (bounceState) bounceState = false },
        label = "bounceScale"
    )

    val wiggleRotation by animateFloatAsState(
        targetValue = if (bounceState) 2f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "wiggleRotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .scale(bounceScale)
            .graphicsLayer { rotationZ = wiggleRotation }
            .clickable { bounceState = true },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Playful Micro-Interaction",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Tap for playful bounce!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Adaptive color card with context-aware theming
 */
@Composable
private fun AdaptiveColorCard() {
    var colorIntensity by remember { mutableStateOf(0f) }

    val adaptiveColor by animateColorAsState(
        targetValue = lerp(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primaryContainer,
            colorIntensity
        ),
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
        label = "adaptiveColor"
    )

    val textColor by animateColorAsState(
        targetValue = lerp(
            MaterialTheme.colorScheme.onSurface,
            MaterialTheme.colorScheme.onPrimaryContainer,
            colorIntensity
        ),
        label = "adaptiveTextColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    colorIntensity = (colorIntensity + dragAmount.x / 500f).coerceIn(0f, 1f)
                }
            },
        colors = CardDefaults.cardColors(containerColor = adaptiveColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Adaptive Color Card",
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor
                )
                Text(
                    text = "Drag horizontally to adapt colors",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Fluid container card with liquid-like motion
 */
@Composable
private fun FluidContainerCard() {
    var flowState by remember { mutableStateOf(false) }

    val flowProgress by animateFloatAsState(
        targetValue = if (flowState) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        finishedListener = { if (flowState) flowState = false },
        label = "flowProgress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { flowState = true },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fluid background effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((140 * flowProgress).dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(
                            alpha = 0.3f + flowProgress * 0.4f
                        )
                    )
                    .align(Alignment.BottomStart)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Fluid Container Card",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Tap to watch fluid motion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Elastic button card with advanced spring physics
 */
@Composable
private fun ElasticButtonCard() {
    var isPressed by remember { mutableStateOf(false) }

    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "elasticScale"
    )

    val buttonWidth by animateDpAsState(
        targetValue = if (isPressed) 180.dp else 150.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "elasticWidth"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Elastic Button Card",
                style = MaterialTheme.typography.titleLarge
            )

            Button(
                onClick = { isPressed = !isPressed },
                modifier = Modifier
                    .width(buttonWidth)
                    .height(48.dp)
                    .scale(buttonScale),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Elastic Button")
            }

            Text(
                text = "Button with elastic spring physics",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Continuous motion card with seamless loops
 */
@Composable
private fun ContinuousMotionCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "continuous")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "continuousRotation"
    )

    val wave by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "continuousWave"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Rotating background element
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer { rotationZ = rotation }
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        CircleShape
                    )
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.scale(1f + wave * 0.2f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Continuous Motion Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Seamless infinite animations",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Expressive typography card with dynamic text effects
 */
@Composable
private fun ExpressiveTypographyCard() {
    var textState by remember { mutableStateOf(TypographyState.NORMAL) }

    val textScale by animateFloatAsState(
        targetValue = when (textState) {
            TypographyState.NORMAL -> 1f
            TypographyState.LARGE -> 1.2f
            TypographyState.EMPHASIZED -> 1.1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "textScale"
    )

    val letterSpacing by animateFloatAsState(
        targetValue = when (textState) {
            TypographyState.NORMAL -> 0f
            TypographyState.LARGE -> 2f
            TypographyState.EMPHASIZED -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "letterSpacing"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable {
                textState = when (textState) {
                    TypographyState.NORMAL -> TypographyState.LARGE
                    TypographyState.LARGE -> TypographyState.EMPHASIZED
                    TypographyState.EMPHASIZED -> TypographyState.NORMAL
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "EXPRESSIVE",
                style = MaterialTheme.typography.headlineSmall.copy(
                    letterSpacing = letterSpacing.sp
                ),
                modifier = Modifier.scale(textScale),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap to cycle typography styles",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private enum class TypographyState {
    NORMAL, LARGE, EMPHASIZED
}