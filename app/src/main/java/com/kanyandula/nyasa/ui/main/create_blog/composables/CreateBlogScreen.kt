@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog.composables

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanyandula.nyasa.models.Category
import com.kanyandula.nyasa.ui.components.ImagePickerBox
import com.kanyandula.nyasa.ui.components.LoadingOverlay
import com.kanyandula.nyasa.ui.components.NyasaCategoryDropdown
import com.kanyandula.nyasa.ui.components.NyasaTextField
import com.kanyandula.nyasa.ui.components.NyasaTopBar
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBlogScreen(
    initialTitle: String,
    initialBody: String,
    imageUri: Uri?,
    selectedCategory: String?,
    initialTags: String,
    categories: List<Category>,
    isLoading: Boolean,
    onPublish: (title: String, body: String, tags: String) -> Unit,
    onPickImage: () -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var body by rememberSaveable { mutableStateOf(initialBody) }
    var tags by rememberSaveable { mutableStateOf(initialTags) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                NyasaTopBar(
                    title = "Draft",
                    navigationIcon = Icons.Filled.Close,
                    onNavigationClick = { /* navigate back */ },
                    actions = {
                        TextButton(onClick = { }) {
                            Text(
                                text = "Preview",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme
                                    .onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { onPublish(title, body, tags) },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary
                            ),
                            contentPadding = PaddingValues(
                                horizontal = 20.dp,
                                vertical = 8.dp
                            )
                        ) {
                            Text(
                                text = "Publish",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                )
            },
            bottomBar = {
                FormattingToolbar()
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                ImagePickerBox(
                    imageUri = imageUri,
                    onPickImage = onPickImage
                )
                Spacer(Modifier.height(24.dp))

                // Story Title
                Text(
                    text = "STORY TITLE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(8.dp))
                NyasaTextField(
                    value = title,
                    onValueChange = {
                        if (it.length <= MAX_TITLE_LENGTH) title = it
                    },
                    label = "Enter a captivating headline",
                    helperText = "${title.length} / $MAX_TITLE_LENGTH",
                    singleLine = false,
                    maxLines = 3,
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(16.dp))

                if (categories.isNotEmpty()) {
                    NyasaCategoryDropdown(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = onCategorySelected
                    )
                    Spacer(Modifier.height(16.dp))
                }

                NyasaTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = "Tags",
                    helperText = "Comma-separated (e.g. Travel, Culture)",
                    singleLine = true,
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(16.dp))

                // Your Story
                Text(
                    text = "YOUR STORY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(8.dp))
                NyasaTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = "Tell your story from the heart of Malawi...",
                    singleLine = false,
                    maxLines = Int.MAX_VALUE,
                    imeAction = ImeAction.Default
                )
                Spacer(Modifier.height(32.dp))
            }
        }
        LoadingOverlay(isLoading = isLoading)
    }
}

@Composable
private fun FormattingToolbar() {
    Column {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FormatButton(Icons.Filled.FormatBold, "Bold")
            FormatButton(Icons.Filled.FormatItalic, "Italic")
            FormatButton(Icons.Filled.Link, "Link")
            FormatButton(Icons.AutoMirrored.Filled.FormatListBulleted, "List")
            FormatButton(Icons.Filled.FormatQuote, "Quote")
            FormatButton(Icons.Filled.Image, "Image")
        }
    }
}

@Composable
private fun FormatButton(icon: ImageVector, description: String) {
    IconButton(onClick = { }) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
            selectedCategory = null,
            initialTags = "",
            categories = listOf(
                Category(1, "Travel", "travel"),
                Category(2, "Culture", "culture")
            ),
            isLoading = false,
            onPublish = { _, _, _ -> },
            onPickImage = {},
            onCategorySelected = {}
        )
    }
}
