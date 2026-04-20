package com.kanyandula.nyasa.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Primary
val Primary = Color(0xFF005275)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFF1B6B93)
val OnPrimaryContainer = Color(0xFFC7E7FF)
val PrimaryFixed = Color(0xFFC7E7FF)
val PrimaryFixedDim = Color(0xFF8BCEFB)
val OnPrimaryFixed = Color(0xFF001E2E)
val OnPrimaryFixedVariant = Color(0xFF004C6C)

// Secondary
val Secondary = Color(0xFF954A00)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFFF9B4E)
val OnSecondaryContainer = Color(0xFF6F3600)
val SecondaryFixed = Color(0xFFFFDCC6)
val SecondaryFixedDim = Color(0xFFFFB784)
val OnSecondaryFixed = Color(0xFF301400)
val OnSecondaryFixedVariant = Color(0xFF713700)

// Tertiary
val Tertiary = Color(0xFF6D4400)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFF8B5A0D)
val OnTertiaryContainer = Color(0xFFFFDDB6)
val TertiaryFixed = Color(0xFFFFDDB7)
val TertiaryFixedDim = Color(0xFFFBBA68)
val OnTertiaryFixed = Color(0xFF2A1700)
val OnTertiaryFixedVariant = Color(0xFF653E00)

// Error
val Error = Color(0xFFBA1A1A)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFDAD6)
val OnErrorContainer = Color(0xFF93000A)

// Surface
val Surface = Color(0xFFFEF8F3)
val OnSurface = Color(0xFF1D1B19)
val SurfaceBright = Color(0xFFFEF8F3)
val SurfaceDim = Color(0xFFDED9D4)
val SurfaceContainerLowest = Color(0xFFFFFFFF)
val SurfaceContainerLow = Color(0xFFF8F3EE)
val SurfaceContainer = Color(0xFFF2EDE8)
val SurfaceContainerHigh = Color(0xFFECE7E2)
val SurfaceContainerHighest = Color(0xFFE6E2DD)
val SurfaceVariant = Color(0xFFE6E2DD)
val OnSurfaceVariant = Color(0xFF40484E)
val SurfaceTint = Color(0xFF0E658C)

// Inverse
val InverseSurface = Color(0xFF32302D)
val InverseOnSurface = Color(0xFFF5F0EB)
val InversePrimary = Color(0xFF8BCEFB)

// Outline
val Outline = Color(0xFF70787F)
val OutlineVariant = Color(0xFFC0C7CF)

// Accent
val SunsetOrange = Color(0xFFE8883C)

val NyasaLightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Surface,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceTint = SurfaceTint,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    inversePrimary = InversePrimary,
    outline = Outline,
    outlineVariant = OutlineVariant,
    surfaceBright = SurfaceBright,
    surfaceDim = SurfaceDim,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainerLowest = SurfaceContainerLowest
)

val NyasaDarkColorScheme = darkColorScheme(
    primary = Color(0xFF8ECAE6),
    onPrimary = Color(0xFF003549),
    primaryContainer = Color(0xFF004D6B),
    onPrimaryContainer = Color(0xFFC0E8FF),
    secondary = Color(0xFFFFB871),
    onSecondary = Color(0xFF4E2600),
    secondaryContainer = Color(0xFF6F3800),
    onSecondaryContainer = Color(0xFFFFDCC2),
    tertiary = Color(0xFFE2C28C),
    onTertiary = Color(0xFF3F2E04),
    tertiaryContainer = Color(0xFF584419),
    onTertiaryContainer = Color(0xFFFFDEA6),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceContainerLowest = Color(0xFF0D0D0D),
    surfaceContainerLow = Color(0xFF1D1B20),
    surfaceContainer = Color(0xFF211F26),
    surfaceContainerHigh = Color(0xFF2B2930),
    surfaceContainerHighest = Color(0xFF36343B),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF313033),
    inversePrimary = Color(0xFF005275),
    surfaceTint = Color(0xFF8ECAE6)
)
