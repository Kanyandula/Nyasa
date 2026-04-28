package com.kanyandula.nyasa.ui.theme.window

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview

/**
 * Slot-based router that swaps between [mediumWindow] and [default] content based on the
 * current [WindowSizeClass] from [LocalWindow]. Use for screens that branch wholesale on
 * size class; for finer-grained branching read [WindowClassifier.isMediumWindowAsState]
 * inline.
 */
@Composable
fun WindowSizeClassScaffold(
    mediumWindow: @Composable () -> Unit,
    default: @Composable () -> Unit
) {
    val isMedium by LocalWindow.current.isMediumWindowAsState()
    if (isMedium) mediumWindow() else default()
}

private class FixedWindowClassifier(private val sizeClass: WindowSizeClass) : WindowClassifier {
    @Composable override fun windowSizeClassAsState(): State<WindowSizeClass> =
        remember { mutableStateOf(sizeClass) }
    @Composable override fun isMediumWindowAsState(): State<Boolean> =
        remember { mutableStateOf(sizeClass == WindowSizeClass.Medium) }
    @Composable override fun isSmallWindowAsState(): State<Boolean> =
        remember { mutableStateOf(sizeClass == WindowSizeClass.Small) }
    @Composable override fun isLandscapeAsState(): State<Boolean> =
        remember { mutableStateOf(false) }
}

@Preview
@Composable
private fun WindowSizeClassScaffoldMediumPreview() {
    CompositionLocalProvider(LocalWindow provides FixedWindowClassifier(WindowSizeClass.Medium)) {
        WindowSizeClassScaffold(
            mediumWindow = { Text("Medium") },
            default = { Text("Small") }
        )
    }
}

@Preview
@Composable
private fun WindowSizeClassScaffoldSmallPreview() {
    CompositionLocalProvider(LocalWindow provides FixedWindowClassifier(WindowSizeClass.Small)) {
        WindowSizeClassScaffold(
            mediumWindow = { Text("Medium") },
            default = { Text("Small") }
        )
    }
}
