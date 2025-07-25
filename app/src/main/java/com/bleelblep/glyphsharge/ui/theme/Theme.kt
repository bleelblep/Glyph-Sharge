package com.bleelblep.glyphsharge.ui.theme

// Android imports
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import javax.inject.Inject
import javax.inject.Singleton
import androidx.compose.material3.Shapes as M3Shapes
import androidx.compose.material3.Typography as Material3Typography

/**
 * Available theme styles for the app
 */
enum class AppThemeStyle {
    CLASSIC,    // Clean, standard Material 3 theme
    Y2K,        // Chrome, cyber, futuristic aesthetic
    NEON,       // High contrast electric colors
    AMOLED,     // True black with minimal design
    PASTEL,     // Soft, dreamy colors
    EXPRESSIVE  // Vibrant, bold Material 3 expressive
}

/**
 * Theme state management for the app
 * Handles dark/light theme switching and theme style selection
 */
@Singleton
class ThemeState @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    private var _isDarkTheme by mutableStateOf(settingsRepository.getTheme())
    val isDarkTheme: Boolean get() = _isDarkTheme
    
    private var _themeStyle by mutableStateOf(settingsRepository.getThemeStyle())
    val themeStyle: AppThemeStyle get() = _themeStyle

    fun toggleTheme() {
        _isDarkTheme = !_isDarkTheme
        settingsRepository.saveTheme(_isDarkTheme)
    }
    
    fun setDarkTheme(darkMode: Boolean) {
        _isDarkTheme = darkMode
        settingsRepository.saveTheme(_isDarkTheme)
    }
    
    fun setThemeStyle(style: AppThemeStyle) {
        _themeStyle = style
        settingsRepository.saveThemeStyle(style)
    }
}

// ============================================================================
// CLASSIC THEME - Clean Material 3 Design
// ============================================================================

private val ClassicDarkColorScheme = darkColorScheme(
    primary = Color(0xFFB8C6DB), // Metallic silver-blue for better visibility
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF4A3D77), // Darker purple for containers
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFE3DCEC), // Light purple for accents
    onSecondary = Color.Black,
    background = Color(0xFF121212), // Material 3 dark background
    surface = Color(0xFF1E1E1E), // Slightly lighter than background
    onSurface = Color(0xFFE0E0E0), // Softer white for text
    surfaceVariant = Color(0xFF2D2D2D), // For elevated surfaces
    onSurfaceVariant = Color(0xFFB0B0B0), // Secondary text
    outline = Color(0xFF938F99),
    inverseOnSurface = Color(0xFF313033),
    inverseSurface = Color(0xFFE6E1E5),
    error = Color(0xFFFF5252), // Brighter red for better visibility
    onError = Color.White,
    errorContainer = Color(0xFF8B0000), // Darker red for containers
    onErrorContainer = Color(0xFFFFDAD6),
    surfaceTint = Color(0xFF1E1E1E)
)

private val ClassicLightColorScheme = lightColorScheme(
    primary = Color(0xFF0066CC), // Chrome blue for better visibility
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    background = Color(0xFFD9D0E0), // Original light background
    surface = Color.White,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color.White,
    onSurfaceVariant = Color(0xFF49454F),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,
    outline = Color(0xFF79747E),
    inverseOnSurface = Color.White,
    inverseSurface = Color(0xFF313033),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    surfaceTint = Color.White
)

// ============================================================================
// Y2K THEME - Chrome, Cyber, Futuristic
// ============================================================================

private val Y2KDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00D4FF),
    onPrimary = Color(0xFF000F1A),
    primaryContainer = Color(0xFF0077B5),
    onPrimaryContainer = Color(0xFFBBEBFF),
    
    secondary = Color(0xFFFF0099),
    onSecondary = Color(0xFF1A0014),
    secondaryContainer = Color(0xFFCC0077),
    onSecondaryContainer = Color(0xFFFFB3E0),
    
    tertiary = Color(0xFF00FF41),
    onTertiary = Color(0xFF001A0A),
    tertiaryContainer = Color(0xFF00CC33),
    onTertiaryContainer = Color(0xFFB3FFD1),
    
    background = Color(0xFF000B14),
    onBackground = Color(0xFF00D4FF),
    surface = Color(0xFF001629),
    onSurface = Color(0xFF00D4FF),
    surfaceVariant = Color(0xFF1A2B3D),
    onSurfaceVariant = Color(0xFF66B3E0),
    
    outline = Color(0xFF0099CC),
    outlineVariant = Color(0xFF003D5C),
    scrim = Color(0xFF000000),
    
    error = Color(0xFFFF0066),
    onError = Color(0xFF1A0014),
    errorContainer = Color(0xFFCC0052),
    onErrorContainer = Color(0xFFFFB3D1),
    
    surfaceTint = Color(0xFF00D4FF)
)

