package com.kanyandula.nyasa.ui.main.blog.composables

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kanyandula.nyasa.models.Category
import com.kanyandula.nyasa.ui.components.ImagePickerBox
import com.kanyandula.nyasa.ui.components.LoadingOverlay
import com.kanyandula.nyasa.ui.components.NyasaCategoryDropdown
import com.kanyandula.nyasa.ui.components.NyasaTextField
import com.kanyandula.nyasa.ui.components.NyasaTopBar
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.util.analytics.TrackScreen
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBlogScreen(
    initialTitle: String,
    initialBody: String,
    imageModel: Any?,
    selectedCategory: String?,
    initialTags: String,
    categories: List<Category>,
    isLoading: Boolean,
    onAction: (EditBlogAction) -> Unit
) {
    TrackScreen("EditBlog")
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var body by rememberSaveable { mutableStateOf(initialBody) }
    var tags by rememberSaveable { mutableStateOf(initialTags) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                NyasaTopBar(
                    title = "Edit Story",
                    navigationIcon = Icons.Filled.Close,
                    onNavigationClick = { onAction(EditBlogAction.NavigateBack) },
                    actions = {
                        Button(
                            onClick = {
                                onAction(EditBlogAction.Save(title, body, tags))
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(
                                horizontal = 20.dp,
                                vertical = 8.dp
                            )
                        ) {
                            Text(
                                text = "Save",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                )
            },
            bottomBar = {
                EditBottomToolbar()
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = NyasaTheme.spacing.l, vertical = NyasaTheme.spacing.m)
            ) {
                ImagePickerBox(
                    imageModel = imageModel,
                    onPickImage = { onAction(EditBlogAction.PickImage) },
                    showPlaceholderText = false
                )
                Spacer(Modifier.height(NyasaTheme.spacing.xs))
                Text(
                    text = "TAP TO CHANGE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(20.dp))

                // Article Headline
                Text(
                    text = "ARTICLE HEADLINE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(NyasaTheme.spacing.s))
                NyasaTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Story Title",
                    singleLine = false,
                    maxLines = 3,
                    imeAction = ImeAction.Next
                )
                Spacer(Modifier.height(20.dp))

                // Main Narrative
                Text(
                    text = "MAIN NARRATIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(NyasaTheme.spacing.s))
                NyasaTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = "Your Story",
                    singleLine = false,
                    maxLines = Int.MAX_VALUE,
                    imeAction = ImeAction.Default
                )
                Spacer(Modifier.height(20.dp))

                // Category + Reading Time row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CATEGORY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme
                                .onSurfaceVariant
                        )
                        Spacer(Modifier.height(NyasaTheme.spacing.s))
                        if (categories.isNotEmpty()) {
                            NyasaCategoryDropdown(
                                categories = categories,
                                selectedCategory = selectedCategory,
                                onCategorySelected = {
                                    onAction(EditBlogAction.CategorySelected(it))
                                }
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "READING TIME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme
                                .onSurfaceVariant
                        )
                        Spacer(Modifier.height(NyasaTheme.spacing.s))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement
                                .spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme
                                    .onSurfaceVariant
                            )
                            val readTime = remember(body) {
                                val wordCount = body
                                    .split("\\s+".toRegex())
                                    .count { it.isNotBlank() }
                                (wordCount / 200).coerceAtLeast(1)
                            }
                            Text(
                                text = "$readTime min",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Tags
                NyasaTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = "Tags",
                    helperText = "Comma-separated (e.g. Travel, Culture)",
                    singleLine = true,
                    imeAction = ImeAction.Done
                )
                Spacer(Modifier.height(NyasaTheme.spacing.m))
            }
        }
        LoadingOverlay(isLoading = isLoading)
    }
}

@Composable
private fun EditBottomToolbar() {
    Column {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = NyasaTheme.spacing.s, vertical = NyasaTheme.spacing.xs),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ToolbarItem(Icons.Filled.Image, "Media")
            ToolbarItem(Icons.Filled.LocalOffer, "Tags")
            ToolbarItem(Icons.Filled.Preview, "Preview")
            ToolbarItem(
                Icons.Filled.DeleteOutline,
                "Discard",
                tintError = true
            )
        }
    }
}

@Composable
private fun ToolbarItem(
    icon: ImageVector,
    label: String,
    tintError: Boolean = false
) {
    val color = if (tintError) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    TextButton(onClick = { }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@ComposePreview(showSystemUi = true)
@Composable
private fun EditBlogScreenPreview() {
    NyasaTheme {
        EditBlogScreen(
            initialTitle = "The Silent Wisdom of the Baobab:",
            initialBody = "The sun hung low over the Dedza mountains," +
                " painting the landscape in a shade of burnt sienna" +
                " and deep ochre. Under the sprawling branches of" +
                " an ancient baobab, time seems to slow down.",
            imageModel = null,
            selectedCategory = "culture",
            initialTags = "Malawi, Travel",
            categories = listOf(
                Category(1, "Travel", "travel"),
                Category(2, "Culture", "culture")
            ),
            isLoading = false,
            onAction = {}
        )
    }
}
