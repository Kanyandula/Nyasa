package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.ui.theme.window.LocalWindow
import com.kanyandula.nyasa.ui.theme.window.WindowClassifier
import com.kanyandula.nyasa.ui.theme.window.WindowSizeClass
import org.junit.Assert.assertEquals
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

private fun Modifier.clickableForTest(onClick: () -> Unit): Modifier =
    this.clickable(onClick = onClick)

class BlogListDetailScaffoldTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun compactWidth_scaffoldNotMounted_clickFiresFullScreenNav() {
        var navigatedSlug: String? = null

        composeRule.setContent {
            CompositionLocalProvider(LocalWindow provides FakeWindow(WindowSizeClass.Small)) {
                NyasaTheme {
                    BlogListDetailScaffold(
                        onNavigateToDetailFullScreen = { slug -> navigatedSlug = slug },
                        visibleSlugs = emptySet(),
                        mode = FeedMode.Home,
                        listPane = { onBlogClicked ->
                            Box(Modifier) {
                                Text(
                                    text = "Tap me",
                                    modifier = Modifier.clickableForTest { onBlogClicked("some-slug") }
                                )
                            }
                        },
                        detailPane = { _, _ -> Text("DETAIL_PANE") }
                    )
                }
            }
        }

        composeRule.onNodeWithText("Tap me").assertIsDisplayed()
        composeRule.onNodeWithText("DETAIL_PANE").assertDoesNotExist()
        composeRule.onNodeWithText("Tap me").performClick()
        assertEquals("some-slug", navigatedSlug)
    }
}
