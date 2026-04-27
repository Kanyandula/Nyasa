package com.kanyandula.nyasa.ui.components.richtext

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RichTextToolbarUiTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun bold_button_toggles_currentSpanStyle_fontWeight() {
        var captured: RichTextState? = null
        rule.setContent {
            NyasaTheme {
                val state = rememberRichTextState()
                captured = state
                RichTextToolbar(state = state, onInsertLinkClick = {})
            }
        }

        rule.onNodeWithContentDescription("Bold").performClick()
        rule.runOnIdle {
            assertEquals(FontWeight.Bold, captured!!.currentSpanStyle.fontWeight)
        }
        rule.onNodeWithContentDescription("Bold").performClick()
        rule.runOnIdle {
            assertTrue(captured!!.currentSpanStyle.fontWeight != FontWeight.Bold)
        }
    }

    @Test
    fun italic_button_toggles_currentSpanStyle_fontStyle() {
        var captured: RichTextState? = null
        rule.setContent {
            NyasaTheme {
                val state = rememberRichTextState()
                captured = state
                RichTextToolbar(state = state, onInsertLinkClick = {})
            }
        }

        rule.onNodeWithContentDescription("Italic").performClick()
        rule.runOnIdle {
            assertEquals(FontStyle.Italic, captured!!.currentSpanStyle.fontStyle)
        }
        rule.onNodeWithContentDescription("Italic").performClick()
        rule.runOnIdle {
            assertTrue(captured!!.currentSpanStyle.fontStyle != FontStyle.Italic)
        }
    }

    @Test
    fun underline_button_toggles_currentSpanStyle_textDecoration() {
        var captured: RichTextState? = null
        rule.setContent {
            NyasaTheme {
                val state = rememberRichTextState()
                captured = state
                RichTextToolbar(state = state, onInsertLinkClick = {})
            }
        }

        rule.onNodeWithContentDescription("Underline").performClick()
        rule.runOnIdle {
            assertEquals(TextDecoration.Underline, captured!!.currentSpanStyle.textDecoration)
        }
        rule.onNodeWithContentDescription("Underline").performClick()
        rule.runOnIdle {
            assertTrue(captured!!.currentSpanStyle.textDecoration != TextDecoration.Underline)
        }
    }

    @Test
    fun bullet_list_button_toggles_isUnorderedList() {
        var captured: RichTextState? = null
        rule.setContent {
            NyasaTheme {
                val state = rememberRichTextState()
                captured = state
                RichTextToolbar(state = state, onInsertLinkClick = {})
            }
        }

        rule.onNodeWithContentDescription("Bullet list").performClick()
        rule.runOnIdle { assertTrue(captured!!.isUnorderedList) }
        rule.onNodeWithContentDescription("Bullet list").performClick()
        rule.runOnIdle { assertTrue(!captured!!.isUnorderedList) }
    }

    @Test
    fun numbered_list_button_toggles_isOrderedList() {
        var captured: RichTextState? = null
        rule.setContent {
            NyasaTheme {
                val state = rememberRichTextState()
                captured = state
                RichTextToolbar(state = state, onInsertLinkClick = {})
            }
        }

        rule.onNodeWithContentDescription("Numbered list").performClick()
        rule.runOnIdle { assertTrue(captured!!.isOrderedList) }
        rule.onNodeWithContentDescription("Numbered list").performClick()
        rule.runOnIdle { assertTrue(!captured!!.isOrderedList) }
    }

    @Test
    fun bold_button_active_state_reflects_currentSpanStyle() {
        rule.setContent {
            NyasaTheme {
                val state = rememberRichTextState()
                RichTextToolbar(state = state, onInsertLinkClick = {})
            }
        }

        rule.onNodeWithContentDescription("Bold").assertIsOff()
        rule.onNodeWithContentDescription("Bold").performClick()
        rule.onNodeWithContentDescription("Bold").assertIsOn()
    }

    @Test
    fun link_button_invokes_onInsertLinkClick() {
        val opened = mutableStateOf(false)
        rule.setContent {
            NyasaTheme {
                val state = rememberRichTextState()
                RichTextToolbar(
                    state = state,
                    onInsertLinkClick = { opened.value = true }
                )
            }
        }

        rule.onNodeWithContentDescription("Insert link").performClick()
        rule.runOnIdle { assertTrue(opened.value) }
    }

    @Test
    fun link_dialog_confirm_invokes_onConfirm_with_normalized_url() {
        val showing = mutableStateOf(true)
        var capturedUrl: String? = null
        var capturedText: String? = null
        rule.setContent {
            NyasaTheme {
                if (showing.value) {
                    LinkInsertDialog(
                        initialUrl = "example.com",
                        initialText = "click me",
                        onConfirm = { url, text ->
                            capturedUrl = url
                            capturedText = text
                            showing.value = false
                        },
                        onDismiss = { showing.value = false }
                    )
                }
            }
        }

        rule.onNodeWithText("Insert").performClick()
        rule.runOnIdle {
            assertEquals("https://example.com", capturedUrl)
            assertEquals("click me", capturedText)
        }
    }

    @Test
    fun link_dialog_confirm_disabled_for_blank_url() {
        rule.setContent {
            NyasaTheme {
                LinkInsertDialog(
                    initialUrl = "",
                    initialText = "",
                    onConfirm = { _, _ -> },
                    onDismiss = {}
                )
            }
        }

        rule.onNodeWithText("Insert").assertIsDisplayed()
        rule.onNodeWithText("Insert").assertIsNotEnabled()
    }
}
