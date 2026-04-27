package com.kanyandula.nyasa.ui.components.richtext

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mohamedrejeb.richeditor.model.RichTextState

/**
 * Formatting toolbar shown while the body editor has focus.
 *
 * Owns its own dialog visibility state (rememberSaveable so it survives configuration
 * changes). Hosts both the toolbar and the link insertion dialog so each consumer screen
 * just decides when to show it (typically when the editor has focus). Designed to be
 * placed inside a Scaffold `bottomBar` slot, stacked above the screen's existing bottom
 * bar (e.g. PublishBar). Scaffold handles its own IME/system insets — this composable
 * does not add `imePadding`.
 */
@Composable
fun RichTextToolbarOverlay(
    state: RichTextState,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    var linkDialogPrefill: String? by rememberSaveable { mutableStateOf(null) }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier
    ) {
        RichTextToolbar(
            state = state,
            onInsertLinkClick = {
                val sel = state.selection
                linkDialogPrefill = if (sel.collapsed) {
                    ""
                } else {
                    state.annotatedString.text.substring(sel.start, sel.end)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }

    linkDialogPrefill?.let { prefill ->
        LinkInsertDialog(
            initialUrl = "",
            initialText = prefill,
            onConfirm = { url, text ->
                state.addLink(text = text, url = url)
                linkDialogPrefill = null
            },
            onDismiss = { linkDialogPrefill = null }
        )
    }
}
