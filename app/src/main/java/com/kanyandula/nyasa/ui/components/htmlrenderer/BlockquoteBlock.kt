package com.kanyandula.nyasa.ui.components.htmlrenderer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@Composable
internal fun BlockquoteBlock(
    children: List<BlockNode>,
    modifier: Modifier = Modifier,
) {
    val borderColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Box(
        modifier = modifier
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 4.dp.toPx(),
                )
            }
            .background(
                color = bgColor,
                shape = MaterialTheme.shapes.small,
            )
            .padding(
                start = NyasaTheme.spacing.m,
                top = NyasaTheme.spacing.s,
                end = NyasaTheme.spacing.m,
                bottom = NyasaTheme.spacing.s,
            ),
    ) {
        Column {
            children.forEach { node ->
                RenderBlockNode(node)
            }
        }
    }
}
