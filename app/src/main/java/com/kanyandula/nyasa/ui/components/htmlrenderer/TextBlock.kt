package com.kanyandula.nyasa.ui.components.htmlrenderer

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle

@Composable
internal fun TextBlock(
    content: AnnotatedString,
    style: TextStyle,
    modifier: Modifier = Modifier
) {
    Text(
        text = content,
        style = style,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

@Composable
internal fun headingStyle(level: Int): TextStyle = when (level) {
    1 -> MaterialTheme.typography.headlineLarge
    2 -> MaterialTheme.typography.headlineMedium
    3 -> MaterialTheme.typography.headlineSmall
    4 -> MaterialTheme.typography.titleLarge
    5 -> MaterialTheme.typography.titleMedium
    else -> MaterialTheme.typography.titleSmall
}
