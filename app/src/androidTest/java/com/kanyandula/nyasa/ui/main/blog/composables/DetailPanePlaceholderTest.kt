package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import org.junit.Rule
import org.junit.Test

class DetailPanePlaceholderTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun rendersSelectAPostPrompt() {
        composeRule.setContent {
            NyasaTheme {
                DetailPanePlaceholder()
            }
        }

        composeRule.onNodeWithText("Select a post to read").assertIsDisplayed()
    }
}
