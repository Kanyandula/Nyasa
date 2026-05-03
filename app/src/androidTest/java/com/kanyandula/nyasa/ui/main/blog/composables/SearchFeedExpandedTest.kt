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

class SearchFeedExpandedTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun samplePost(index: Int) = BlogPost(
        pk = index, // BlogPost.pk is Int (per Task 2 finding)
        title = "Search Result $index",
        slug = "result-$index",
        body = "",
        image = "https://example.test/$index.jpg",
        date_updated = 1746057600L, // BlogPost.date_updated is Long (per Task 1 finding)
        username = "author$index",
        category = "Travel",
        tags = "",
        view_count = 0,
        like_count = 0,
        comment_count = 0,
        reading_time = 4,
        author_avatar = null
    )

    @Test
    fun rendersAllResultsInGrid() {
        val posts = (0..5).map { samplePost(it) }
        val pagingFlow = flowOf(PagingData.from(posts))

        composeRule.setContent {
            NyasaTheme {
                SearchFeedExpanded(
                    pagingDataFlow = pagingFlow,
                    state = BlogListUiState(),
                    onAction = {},
                    onBlogClicked = {}
                )
            }
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("Search Result 0").assertIsDisplayed()
        composeRule.onNodeWithText("Search Result 1").assertIsDisplayed()
        composeRule.onNodeWithText("Search Result 5").assertIsDisplayed()
        composeRule.onNodeWithText("Up next").assertDoesNotExist()
    }
}
