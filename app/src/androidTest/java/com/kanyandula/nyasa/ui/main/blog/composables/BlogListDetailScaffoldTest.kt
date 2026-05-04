package com.kanyandula.nyasa.ui.main.blog.composables

import android.content.Context
import android.content.res.Configuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.ui.theme.window.LocalWindow
import com.kanyandula.nyasa.ui.theme.window.WindowClassifier
import com.kanyandula.nyasa.ui.theme.window.WindowSizeClass
import org.junit.Rule
import org.junit.Test

private class FakeWindow(private val sizeClass: WindowSizeClass) : WindowClassifier {
    @Composable override fun windowSizeClassAsState(): State<WindowSizeClass> =
        remember { mutableStateOf(sizeClass) }

    @Composable override fun isMediumWindowAsState(): State<Boolean> =
        remember { mutableStateOf(sizeClass == WindowSizeClass.Medium) }

    @Composable override fun isSmallWindowAsState(): State<Boolean> =
        remember { mutableStateOf(sizeClass == WindowSizeClass.Small) }

    @Composable override fun isLandscapeAsState(): State<Boolean> =
        remember { mutableStateOf(false) }
}

class BlogListDetailScaffoldTest {

    @get:Rule
    val composeRule = createComposeRule()

    /**
     * On Small windows the scaffold is not mounted: the wrapper renders only the list slot
     * (no `DetailPanePlaceholder`). The unique placeholder text "Select a post to read" only
     * appears when the scaffold is mounted, so its absence is a faithful signal that the
     * Compact path took the early return.
     */
    @Test
    fun compactWidth_scaffoldNotMounted_listOnly_noPlaceholder() {
        composeRule.setContent {
            NyasaTheme {
                CompositionLocalProvider(LocalWindow provides FakeWindow(WindowSizeClass.Small)) {
                    BlogListDetailScaffold(
                        onNavigateToDetailFullScreen = {},
                        visibleSlugs = emptySet(),
                        mode = FeedMode.Home,
                        listPane = { Text("LIST_PANE_MARKER") },
                        detailPane = { _, _ -> Text("DETAIL_PANE_MARKER") }
                    )
                }
            }
        }

        composeRule.onNodeWithText("LIST_PANE_MARKER").assertIsDisplayed()
        composeRule.onNodeWithText("Select a post to read").assertDoesNotExist()
        composeRule.onNodeWithText("DETAIL_PANE_MARKER").assertDoesNotExist()
    }

    /**
     * On Medium windows the scaffold mounts and the detail pane renders `DetailPanePlaceholder`
     * because the navigator has no current destination yet. The list pane content is also
     * visible (the scaffold's directive on this device is dual-pane, but on a single-pane
     * device the list would still be the primary content).
     */
    @Test
    fun mediumWidth_scaffoldMounted_emptyNavigator_showsPlaceholder() {
        composeRule.setContent {
            NyasaTheme {
                CompositionLocalProvider(LocalWindow provides FakeWindow(WindowSizeClass.Medium)) {
                    BlogListDetailScaffold(
                        onNavigateToDetailFullScreen = {},
                        visibleSlugs = emptySet(),
                        mode = FeedMode.Home,
                        listPane = { Text("LIST_PANE_MARKER") },
                        detailPane = { slug, _ -> Text("DETAIL[$slug]") }
                    )
                }
            }
        }

        composeRule.onNodeWithText("Select a post to read").assertIsDisplayed()
        composeRule.onNodeWithText("DETAIL[post-1]").assertDoesNotExist()
    }

    @Test
    fun expandedWidth_noSelection_rendersExpandedListPane() {
        val expandedConfig = Configuration(
            ApplicationProvider.getApplicationContext<Context>().resources.configuration
        ).apply {
            screenWidthDp = 1280
            screenHeightDp = 800
            smallestScreenWidthDp = 1280
        }

        composeRule.setContent {
            NyasaTheme {
                CompositionLocalProvider(
                    LocalConfiguration provides expandedConfig,
                    LocalWindow provides FakeWindow(WindowSizeClass.Medium)
                ) {
                    BlogListDetailScaffold(
                        onNavigateToDetailFullScreen = {},
                        visibleSlugs = emptySet(),
                        mode = FeedMode.Home,
                        listPane = { Text("LIST_PANE_MARKER") },
                        detailPane = { _, _ -> Text("DETAIL_PANE_MARKER") },
                        expandedListPane = { Text("EXPANDED_MARKER") }
                    )
                }
            }
        }

        composeRule.onNodeWithText("EXPANDED_MARKER").assertIsDisplayed()
        composeRule.onNodeWithText("Select a post to read").assertDoesNotExist()
        composeRule.onNodeWithText("LIST_PANE_MARKER").assertDoesNotExist()
    }
}
