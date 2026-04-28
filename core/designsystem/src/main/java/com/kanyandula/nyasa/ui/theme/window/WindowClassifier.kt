package com.kanyandula.nyasa.ui.theme.window

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reactive window-size classifier. Read each helper via `by` delegation so the consumer
 * recomposes when the value flips:
 * ```
 * val isMedium by LocalWindow.current.isMediumWindowAsState()
 * ```
 */
interface WindowClassifier {
    @Composable fun windowSizeClassAsState(): State<WindowSizeClass>
    @Composable fun isMediumWindowAsState(): State<Boolean>
    @Composable fun isSmallWindowAsState(): State<Boolean>
    @Composable fun isLandscapeAsState(): State<Boolean>
}

/**
 * [WindowClassifier] backed by [LocalConfiguration]. Classifies a window as [WindowSizeClass.Medium]
 * when either:
 * - width ≥ [portraitMediumMinWidthDp] (tablets in any orientation), or
 * - landscape with height < [landscapeMediumMaxHeightDp] (phone landscape — claws back vertical
 *   space by switching to a side rail).
 */
class DefaultWindowClassifier(
    private val portraitMediumMinWidthDp: Dp = 600.dp,
    private val landscapeMediumMaxHeightDp: Dp = 400.dp
) : WindowClassifier {

    @Composable
    override fun windowSizeClassAsState(): State<WindowSizeClass> {
        val configuration = LocalConfiguration.current
        return remember(configuration, portraitMediumMinWidthDp, landscapeMediumMaxHeightDp) {
            derivedStateOf { classify(configuration) }
        }
    }

    @Composable
    override fun isMediumWindowAsState(): State<Boolean> {
        val sizeClassState = windowSizeClassAsState()
        return remember(sizeClassState) {
            derivedStateOf { sizeClassState.value == WindowSizeClass.Medium }
        }
    }

    @Composable
    override fun isSmallWindowAsState(): State<Boolean> {
        val sizeClassState = windowSizeClassAsState()
        return remember(sizeClassState) {
            derivedStateOf { sizeClassState.value == WindowSizeClass.Small }
        }
    }

    @Composable
    override fun isLandscapeAsState(): State<Boolean> {
        val configuration = LocalConfiguration.current
        return remember(configuration) {
            derivedStateOf { configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
        }
    }

    private fun classify(configuration: Configuration): WindowSizeClass = classify(
        widthDp = configuration.screenWidthDp.dp,
        heightDp = configuration.screenHeightDp.dp,
        isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE,
        portraitMediumMinWidthDp = portraitMediumMinWidthDp,
        landscapeMediumMaxHeightDp = landscapeMediumMaxHeightDp
    )
}

internal fun classify(
    widthDp: Dp,
    heightDp: Dp,
    isLandscape: Boolean,
    portraitMediumMinWidthDp: Dp,
    landscapeMediumMaxHeightDp: Dp
): WindowSizeClass {
    val widthQualifies = widthDp >= portraitMediumMinWidthDp
    val landscapeShortHeightQualifies = isLandscape && heightDp < landscapeMediumMaxHeightDp
    return if (widthQualifies || landscapeShortHeightQualifies) {
        WindowSizeClass.Medium
    } else {
        WindowSizeClass.Small
    }
}
