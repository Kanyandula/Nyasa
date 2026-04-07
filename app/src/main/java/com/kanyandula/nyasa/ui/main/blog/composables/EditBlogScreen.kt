package com.kanyandula.nyasa.ui.main.blog.composables

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kanyandula.nyasa.models.Category
import com.kanyandula.nyasa.ui.components.ImagePickerBox
import com.kanyandula.nyasa.ui.components.LoadingOverlay
import com.kanyandula.nyasa.ui.components.NyasaCategoryDropdown
import com.kanyandula.nyasa.ui.components.NyasaTextField
import com.kanyandula.nyasa.ui.components.NyasaTopBar
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBlogScreen(
    initialTitle: String,
    initialBody: String,
    imageUri: Uri?,
    selectedCategory: String?,
    initialTags: String,
    categories: List<Category>,
    isLoading: Boolean,
    onSave: (title: String, body: String, tags: String) -> Unit,
    onPickImage: () -> Unit,
    onNavigateBack: () -> Unit,
    onCategorySelected: (String?) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var body by rememberSaveable { mutableStateOf(initialBody) }
    var tags by rememberSaveable { mutableStateOf(initialTags) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                NyasaTopBar(
                    title = "Edit Story",
                    navigationIcon = Icons.Filled.Close,
                    onNavigationClick = onNavigateBack,
                    actions = {
                        IconButton(
                            onClick = { onSave(title, body, tags) },
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Save"
                            )
                        }
                    }
                )
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
                    onPickImage = onPickImage,
                    showPlaceholderText = false
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Tap to change image",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))

                NyasaTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Story Title",
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

                NyasaTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = "Your Story",
                    singleLine = false,
                    maxLines = Int.MAX_VALUE,
                    imeAction = ImeAction.Default
                )
            }
        }
        LoadingOverlay(isLoading = isLoading)
    }
}

@Preview(showSystemUi = true)
@Composable
private fun EditBlogScreenPreview() {
    NyasaTheme {
        EditBlogScreen(
            initialTitle = "Whispers of the Lake",
            initialBody = "The sun hung low over the Dedza mountains...",
            imageUri = null,
            selectedCategory = "travel",
            initialTags = "Malawi, Travel",
            categories = listOf(
                Category(1, "Travel", "travel"),
                Category(2, "Culture", "culture")
            ),
            isLoading = false,
            onSave = { _, _, _ -> },
            onPickImage = {},
            onNavigateBack = {},
            onCategorySelected = {}
        )
    }
}
