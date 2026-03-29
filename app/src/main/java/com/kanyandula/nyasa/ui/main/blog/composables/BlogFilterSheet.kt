package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kanyandula.nyasa.persistance.BlogQueryUtils.BLOG_FILTER_DATE_UPDATED
import com.kanyandula.nyasa.persistance.BlogQueryUtils.BLOG_FILTER_USERNAME
import com.kanyandula.nyasa.persistance.BlogQueryUtils.BLOG_ORDER_ASC
import com.kanyandula.nyasa.persistance.BlogQueryUtils.BLOG_ORDER_DESC
import com.kanyandula.nyasa.ui.components.ButtonStyle
import com.kanyandula.nyasa.ui.components.NyasaButton
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogFilterSheet(
    currentFilter: String,
    currentOrder: String,
    onApply: (filter: String, order: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(currentFilter) }
    var selectedOrder by remember { mutableStateOf(currentOrder) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Text(
                text = "Filter stories",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Customize your reading experience",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Filter by",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Column(modifier = Modifier.selectableGroup()) {
                FilterRadioOption(
                    text = "Date",
                    selected = selectedFilter == BLOG_FILTER_DATE_UPDATED,
                    onClick = { selectedFilter = BLOG_FILTER_DATE_UPDATED }
                )
                FilterRadioOption(
                    text = "Author",
                    selected = selectedFilter == BLOG_FILTER_USERNAME,
                    onClick = { selectedFilter = BLOG_FILTER_USERNAME }
                )
            }
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Order",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Column(modifier = Modifier.selectableGroup()) {
                FilterRadioOption(
                    text = "Ascending",
                    selected = selectedOrder == BLOG_ORDER_ASC,
                    onClick = { selectedOrder = BLOG_ORDER_ASC }
                )
                FilterRadioOption(
                    text = "Descending",
                    selected = selectedOrder == BLOG_ORDER_DESC,
                    onClick = { selectedOrder = BLOG_ORDER_DESC }
                )
            }
            Spacer(Modifier.height(24.dp))

            NyasaButton(
                text = "Apply filters",
                onClick = { onApply(selectedFilter, selectedOrder) }
            )
            Spacer(Modifier.height(8.dp))
            NyasaButton(
                text = "Cancel",
                onClick = onDismiss,
                style = ButtonStyle.Secondary
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FilterRadioOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Preview
@Composable
private fun BlogFilterSheetPreview() {
    NyasaTheme {
        BlogFilterSheet(
            currentFilter = BLOG_FILTER_DATE_UPDATED,
            currentOrder = BLOG_ORDER_ASC,
            onApply = { _, _ -> },
            onDismiss = {}
        )
    }
}
