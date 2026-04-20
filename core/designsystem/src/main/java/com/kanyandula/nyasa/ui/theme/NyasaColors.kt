package com.kanyandula.nyasa.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class NyasaColors(
    val likeActive: Color,
    val categoryChip: Color,
    val readTimeText: Color,
    val sunsetSkyTop: Color,
    val sunsetSkyBottom: Color,
    val sunsetGradientStart: Color,
    val sunsetGradientEnd: Color,
    val sunsetTextHighlight: Color,
    val sunsetTextGlow: Color
)

val lightNyasaColors = NyasaColors(
    likeActive = Color(0xFFE53935),
    categoryChip = Color(0xFF005275),
    readTimeText = Color(0xFF6B7280),
    sunsetSkyTop = Color(0xFF87CEEB),
    sunsetSkyBottom = Color(0xFFF4A460),
    sunsetGradientStart = Color(0xFFFDB777),
    sunsetGradientEnd = Color(0xFFE8883C),
    sunsetTextHighlight = Color(0xFFFFF3E0),
    sunsetTextGlow = Color(0xFFFFCC80)
)

val darkNyasaColors = NyasaColors(
    likeActive = Color(0xFFEF5350),
    categoryChip = Color(0xFF4FC3F7),
    readTimeText = Color(0xFF9CA3AF),
    sunsetSkyTop = Color(0xFF1A3A5C),
    sunsetSkyBottom = Color(0xFF8B5E3C),
    sunsetGradientStart = Color(0xFFB8864A),
    sunsetGradientEnd = Color(0xFFA0612A),
    sunsetTextHighlight = Color(0xFF3E2C1A),
    sunsetTextGlow = Color(0xFF5C4020)
)

val LocalNyasaColors = staticCompositionLocalOf { lightNyasaColors }