private val Y2KLightColorScheme = lightColorScheme(
    primary = Color(0xFF0099CC),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE6F7FF),
    onPrimaryContainer = Color(0xFF001A2E),
    
    secondary = Color(0xFFCC0077),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE6F2),
    onSecondaryContainer = Color(0xFF1A0014),
    
    tertiary = Color(0xFF00B333),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE6FFE6),
    onTertiaryContainer = Color(0xFF001A0A),
    
    background = Color(0xFFF0FBFF),
    onBackground = Color(0xFF001A2E),
    surface = Color(0xFFF0FBFF),
    onSurface = Color(0xFF001A2E),
    surfaceVariant = Color(0xFFE6F3FF),
    onSurfaceVariant = Color(0xFF004D70),
    
    outline = Color(0xFF0099CC),
    outlineVariant = Color(0xFFB3E0FF),
    scrim = Color(0xFF000000),
    
    error = Color(0xFFCC0052),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFE6F2),
    onErrorContainer = Color(0xFF1A0014),
    
    surfaceTint = Color(0xFF0099CC)
)

// ============================================================================
// NEON THEME - High Contrast Electric
// ============================================================================

private val NeonDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00FF00),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF00B300),
    onPrimaryContainer = Color(0xFF80FF80),
    
    secondary = Color(0xFFFF0080),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFFB30060),
    onSecondaryContainer = Color(0xFFFF80C0),
    
    tertiary = Color(0xFF00FFFF),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF00B3B3),
    onTertiaryContainer = Color(0xFF80FFFF),
    
    background = Color(0xFF000000),
    onBackground = Color(0xFF00FF00),
    surface = Color(0xFF0D0D0D),
    onSurface = Color(0xFF00FF00),
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF80FF80),
    
    outline = Color(0xFF00FF00),
    outlineVariant = Color(0xFF004D00),
    scrim = Color(0xFF000000),
    
    error = Color(0xFFFF0040),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFFB30030),
    onErrorContainer = Color(0xFFFF8099),
    
    surfaceTint = Color(0xFF00FF00)
)

private val NeonLightColorScheme = lightColorScheme(
    primary = Color(0xFF00CC00),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE6FFE6),
    onPrimaryContainer = Color(0xFF003300),
    
    secondary = Color(0xFFCC0066),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE6F2),
    onSecondaryContainer = Color(0xFF330019),
    
    tertiary = Color(0xFF00CCCC),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE6FFFF),
    onTertiaryContainer = Color(0xFF003333),
    
    background = Color(0xFFE6FFE6),
    onBackground = Color(0xFF003300),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF003300),
    surfaceVariant = Color(0xFFF0FFF0),
    onSurfaceVariant = Color(0xFF006600),
    
    outline = Color(0xFF00CC00),
    outlineVariant = Color(0xFF80FF80),
    scrim = Color(0xFF000000),
    
    error = Color(0xFFCC0033),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFE6E6),
    onErrorContainer = Color(0xFF330008),
    
    surfaceTint = Color(0xFF00CC00)
)

// ============================================================================
// AMOLED THEME - True Black Minimal
// ============================================================================

private val AmoledDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF5555),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFFCC2222),
    onPrimaryContainer = Color(0xFFFFAAAA),
    
    secondary = Color(0xFFCCCCCC),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF333333),
    onSecondaryContainer = Color(0xFFEEEEEE),
    
    tertiary = Color(0xFFAAAAAA),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF222222),
    onTertiaryContainer = Color(0xFFDDDDDD),
    
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF111111),
    onSurfaceVariant = Color(0xFFCCCCCC),
    
    outline = Color(0xFF444444),
    outlineVariant = Color(0xFF222222),
    scrim = Color(0xFF000000),
    
    error = Color(0xFFFF5555),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFFCC2222),
    onErrorContainer = Color(0xFFFFAAAA),
    
    surfaceTint = Color(0xFFFF5555)
)

