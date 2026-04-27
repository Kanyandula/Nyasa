package com.kanyandula.nyasa.ui.components.richtext

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
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
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Display text (optional)") },
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
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
