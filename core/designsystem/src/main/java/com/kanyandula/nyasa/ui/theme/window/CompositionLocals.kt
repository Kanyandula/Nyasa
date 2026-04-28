package com.kanyandula.nyasa.ui.theme.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Provides the active [WindowClassifier] for adaptive layouts. Set by `NyasaTheme`.
 *
 * Always read reactive helpers via `by` delegation so recomposition tracks the State:
 * ```
 * val isMedium by LocalWindow.current.isMediumWindowAsState()  // recomposes on flip
 * ```
 * Reading `.value` once captures the snapshot and won't update on rotation/fold.
 */
val LocalWindow = staticCompositionLocalOf<WindowClassifier> {
    error("LocalWindow not provided — wrap content in NyasaTheme")
}

/** Remembers a [DefaultWindowClassifier] keyed on its breakpoints. */
@Composable
fun rememberWindowClassifier(
    portraitMediumMinWidthDp: Dp = 600.dp,
    landscapeMediumMaxHeightDp: Dp = 400.dp
): WindowClassifier = remember(portraitMediumMinWidthDp, landscapeMediumMaxHeightDp) {
    DefaultWindowClassifier(portraitMediumMinWidthDp, landscapeMediumMaxHeightDp)
}