private val AmoledLightColorScheme = lightColorScheme(
    primary = Color(0xFFDD2222),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE6E6),
    onPrimaryContainer = Color(0xFF440000),
    
    secondary = Color(0xFF777777),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF5F5F5),
    onSecondaryContainer = Color(0xFF111111),
    
    tertiary = Color(0xFF555555),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEEEEEE),
    onTertiaryContainer = Color(0xFF000000),
    
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF333333),
    
    outline = Color(0xFFBBBBBB),
    outlineVariant = Color(0xFFDDDDDD),
    scrim = Color(0xFF000000),
    
    error = Color(0xFFDD2222),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFE6E6),
    onErrorContainer = Color(0xFF440000),
    
    surfaceTint = Color(0xFFDD2222)
)

// ============================================================================
// PASTEL THEME - Soft Dreamy Colors
// ============================================================================

private val PastelDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD4A5FF),
    onPrimary = Color(0xFF2A1A3D),
    primaryContainer = Color(0xFF8B5FBD),
    onPrimaryContainer = Color(0xFFEDD5FF),
    
    secondary = Color(0xFFFFB3D9),
    onSecondary = Color(0xFF3D1A2E),
    secondaryContainer = Color(0xFFBD5F8F),
    onSecondaryContainer = Color(0xFFFFD5ED),
    
    tertiary = Color(0xFFB3E6FF),
    onTertiary = Color(0xFF1A2E3D),
    tertiaryContainer = Color(0xFF5F8FBD),
    onTertiaryContainer = Color(0xFFD5EDFF),
    
    background = Color(0xFF1A0F26),
    onBackground = Color(0xFFF0E6FF),
    surface = Color(0xFF2A1F3D),
    onSurface = Color(0xFFF0E6FF),
    surfaceVariant = Color(0xFF3D2F52),
    onSurfaceVariant = Color(0xFFD4C5E6),
    
    outline = Color(0xFF9B7FB3),
    outlineVariant = Color(0xFF52335F),
    scrim = Color(0xFF000000),
    
    error = Color(0xFFFF99B3),
    onError = Color(0xFF3D1A22),
    errorContainer = Color(0xFFBD5F75),
    onErrorContainer = Color(0xFFFFD5E0),
    
    surfaceTint = Color(0xFFD4A5FF)
)

private val PastelLightColorScheme = lightColorScheme(
    primary = Color(0xFF8B5FBD),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF0E6FF),
    onPrimaryContainer = Color(0xFF2A1A3D),
    
    secondary = Color(0xFFBD5F8F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE6F2),
    onSecondaryContainer = Color(0xFF3D1A2E),
    
    tertiary = Color(0xFF5F8FBD),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE6F2FF),
    onTertiaryContainer = Color(0xFF1A2E3D),
    
    background = Color(0xFFFAF0FF),
    onBackground = Color(0xFF2A1A3D),
    surface = Color(0xFFFAF0FF),
    onSurface = Color(0xFF2A1A3D),
    surfaceVariant = Color(0xFFF5E6FF),
    onSurfaceVariant = Color(0xFF524F5C),
    
    outline = Color(0xFF9B7FB3),
    outlineVariant = Color(0xFFD4C5E6),
    scrim = Color(0xFF000000),
    
    error = Color(0xFFBD2F52),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFE6ED),
    onErrorContainer = Color(0xFF3D0F1A),
    
    surfaceTint = Color(0xFF8B5FBD)
)

// ============================================================================
// EXPRESSIVE THEME - Bold Material 3 Expressive
// ============================================================================

private val ExpressiveDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFD60A),
    onPrimary = Color(0xFF3D2F00),
    primaryContainer = Color(0xFFB8A000),
    onPrimaryContainer = Color(0xFFFFF566),
    
    secondary = Color(0xFFFF453A),
    onSecondary = Color(0xFF3D0F0A),
    secondaryContainer = Color(0xFFBD2217),
    onSecondaryContainer = Color(0xFFFF9980),
    
    tertiary = Color(0xFF30D158),
    onTertiary = Color(0xFF0A3D17),
    tertiaryContainer = Color(0xFF17BD3B),
    onTertiaryContainer = Color(0xFF80FF99),
    
    background = Color(0xFF0F0A00),
    onBackground = Color(0xFFFFF566),
    surface = Color(0xFF1F1A0A),
    onSurface = Color(0xFFFFF566),
    surfaceVariant = Color(0xFF3D331A),
    onSurfaceVariant = Color(0xFFE6CC80),
    
    // Material 3 Surface Container Colors for depth hierarchy
    surfaceContainer = Color(0xFF2A2410),
    surfaceContainerLow = Color(0xFF1F1A0A),
    surfaceContainerHigh = Color(0xFF3D331A),
    surfaceContainerHighest = Color(0xFF4A3F20),
    
    outline = Color(0xFFB8A000),
    outlineVariant = Color(0xFF5C4D1A),
    scrim = Color(0xFF000000),
    
    error = Color(0xFFFF453A),
    onError = Color(0xFF3D0F0A),
    errorContainer = Color(0xFFBD2217),
    onErrorContainer = Color(0xFFFF9980),
    
    surfaceTint = Color(0xFFFFD60A)
)

