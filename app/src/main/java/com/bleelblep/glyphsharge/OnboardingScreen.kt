package com.bleelblep.glyphsharge

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.bleelblep.glyphsharge.ui.theme.SettingsRepository
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import kotlin.math.absoluteValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.offset
import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Path
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.clipRect
import com.bleelblep.glyphsharge.ui.components.rememberEmojiPainter
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke

/**
 * Fresh onboarding experience inspired by Samad Talukder's Compose onboarding sample.
 * - Supports Lottie & image slides.
 * - Uses app theme (fonts/colours) so it blends with the rest of the UI.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }

    // 1️⃣ Define your pages (replace URLs/images as desired)
    val pages = remember {
        listOf(
            OnboardingPage.Feature(
                title = "Welcome to Glyph Sharge",
                description = "Super-charge your Nothing phone Glyphs.",
                imageRes = R.drawable.phone1,
                demo = DemoKind.GLYPH_SERVICE
            ),
            OnboardingPage.Feature(
                title = "Power Peek",
                description = "Shake with the screen off to preview battery on the Glyph LEDs.",
                imageRes = R.drawable.phone1,
                demo = DemoKind.POWER_PEEK
            ),
            OnboardingPage.Feature(
                title = "Glyph Guard",
                description = "Instant LED + sound alert if someone unplugs your charger.",
                imageRes = R.drawable.phone1,
                demo = DemoKind.GLYPH_GUARD
            ),
            OnboardingPage.Feature(
                title = "Battery Story",
                description = "Track charge speed, temperature & battery health in real time.",
                demo = DemoKind.BATTERY_STORY
            )
        )
    }

    // Pager state + coroutine scope
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Skip button ----------------------------------------------------
            TextButton(
                onClick = {
                    settingsRepository.setOnboardingComplete(true)
                    onFinish()
                },
                modifier = Modifier.align(Alignment.End)
            ) { Text("Skip") }

            // Pager ---------------------------------------------------------
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIdx ->
                val pageOffset = (
                    (pagerState.currentPage - pageIdx) + pagerState.currentPageOffsetFraction
                ).absoluteValue
                OnboardingPageContent(pages[pageIdx], pageOffset)
            }

            // Dots indicator ------------------------------------------------
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                pages.indices.forEach { idx ->
                    val selected = pagerState.currentPage == idx
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (selected) 12.dp else 8.dp)
                            .background(
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                    )
                }
            }

            // Next / Done button -------------------------------------------
            Button(
                onClick = {
                    if (pagerState.currentPage == pages.lastIndex) {
                        settingsRepository.setOnboardingComplete(true)
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(if (pagerState.currentPage == pages.lastIndex) "Get Started" else "Next")
            }
        }
    }
}

// -------------------------------------------------------------------------
//  Internal helpers
// -------------------------------------------------------------------------
sealed class OnboardingPage {
    abstract val title: String
    abstract val description: String

    data class Feature(
        override val title: String,
        override val description: String,
        val lottieUrl: String? = null,
        val imageRes: Int? = null,
        val demo: DemoKind
    ) : OnboardingPage()

    data class Image(
        override val title: String,
        override val description: String,
        val imageRes: Int
    ) : OnboardingPage()
}

enum class DemoKind { NONE, GLYPH_SERVICE, POWER_PEEK, GLYPH_GUARD, BATTERY_STORY }

@Composable
private fun OnboardingPageContent(page: OnboardingPage, pageOffset: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // ---------- Visual area (adaptive height) ----------
        val visualHeight = when {
            page is OnboardingPage.Feature && page.demo in listOf(DemoKind.POWER_PEEK, DemoKind.GLYPH_GUARD) -> 360.dp
            page is OnboardingPage.Feature && page.demo == DemoKind.BATTERY_STORY -> 330.dp // Only Battery Story reduced by 25%
            else -> 440.dp
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(visualHeight),
            contentAlignment = Alignment.Center
        ) {
            when (page) {
                is OnboardingPage.Feature -> {
                    if (page.imageRes != null) {
                        // Visual effects depending on slide type
                        val rotation = if (page.demo == DemoKind.POWER_PEEK && pageOffset < 0.01f) {
                            val infinite = rememberInfiniteTransition()
                            val rot by infinite.animateFloat(
                                initialValue = -6f,
                                targetValue = 6f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )
                            rot
                        } else 0f

                        // 🔄 Swap between light & dark phone mock-ups for Power Peek and Glyph Guard pages
                        var darkMode by remember { mutableStateOf(false) }
                        if ((page.demo == DemoKind.POWER_PEEK || page.demo == DemoKind.GLYPH_GUARD) && pageOffset < 0.01f) {
                            LaunchedEffect(Unit) {
                                while (true) {
                                    darkMode = !darkMode
                                    kotlinx.coroutines.delay(700)
                                }
                            }
                        }

                        val imageResToShow = if (page.demo == DemoKind.GLYPH_GUARD || page.demo == DemoKind.POWER_PEEK) {
                            if (darkMode) R.drawable.phone1dark else R.drawable.phone1
                        } else page.imageRes

                        val imgSize = visualHeight

                        Image(
                            painter = painterResource(id = imageResToShow!!),
                            contentDescription = null,
                            modifier = Modifier
                                .size(imgSize)
                                .graphicsLayer {
                                    rotationZ = rotation
                                }
                        )

                        // Battery Story uses custom graphic
                        if (page.demo == DemoKind.BATTERY_STORY) {
                            BatteryStoryImage(
                                modifier = Modifier.size(visualHeight)
                            )
                        } else if (!page.lottieUrl.isNullOrEmpty()) {
                            val comp by rememberLottieComposition(LottieCompositionSpec.Url(page.lottieUrl))
                            LottieAnimation(
                                composition = comp,
                                iterations = Int.MAX_VALUE,
                                modifier = Modifier.size(
                                    width = visualHeight / 2,
                                    height = visualHeight
                                )
                            )
                        }
                    }
                    // 🚫 When no static image is provided (e.g. Battery Story), fall back to a custom visual or Lottie
                    else {
                        when {
                            page.demo == DemoKind.BATTERY_STORY -> {
                                BatteryStoryImage(
                                    modifier = Modifier.size(visualHeight)
                                )
                            }
                            !page.lottieUrl.isNullOrEmpty() -> {
                                val comp by rememberLottieComposition(LottieCompositionSpec.Url(page.lottieUrl))
                                LottieAnimation(
                                    composition = comp,
                                    iterations = Int.MAX_VALUE,
                                    modifier = Modifier.size(
                                        width = visualHeight / 2,
                                        height = visualHeight
                                    )
                                )
                            }
                        }
                    }
                }
                is OnboardingPage.Image -> {
                    Image(
                        painter = painterResource(id = page.imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(visualHeight)
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        
        // Extra spacing for Battery Story to move content down by 10%
        if (page is OnboardingPage.Feature && page.demo == DemoKind.BATTERY_STORY) {
            Spacer(Modifier.height(32.dp))
        }

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )

        // Show demo card matching each feature page so users see the real UI element
        if (page is OnboardingPage.Feature) {
            when (page.demo) {
                DemoKind.GLYPH_SERVICE -> DemoGlyphServiceCard()
                DemoKind.POWER_PEEK -> DemoPowerPeekCard(modifier = Modifier.width(visualHeight / 2))
                DemoKind.GLYPH_GUARD -> DemoGlyphGuardCard(modifier = Modifier.width(visualHeight / 2))
                DemoKind.BATTERY_STORY -> DemoBatteryStoryCard(modifier = Modifier.width(180.dp))
                else -> {}
            }
        }
    }
}

// Mini demo composables ------------------------------------------------------
@Composable
private fun DemoGlyphServiceCard() {
    var enabled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while(true) {
            enabled = !enabled
            kotlinx.coroutines.delay(1200)
        }
    }
    com.bleelblep.glyphsharge.ui.components.GlyphControlCard(
        enabled = enabled,
        onEnabledChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    )
}

@Composable
private fun DemoPowerPeekCard(modifier: Modifier = Modifier) {
    val enabled = true
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    com.bleelblep.glyphsharge.ui.components.PowerPeekCard(
        title = "Power Peek",
        description = "Peek battery",
        icon = painterResource(id = R.drawable._44),
        onTestPowerPeek = {}, onEnablePowerPeek = {}, onDisablePowerPeek = {},
        modifier = modifier
            .padding(top = 20.dp),
        iconSize = 32,
        isServiceActive = enabled,
        settingsRepository = settingsRepository
    )
}

@Composable
private fun DemoGlyphGuardCard(modifier: Modifier = Modifier) {
    val enabled = true
    val context = LocalContext.current
    val settingsRepository = remember { SettingsRepository(context) }
    com.bleelblep.glyphsharge.ui.components.GlyphGuardCard(
        title = "Glyph Guard",
        description = "USB alert",
        icon = painterResource(id = R.drawable._78),
        onTest = {}, onStart = {}, onStop = {},
        modifier = modifier
            .padding(top = 20.dp),
        isServiceActive = enabled,
        settingsRepository = settingsRepository,
        glyphGuardMode = com.bleelblep.glyphsharge.ui.components.GlyphGuardMode.Standard
    )
}

@Composable
private fun DemoBatteryStoryCard(modifier: Modifier = Modifier) {
    var showDialog by remember { mutableStateOf(false) }
    
    // Single example charging session
    com.bleelblep.glyphsharge.ui.components.SquareFeatureCard(
        title = "⬆️ 32%",
        description = "9:15 AM - 10:30 AM • 1h 15m",
        icon = rememberEmojiPainter("🟢", fontSizeDp = 36f),
        iconTint = Color.Unspecified,
        onClick = { showDialog = true },
        modifier = modifier
            .padding(top = 20.dp),
        iconSize = 40,
        isServiceActive = true,
        skipConfirmation = true
    )

    if (showDialog) {
        ExampleSessionDialog(onDismiss = { showDialog = false })
    }
}

@Composable
private fun ExampleSessionDialog(onDismiss: () -> Unit) {
    var expandLegend by remember { mutableStateOf(false) }
    
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
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailRow("🕐 Start", "Sep 26 2024, 9:15 AM")
                    DetailRow("⏰ End", "Sep 26 2024, 10:30 AM")
                    DetailRow("⚡ Charge Gain", "32%")
                    DetailRow("🌡️ Avg Temp", "28°C")
                    DetailRow("⏳ Duration", "1h 15m")
                    DetailRow("👍 Health", "Excellent 🟢 89")

                    // Expandable Legend
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
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Close", fontWeight = FontWeight.Medium) }
        },
        dismissButton = {}
    )
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

// Battery fill composable ---------------------------------------------------
@Composable
private fun BatteryFillGraphic(modifier: Modifier = Modifier) {
    val fillAnim = rememberInfiniteTransition()
    val progress by fillAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Capture theme colours upfront – we cannot access @Composable values inside the Canvas draw lambda
    val outlineColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier) {
        val bodyWidth = size.width * 0.7f
        val bodyHeight = size.height * 0.6f
        val bodyLeft = (size.width - bodyWidth) / 2
        val bodyTop = (size.height - bodyHeight) / 2 + 20f

        val notchWidth = bodyWidth * 0.25f
        val notchHeight = bodyHeight * 0.08f

        val outlinePath = Path().apply {
            addRoundRect(
                RoundRect(
                    Rect(bodyLeft, bodyTop, bodyLeft + bodyWidth, bodyTop + bodyHeight),
                    CornerRadius(24f, 24f)
                )
            )
            addRect(
                Rect(
                    bodyLeft + (bodyWidth - notchWidth) / 2,
                    bodyTop - notchHeight,
                    bodyLeft + (bodyWidth + notchWidth) / 2,
                    bodyTop
                )
            )
        }

        drawPath(outlinePath, color = outlineColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))

        val innerPath = Path().apply {
            addRoundRect(
                RoundRect(
                    Rect(bodyLeft + 4f, bodyTop + 4f, bodyLeft + bodyWidth - 4f, bodyTop + bodyHeight - 4f),
                    CornerRadius(16f, 16f)
                )
            )
        }

        clipPath(innerPath) {
            val fillHeight = (bodyHeight - 8f) * progress
            drawRect(
                Color(0xFF00FF88),
                topLeft = androidx.compose.ui.geometry.Offset(bodyLeft + 4f, bodyTop + bodyHeight - 4f - fillHeight),
                size = androidx.compose.ui.geometry.Size(bodyWidth - 8f, fillHeight)
            )
        }
    }
}

// -------------------------------------------------------------------------
// Battery Story static image
// -------------------------------------------------------------------------

@Composable
private fun BatteryStoryImage(modifier: Modifier = Modifier) {
    // Static image display - no animation
    Image(
        painter = painterResource(id = R.drawable.batterystory),
        contentDescription = "Battery Story visualization",
        modifier = modifier.fillMaxSize()
    )
} 