package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.PagingData
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.ui.main.blog.state.BlogListUiState
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class HomeFeedExpandedTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun samplePost(index: Int) = BlogPost(
        pk = index,
        title = "Post $index Title",
        slug = "post-$index",
        body = "",
        image = "https://example.test/$index.jpg",
        date_updated = 1746057600L,
        username = "user$index",
        category = "Culture",
        tags = "",
        view_count = 0,
        like_count = 0,
        comment_count = 0,
        reading_time = 4,
        author_avatar = null
    )

    @Test
    fun fullData_rendersHeroAndGridAndRail() {
        val posts = (0..9).map { samplePost(it) }
        val pagingFlow = flowOf(PagingData.from(posts))

        composeRule.setContent {
            NyasaTheme {
                HomeFeedExpanded(
                    pagingDataFlow = pagingFlow,
                    state = BlogListUiState(),
                    onAction = {},
                    onBlogClicked = {}
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Post 0 Title").assertIsDisplayed()
        composeRule.onNodeWithText("Post 1 Title").assertIsDisplayed()
        composeRule.onNodeWithText("Post 4 Title").assertIsDisplayed()
        composeRule.onNodeWithText("Post 5 Title").assertIsDisplayed()
        composeRule.onNodeWithText("Post 9 Title").assertIsDisplayed()
    }

    @Test
    fun partialData_rendersHeroAndGridOmitsRail() {
        val posts = (0..2).map { samplePost(it) }
        val pagingFlow = flowOf(PagingData.from(posts))

        composeRule.setContent {
            NyasaTheme {
                HomeFeedExpanded(
                    pagingDataFlow = pagingFlow,
                    state = BlogListUiState(),
                    onAction = {},
                    onBlogClicked = {}
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Post 0 Title").assertIsDisplayed()
        composeRule.onNodeWithText("Post 1 Title").assertIsDisplayed()
        composeRule.onNodeWithText("Post 2 Title").assertIsDisplayed()
        composeRule.onNodeWithText("Post 5 Title").assertDoesNotExist()
    }
}
