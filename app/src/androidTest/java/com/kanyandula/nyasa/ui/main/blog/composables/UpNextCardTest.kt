package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import org.junit.Rule
import org.junit.Test

class UpNextCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val samplePost = BlogPost(
        pk = 1,
        title = "Lake Malawi at Sunrise",
        slug = "lake-malawi-at-sunrise",
        body = "",
        image = "https://example.test/img.jpg",
        date_updated = 1746057600L,
        username = "tamanda",
        category = "Travel",
        tags = "",
        view_count = 0,
        like_count = 0,
        comment_count = 0,
        reading_time = 4,
        author_avatar = null
    )

    @Test
    fun rendersTitleAndByline() {
        composeRule.setContent {
            NyasaTheme {
                UpNextCard(blogPost = samplePost, onClick = {})
            }
        }

        composeRule.onNodeWithText("Lake Malawi at Sunrise").assertIsDisplayed()
        composeRule.onNodeWithText("tamanda").assertIsDisplayed()
    }
}
