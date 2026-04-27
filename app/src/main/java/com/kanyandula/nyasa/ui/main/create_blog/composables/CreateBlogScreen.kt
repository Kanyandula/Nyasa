@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
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
import com.kanyandula.nyasa.ui.components.richtext.RichTextToolbarOverlay
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.util.BlogUtils
import com.kanyandula.nyasa.util.analytics.TrackScreen
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBlogScreen(
    initialTitle: String,
    bodyState: RichTextState,
    imageModel: Any?,
    selectedCategory: String?,
    initialTags: String,
    categories: List<Category>,
    isLoading: Boolean,
    onPublish: (title: String, body: String, tags: String) -> Unit,
    onPickImage: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onNavigateBack: () -> Unit,
    onSaveDraft: () -> Unit
) {
    TrackScreen("CreateBlog")
    var title by rememberSaveable(initialTitle) { mutableStateOf(initialTitle) }
    var tags by rememberSaveable(initialTags) { mutableStateOf(initialTags) }
    var isEditorFocused by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                NyasaTopBar(
                    title = "Draft",
                    navigationIcon = Icons.Filled.Close,
                    onNavigationClick = onNavigateBack,
                    actions = {
                        TextButton(onClick = { }) {
                            Text(
                                text = "Preview",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            },
            bottomBar = {
                Column {
                    RichTextToolbarOverlay(
                        state = bodyState,
                        isVisible = isEditorFocused,
                        modifier = Modifier.fillMaxWidth()
                    )
                    PublishBar(
                        isLoading = isLoading,
                        onSaveDraft = onSaveDraft,
                        onPublish = { onPublish(title, bodyState.toHtml(), tags) }
                    )
                }
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
                    onPickImage = onPickImage
                )
                Spacer(Modifier.height(NyasaTheme.spacing.s))
                Text(
                    text = "High resolution (16:9) recommended",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(NyasaTheme.spacing.l))

                SectionLabel("STORY TITLE")
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
                Spacer(Modifier.height(NyasaTheme.spacing.l))

                SectionLabel("YOUR STORY")
                RichTextEditor(
                    state = bodyState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp)
                        .onFocusChanged { isEditorFocused = it.isFocused }
                )
                Spacer(Modifier.height(NyasaTheme.spacing.l))

                if (categories.isNotEmpty()) {
                    SectionLabel("CATEGORY")
                    NyasaCategoryDropdown(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = onCategorySelected
                    )
                    Spacer(Modifier.height(NyasaTheme.spacing.l))
                }

                SectionLabel("TAGS")
                TagsChipPicker(
                    tagsString = tags,
                    onTagsChanged = { tags = it }
                )
                Spacer(Modifier.height(NyasaTheme.spacing.xl))
            }
        }
        LoadingOverlay(isLoading = isLoading)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.5.sp),
        color = MaterialTheme.colorScheme.secondary
    )
    Spacer(Modifier.height(NyasaTheme.spacing.s))
}

@Composable
private fun PublishBar(
    isLoading: Boolean,
    onSaveDraft: () -> Unit,
    onPublish: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = NyasaTheme.spacing.l, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSaveDraft,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = "Save Draft",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Button(
                    onClick = onPublish,
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = "Publish",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TagsChipPicker(
    tagsString: String,
    onTagsChanged: (String) -> Unit
) {
    val (allChips, selectedLowercase) = remember(tagsString) {
        val parsed = parseTagsCsv(tagsString)
        val suggestedLower = SUGGESTED_TAGS.map { it.lowercase() }.toSet()
        val extras = parsed.filter { it.lowercase() !in suggestedLower }
        val chips = SUGGESTED_TAGS + extras
        val selected = parsed.map { it.lowercase() }.toSet()
        chips to selected
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.s),
        verticalArrangement = Arrangement.spacedBy(NyasaTheme.spacing.s)
    ) {
        allChips.forEach { tag ->
            val isSelected = tag.lowercase() in selectedLowercase
            FilterChip(
                selected = isSelected,
                onClick = {
                    val current = parseTagsCsv(tagsString)
                    val newTags = if (isSelected) {
                        current.filter { !it.equals(tag, ignoreCase = true) }
                    } else {
                        current + tag
                    }
                    onTagsChanged(newTags.joinToString(", "))
                },
                label = { Text("#$tag") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

private fun parseTagsCsv(csv: String): List<String> =
    BlogUtils.parseTags(csv)

private val SUGGESTED_TAGS = listOf(
    "Malawi",
    "Travel",
    "Photography",
    "Culture",
    "Food",
    "Tech"
)

private const val MAX_TITLE_LENGTH = 60

@Preview(showSystemUi = true)
@Composable
private fun CreateBlogScreenPreview() {
    NyasaTheme {
        CreateBlogScreen(
            initialTitle = "",
            bodyState = rememberRichTextState(),
            imageModel = null,
            selectedCategory = null,
            initialTags = "",
            categories = listOf(
                Category(1, "Travel", "travel"),
                Category(2, "Culture", "culture")
            ),
            isLoading = false,
            onPublish = { _, _, _ -> },
            onPickImage = {},
            onCategorySelected = {},
            onNavigateBack = {},
            onSaveDraft = {}
        )
    }
}
