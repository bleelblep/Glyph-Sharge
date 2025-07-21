package com.bleelblep.glyphsharge.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun createTypography(
    titleFont: FontFamily, 
    bodyFont: FontFamily,
    fontSizeSettings: FontSizeSettings = FontSizeSettings()
) = Typography(
    displayLarge = TextStyle(
        fontFamily = titleFont,
        fontWeight = FontWeight.Normal,
        fontSize = (57 * fontSizeSettings.displayScale).sp,
        lineHeight = (64 * fontSizeSettings.displayScale).sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = titleFont,
        fontWeight = FontWeight.Normal,
        fontSize = (45 * fontSizeSettings.displayScale).sp,
        lineHeight = (52 * fontSizeSettings.displayScale).sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = titleFont,
        fontWeight = FontWeight.Normal,
        fontSize = (36 * fontSizeSettings.displayScale).sp,
        lineHeight = (44 * fontSizeSettings.displayScale).sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = titleFont,
        fontWeight = FontWeight.Normal,
        fontSize = (32 * fontSizeSettings.titleScale).sp,
        lineHeight = (40 * fontSizeSettings.titleScale).sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = titleFont,
        fontWeight = FontWeight.Normal,
        fontSize = (28 * fontSizeSettings.titleScale).sp,
        lineHeight = (36 * fontSizeSettings.titleScale).sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = titleFont,
        fontWeight = FontWeight.Normal,
        fontSize = (24 * fontSizeSettings.titleScale).sp,
        lineHeight = (32 * fontSizeSettings.titleScale).sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = titleFont,
        fontWeight = FontWeight.Normal,
        fontSize = (22 * fontSizeSettings.titleScale).sp,
        lineHeight = (28 * fontSizeSettings.titleScale).sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = titleFont,
        fontWeight = FontWeight.Medium,
        fontSize = (16 * fontSizeSettings.titleScale).sp,
        lineHeight = (24 * fontSizeSettings.titleScale).sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = titleFont,
        fontWeight = FontWeight.Medium,
        fontSize = (14 * fontSizeSettings.titleScale).sp,
        lineHeight = (20 * fontSizeSettings.titleScale).sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = (16 * fontSizeSettings.bodyScale).sp,
        lineHeight = (24 * fontSizeSettings.bodyScale).sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = (14 * fontSizeSettings.bodyScale).sp,
        lineHeight = (20 * fontSizeSettings.bodyScale).sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = (12 * fontSizeSettings.bodyScale).sp,
        lineHeight = (16 * fontSizeSettings.bodyScale).sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = (14 * fontSizeSettings.labelScale).sp,
        lineHeight = (20 * fontSizeSettings.labelScale).sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = (12 * fontSizeSettings.labelScale).sp,
        lineHeight = (16 * fontSizeSettings.labelScale).sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = bodyFont,
        fontWeight = FontWeight.Medium,
        fontSize = (11 * fontSizeSettings.labelScale).sp,
        lineHeight = (16 * fontSizeSettings.labelScale).sp,
        letterSpacing = 0.5.sp
    )
) 