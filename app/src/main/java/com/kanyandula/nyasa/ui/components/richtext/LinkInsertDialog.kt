package com.kanyandula.nyasa.ui.components.richtext

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.ui.components.NyasaTextField
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@Composable
fun LinkInsertDialog(
    initialUrl: String = "",
    initialText: String = "",
    onConfirm: (url: String, text: String) -> Unit,
    onDismiss: () -> Unit
) {
    var url by rememberSaveable(initialUrl) { mutableStateOf(initialUrl) }
    var text by rememberSaveable(initialText) { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Insert link") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.s)) {
                NyasaTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = "URL",
                    singleLine = true
                )
                NyasaTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = "Display text (optional)",
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = url.isNotBlank(),
                onClick = {
                    val normalizedUrl = normalizeUrl(url)
                    val effectiveText = text.ifBlank { normalizedUrl }
                    onConfirm(normalizedUrl, effectiveText)
                }
            ) { Text("Insert") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.text_cancel)) }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    )
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
private fun LinkInsertDialogPreview() {
    NyasaTheme {
        LinkInsertDialog(
            initialUrl = "",
            initialText = "Selected text",
            onConfirm = { _, _ -> },
            onDismiss = {}
        )
    }
}
