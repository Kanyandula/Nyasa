package com.kanyandula.nyasa.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.kanyandula.nyasa.ui.theme.window.LocalWindow
import com.kanyandula.nyasa.ui.theme.window.rememberWindowClassifier

object NyasaTheme {
    val spacing: NyasaSpacing
        @Composable @ReadOnlyComposable
        get() = LocalNyasaSpacing.current

    val colors: NyasaColors
        @Composable @ReadOnlyComposable
        get() = LocalNyasaColors.current
}

@Composable
fun NyasaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NyasaDarkColorScheme else NyasaLightColorScheme
    val nyasaColors = if (darkTheme) darkNyasaColors else lightNyasaColors

    CompositionLocalProvider(
        LocalNyasaSpacing provides NyasaSpacing(),
        LocalNyasaColors provides nyasaColors,
        LocalWindow provides rememberWindowClassifier()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NyasaTypography,
            shapes = NyasaShapes,
            content = content
        )
    }
}