private val ExpressiveLightColorScheme = lightColorScheme(
    primary = Color(0xFFB8A000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFF566),
    onPrimaryContainer = Color(0xFF3D2F00),
    
    secondary = Color(0xFFBD2217),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE6E6),
    onSecondaryContainer = Color(0xFF3D0F0A),
    
    tertiary = Color(0xFF17BD3B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE6FFE6),
    onTertiaryContainer = Color(0xFF0A3D17),
    
    background = Color(0xFFFFFCC7),
    onBackground = Color(0xFF3D2F00),
    surface = Color(0xFFFFFCC7),
    onSurface = Color(0xFF3D2F00),
    surfaceVariant = Color(0xFFFFF9B3),
    onSurfaceVariant = Color(0xFF5C4D1A),
    
    // Material 3 Surface Container Colors for depth hierarchy
    surfaceContainer = Color(0xFFF5F0B3),
    surfaceContainerLow = Color(0xFFFFFCC7),
    surfaceContainerHigh = Color(0xFFF0EBA0),
    surfaceContainerHighest = Color(0xFFEAE58D),
    
    outline = Color(0xFFB8A000),
    outlineVariant = Color(0xFFE6CC80),
    scrim = Color(0xFF000000),
    
    error = Color(0xFFBD2217),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFE6E6),
    onErrorContainer = Color(0xFF3D0F0A),
    
    surfaceTint = Color(0xFFB8A000)
)

// ============================================================================
// THEME SYSTEM FUNCTIONS
// ============================================================================

/**
 * Get the appropriate color scheme based on theme style and dark mode
 */
private fun getColorScheme(themeStyle: AppThemeStyle, isDark: Boolean): ColorScheme = when (themeStyle) {
    AppThemeStyle.CLASSIC -> if (isDark) ClassicDarkColorScheme else ClassicLightColorScheme
    AppThemeStyle.Y2K -> if (isDark) Y2KDarkColorScheme else Y2KLightColorScheme
    AppThemeStyle.NEON -> if (isDark) NeonDarkColorScheme else NeonLightColorScheme
    AppThemeStyle.AMOLED -> if (isDark) AmoledDarkColorScheme else AmoledLightColorScheme
    AppThemeStyle.PASTEL -> if (isDark) PastelDarkColorScheme else PastelLightColorScheme
    AppThemeStyle.EXPRESSIVE -> if (isDark) ExpressiveDarkColorScheme else ExpressiveLightColorScheme
}

/**
 * Get shapes based on theme style
 */
private fun getShapes(themeStyle: AppThemeStyle): M3Shapes = when (themeStyle) {
    AppThemeStyle.EXPRESSIVE -> M3Shapes(
        // Expressive asymmetric shapes for more playful appearance
        extraSmall = RoundedCornerShape(
            topStart = 8.dp,
            topEnd = 12.dp,
            bottomStart = 12.dp,
            bottomEnd = 8.dp
        ),
        small = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 20.dp,
            bottomStart = 20.dp,
            bottomEnd = 16.dp
        ),
        medium = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 28.dp,
            bottomStart = 28.dp,
            bottomEnd = 24.dp
        ),
        large = RoundedCornerShape(
            topStart = 32.dp,
            topEnd = 36.dp,
            bottomStart = 36.dp,
            bottomEnd = 32.dp
        ),
        extraLarge = RoundedCornerShape(
            topStart = 40.dp,
            topEnd = 44.dp,
            bottomStart = 44.dp,
            bottomEnd = 40.dp
        )
    )
    AppThemeStyle.Y2K -> M3Shapes(
        extraSmall = RoundedCornerShape(2.dp),
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(6.dp),
        large = RoundedCornerShape(8.dp),
        extraLarge = RoundedCornerShape(12.dp)
    )
    AppThemeStyle.NEON -> M3Shapes(
        extraSmall = RoundedCornerShape(0.dp),
        small = RoundedCornerShape(2.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(8.dp),
        extraLarge = RoundedCornerShape(12.dp)
    )
    else -> M3Shapes() // Default Material 3 shapes for other themes
}

