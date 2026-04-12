@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog.composables

import android.net.Uri
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
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
import com.kanyandula.nyasa.util.BlogUtils

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
    onCategorySelected: (String?) -> Unit,
    onNavigateBack: () -> Unit,
    onSaveDraft: () -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var body by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(initialBody))
    }
    var tags by rememberSaveable { mutableStateOf(initialTags) }

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
                PublishBar(
                    isLoading = isLoading,
                    onSaveDraft = onSaveDraft,
                    onPublish = { onPublish(title, body.text, tags) }
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
                    onPickImage = onPickImage
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "High resolution (16:9) recommended",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))

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
                Spacer(Modifier.height(24.dp))

                SectionLabel("YOUR STORY")
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("Tell your story from the heart of Malawi...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = Int.MAX_VALUE,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                    shape = MaterialTheme.shapes.small,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = Primary,
                        cursorColor = Primary
                    )
                )
                FormattingToolbar(
                    onFormat = { transform -> body = transform(body) }
                )
                Spacer(Modifier.height(24.dp))

                if (categories.isNotEmpty()) {
                    SectionLabel("CATEGORY")
                    NyasaCategoryDropdown(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = onCategorySelected
                    )
                    Spacer(Modifier.height(24.dp))
                }

                SectionLabel("TAGS")
                TagsChipPicker(
                    tagsString = tags,
                    onTagsChanged = { tags = it }
                )
                Spacer(Modifier.height(32.dp))
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
    Spacer(Modifier.height(8.dp))
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
                    .padding(horizontal = 24.dp, vertical = 12.dp),
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
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
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

@Composable
private fun FormattingToolbar(
    onFormat: ((TextFieldValue) -> TextFieldValue) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FormatButton(Icons.Filled.FormatBold, "Bold") {
            onFormat { wrapSelection(it, "**") }
        }
        FormatButton(Icons.Filled.FormatItalic, "Italic") {
            onFormat { wrapSelection(it, "*") }
        }
        FormatButton(Icons.Filled.Link, "Link") {
            onFormat { insertAtCursor(it, "[link text](https://)", cursorOffset = 1) }
        }
        FormatButton(Icons.AutoMirrored.Filled.FormatListBulleted, "List") {
            onFormat { prefixLines(it, "- ") }
        }
        FormatButton(Icons.Filled.FormatQuote, "Quote") {
            onFormat { prefixLines(it, "> ") }
        }
        FormatButton(Icons.Filled.Image, "Image") {
            onFormat { insertAtCursor(it, "![alt](https://)", cursorOffset = 2) }
        }
    }
}

@Composable
private fun FormatButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun wrapSelection(
    value: TextFieldValue,
    prefix: String,
    suffix: String = prefix
): TextFieldValue {
    val text = value.text
    val start = value.selection.start
    val end = value.selection.end
    val before = text.substring(0, start)
    val selected = text.substring(start, end)
    val after = text.substring(end)
    val newText = before + prefix + selected + suffix + after
    val newSelection = if (value.selection.collapsed) {
        TextRange(start + prefix.length)
    } else {
        TextRange(start + prefix.length, start + prefix.length + selected.length)
    }
    return value.copy(text = newText, selection = newSelection)
}

private fun prefixLines(value: TextFieldValue, linePrefix: String): TextFieldValue {
    val text = value.text
    val selStart = value.selection.start
    val selEnd = value.selection.end
    val lineStart = text.lastIndexOf('\n', (selStart - 1).coerceAtLeast(0)).let {
        if (it < 0) 0 else it + 1
    }
    val lineEnd = text.indexOf('\n', selEnd).let {
        if (it < 0) text.length else it
    }
    val before = text.substring(0, lineStart)
    val block = text.substring(lineStart, lineEnd)
    val after = text.substring(lineEnd)
    val modified = if (block.isEmpty()) {
        linePrefix
    } else {
        block.lines().joinToString("\n") { linePrefix + it }
    }
    val newText = before + modified + after
    return value.copy(
        text = newText,
        selection = TextRange(lineStart, lineStart + modified.length)
    )
}

private fun insertAtCursor(
    value: TextFieldValue,
    insertion: String,
    cursorOffset: Int = insertion.length
): TextFieldValue {
    val text = value.text
    val start = value.selection.start
    val end = value.selection.end
    val newText = text.substring(0, start) + insertion + text.substring(end)
    return value.copy(
        text = newText,
        selection = TextRange(start + cursorOffset)
    )
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    selectedContainerColor = Primary,
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
            onCategorySelected = {},
            onNavigateBack = {},
            onSaveDraft = {}
        )
    }
}
