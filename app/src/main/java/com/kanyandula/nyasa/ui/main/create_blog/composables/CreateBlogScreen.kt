@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog.composables

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kanyandula.nyasa.ui.components.ImagePickerBox
import com.kanyandula.nyasa.ui.components.LoadingOverlay
import com.kanyandula.nyasa.ui.components.NyasaButton
import com.kanyandula.nyasa.ui.components.NyasaTextField
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@Composable
fun CreateBlogScreen(
    initialTitle: String,
    initialBody: String,
    imageUri: Uri?,
    isLoading: Boolean,
    onPublish: (title: String, body: String) -> Unit,
    onPickImage: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var body by rememberSaveable { mutableStateOf(initialBody) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            ImagePickerBox(
                imageUri = imageUri,
                onPickImage = onPickImage
            )
            Spacer(Modifier.height(24.dp))

            NyasaTextField(
                value = title,
                onValueChange = { if (it.length <= MAX_TITLE_LENGTH) title = it },
                label = "Story Title",
                helperText = "${title.length} / $MAX_TITLE_LENGTH",
                singleLine = false,
                maxLines = 3,
                imeAction = ImeAction.Next
            )
            Spacer(Modifier.height(16.dp))

            NyasaTextField(
                value = body,
                onValueChange = { body = it },
                label = "Your Story",
                singleLine = false,
                maxLines = Int.MAX_VALUE,
                imeAction = ImeAction.Default
            )
            Spacer(Modifier.height(32.dp))

            NyasaButton(
                text = "Publish",
                onClick = { onPublish(title, body) }
            )
        }
        LoadingOverlay(isLoading = isLoading)
    }
}

private const val MAX_TITLE_LENGTH = 60

@Preview(showSystemUi = true)
@Composable
private fun CreateBlogScreenPreview() {
    NyasaTheme {
        CreateBlogScreen(
            initialTitle = "",
            initialBody = "",
            imageUri = null,
            isLoading = false,
            onPublish = { _, _ -> },
            onPickImage = {}
        )
    }
}
