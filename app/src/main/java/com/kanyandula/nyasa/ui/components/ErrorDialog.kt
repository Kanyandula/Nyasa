package com.kanyandula.nyasa.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@Composable
private fun NyasaDialog(
    message: String,
    onDismiss: () -> Unit,
    title: String,
    titleColor: Color = Color.Unspecified
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = titleColor
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.text_ok))
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    )
}

@Composable
fun NyasaErrorDialog(
    message: String,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.text_error)
) {
    NyasaDialog(message = message, onDismiss = onDismiss, title = title)
}

@Composable
fun NyasaSuccessDialog(
    message: String,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.text_success)
) {
    NyasaDialog(
        message = message,
        onDismiss = onDismiss,
        title = title,
        titleColor = MaterialTheme.colorScheme.primary
    )
}

@Preview
@Composable
private fun ErrorDialogPreview() {
    NyasaTheme {
        NyasaErrorDialog(
            message = "Failed to load blog posts. Please check your connection.",
            onDismiss = {}
        )
    }
}

@Preview
@Composable
private fun SuccessDialogPreview() {
    NyasaTheme {
        NyasaSuccessDialog(
            message = "Blog post published successfully!",
            onDismiss = {}
        )
    }
}
