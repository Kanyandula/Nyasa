package com.kanyandula.nyasa.ui.components.htmlrenderer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
internal fun ImageBlock(
    src: String,
    alt: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = src,
        contentDescription = alt,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.FillWidth,
        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceContainerHigh),
        error = ColorPainter(MaterialTheme.colorScheme.errorContainer)
    )
}
