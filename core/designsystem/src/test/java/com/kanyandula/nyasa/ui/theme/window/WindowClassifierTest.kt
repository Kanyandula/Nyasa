package com.kanyandula.nyasa.ui.theme.window

import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WindowClassifierTest {

    private val portraitMin = 600.dp
    private val landscapeMaxHeight = 400.dp

    @Test
    fun `phone portrait below breakpoint classifies as Small`() {
        val result = classify(
            widthDp = 360.dp,
            heightDp = 800.dp,
            isLandscape = false,
            portraitMediumMinWidthDp = portraitMin,
            landscapeMediumMaxHeightDp = landscapeMaxHeight
        )
        assertThat(result).isEqualTo(WindowSizeClass.Small)
    }

    @Test
    fun `tablet portrait at breakpoint classifies as Medium`() {
        val result = classify(
            widthDp = 600.dp,
            heightDp = 960.dp,
            isLandscape = false,
            portraitMediumMinWidthDp = portraitMin,
            landscapeMediumMaxHeightDp = landscapeMaxHeight
        )
        assertThat(result).isEqualTo(WindowSizeClass.Medium)
    }

    @Test
    fun `phone landscape with short height classifies as Medium`() {
        val result = classify(
            widthDp = 800.dp,
            heightDp = 360.dp,
            isLandscape = true,
            portraitMediumMinWidthDp = portraitMin,
            landscapeMediumMaxHeightDp = landscapeMaxHeight
        )
        assertThat(result).isEqualTo(WindowSizeClass.Medium)
    }

    @Test
    fun `narrow landscape with tall enough height classifies as Small`() {
        // Width below portrait threshold AND height above landscape threshold → Small.
        val result = classify(
            widthDp = 480.dp,
            heightDp = 500.dp,
            isLandscape = true,
            portraitMediumMinWidthDp = portraitMin,
            landscapeMediumMaxHeightDp = landscapeMaxHeight
        )
        assertThat(result).isEqualTo(WindowSizeClass.Small)
    }

    @Test
    fun `custom portrait breakpoint overrides default`() {
        val result = classify(
            widthDp = 600.dp,
            heightDp = 960.dp,
            isLandscape = false,
            portraitMediumMinWidthDp = 720.dp,
            landscapeMediumMaxHeightDp = landscapeMaxHeight
        )
        assertThat(result).isEqualTo(WindowSizeClass.Small)
    }

    @Test
    fun `tablet landscape classifies as Medium via width qualifier`() {
        val result = classify(
            widthDp = 1280.dp,
            heightDp = 800.dp,
            isLandscape = true,
            portraitMediumMinWidthDp = portraitMin,
            landscapeMediumMaxHeightDp = landscapeMaxHeight
        )
        assertThat(result).isEqualTo(WindowSizeClass.Medium)
    }

    @Test
    fun `phone landscape with width below threshold still qualifies via short height`() {
        // 640 x 360 phone landscape — width (640dp) >= 600dp also qualifies via
        // width path; verify the OR logic by using a width below 600dp.
        val result = classify(
            widthDp = 580.dp,
            heightDp = 360.dp,
            isLandscape = true,
            portraitMediumMinWidthDp = portraitMin,
            landscapeMediumMaxHeightDp = landscapeMaxHeight
        )
        assertThat(result).isEqualTo(WindowSizeClass.Medium)
    }
}
