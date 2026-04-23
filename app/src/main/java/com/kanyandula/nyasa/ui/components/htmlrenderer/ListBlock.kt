package com.kanyandula.nyasa.ui.components.htmlrenderer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@Composable
internal fun UnorderedListBlock(
    items: List<BlockNode.ListItem>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        items.forEach { item ->
            Row(
                modifier = Modifier.padding(
                    bottom = NyasaTheme.spacing.xs
                ),
            ) {
                Text(
                    text = "\u2022",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(20.dp),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
internal fun OrderedListBlock(
    items: List<BlockNode.ListItem>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.padding(
                    bottom = NyasaTheme.spacing.xs
                ),
            ) {
                Text(
                    text = "${index + 1}.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.width(24.dp),
                    textAlign = TextAlign.End,
                )
                Text(
                    text = item.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = NyasaTheme.spacing.xs),
                )
            }
        }
    }
}
