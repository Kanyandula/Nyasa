package com.kanyandula.nyasa.ui.components.richtext

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState

@Composable
fun RichTextToolbar(
    state: RichTextState,
    onInsertLinkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = NyasaTheme.spacing.s, vertical = NyasaTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.xs)
        ) {
            ToggleButton(
                checked = state.currentSpanStyle.fontWeight == FontWeight.Bold,
                onCheckedChange = {
                    state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                }
            ) { Icon(Icons.Filled.FormatBold, contentDescription = "Bold") }

            ToggleButton(
                checked = state.currentSpanStyle.fontStyle == FontStyle.Italic,
                onCheckedChange = {
                    state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                }
            ) { Icon(Icons.Filled.FormatItalic, contentDescription = "Italic") }

            ToggleButton(
                checked = state.currentSpanStyle.textDecoration == TextDecoration.Underline,
                onCheckedChange = {
                    state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                }
            ) { Icon(Icons.Filled.FormatUnderlined, contentDescription = "Underline") }

            ToggleButton(
                checked = state.isUnorderedList,
                onCheckedChange = { state.toggleUnorderedList() }
            ) { Icon(Icons.Filled.FormatListBulleted, contentDescription = "Bullet list") }

            ToggleButton(
                checked = state.isOrderedList,
                onCheckedChange = { state.toggleOrderedList() }
            ) { Icon(Icons.Filled.FormatListNumbered, contentDescription = "Numbered list") }

            IconButton(onClick = onInsertLinkClick) {
                Icon(Icons.Filled.Link, contentDescription = "Insert link")
            }
        }
    }
}

@Composable
private fun ToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    FilledIconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange
    ) { content() }
}

internal fun normalizeUrl(input: String): String {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return trimmed
    val lower = trimmed.lowercase()
    return when {
        lower.startsWith("https://") || lower.startsWith("http://") -> trimmed
        lower.startsWith("mailto:") -> trimmed
        else -> "https://$trimmed"
    }
}

@Preview
@Composable
private fun RichTextToolbarPreview() {
    NyasaTheme {
        val state = rememberRichTextState()
        RichTextToolbar(
            state = state,
            onInsertLinkClick = {}
        )
    }
}