/**
 * Get typography based on theme style
 */
private fun getTypography(themeStyle: AppThemeStyle, fontState: FontState? = null): Material3Typography = when (themeStyle) {
    AppThemeStyle.EXPRESSIVE -> {
        // Expressive typography with enhanced line heights and letter spacing
        Material3Typography(
            displayLarge = TextStyle(
                fontFamily = fontState?.getTitleFont() ?: FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 57.sp,
                lineHeight = 64.sp,
                letterSpacing = (-0.25).sp
            ),
            displayMedium = TextStyle(
                fontFamily = fontState?.getTitleFont() ?: FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 45.sp,
                lineHeight = 52.sp,
                letterSpacing = 0.sp
            ),
            displaySmall = TextStyle(
                fontFamily = fontState?.getTitleFont() ?: FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                letterSpacing = 0.sp
            ),
            headlineLarge = TextStyle(
                fontFamily = fontState?.getTitleFont() ?: FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.sp
            ),
            headlineMedium = TextStyle(
                fontFamily = fontState?.getTitleFont() ?: FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = 0.sp
            ),
            headlineSmall = TextStyle(
                fontFamily = fontState?.getTitleFont() ?: FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.sp
            ),
            titleLarge = TextStyle(
                fontFamily = fontState?.getTitleFont() ?: FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp
            ),
            titleMedium = TextStyle(
                fontFamily = fontState?.getTitleFont() ?: FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp
            ),
            titleSmall = TextStyle(
                fontFamily = fontState?.getTitleFont() ?: FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = fontState?.getBodyFont() ?: FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            ),
            bodyMedium = TextStyle(
                fontFamily = fontState?.getBodyFont() ?: FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp
            ),
            bodySmall = TextStyle(
                fontFamily = fontState?.getBodyFont() ?: FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp
            ),
            labelLarge = TextStyle(
                fontFamily = fontState?.getBodyFont() ?: FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp
            ),
            labelMedium = TextStyle(
                fontFamily = fontState?.getBodyFont() ?: FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp
            ),
            labelSmall = TextStyle(
                fontFamily = fontState?.getBodyFont() ?: FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp
            )
        )
    }
    else -> {
        // Use custom typography for other themes
        if (fontState != null) {
            createTypography(
                titleFont = fontState.getTitleFont(),
                bodyFont = fontState.getBodyFont(),
                fontSizeSettings = fontState.fontSizeSettings
            )
        } else {
            Material3Typography()
        }
    }
}

// ============================================================================
// COMPOSITION LOCALS AND THEME COMPOSABLES
// ============================================================================

// Create placeholder instances for CompositionLocal defaults
private fun createPlaceholderFontState(): FontState {
    throw IllegalStateException("FontState should be provided via dependency injection")
}

private fun createPlaceholderThemeState(): ThemeState {
    throw IllegalStateException("ThemeState should be provided via dependency injection")
}

val LocalFontState = staticCompositionLocalOf { createPlaceholderFontState() }
val LocalThemeState = staticCompositionLocalOf { createPlaceholderThemeState() }

/**
 * Main theme composable with support for multiple theme styles
 */
@Composable
fun AppTheme(
    themeStyle: AppThemeStyle = AppThemeStyle.CLASSIC,
    darkModeEnabled: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkModeEnabled) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> getColorScheme(themeStyle, darkModeEnabled)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkModeEnabled
                isAppearanceLightNavigationBars = !darkModeEnabled
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(themeStyle),
        shapes = getShapes(themeStyle),
        content = content
    )
}

/**
 * Legacy theme composable for backward compatibility with existing FontState system
 */
@Composable
fun GlyphZenTheme(
    themeState: ThemeState,
    fontState: FontState,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = themeState.isDarkTheme
    val themeStyle = themeState.themeStyle

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> getColorScheme(themeStyle, darkTheme)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    val typography = getTypography(themeStyle, fontState)

    CompositionLocalProvider(
        LocalFontState provides fontState,
        LocalThemeState provides themeState
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = getShapes(themeStyle),
            content = content
        )
    }
}
