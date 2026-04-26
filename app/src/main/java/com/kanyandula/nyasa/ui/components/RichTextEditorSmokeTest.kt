package com.kanyandula.nyasa.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor

private const val SAMPLE_HTML = """
    <p>Perched high on the breathtaking Zomba Plateau, <strong>Kuchawe Inn</strong> is one of the iconic places to stay.</p>
    <h3>What Makes Kuchawe Special</h3>
    <p>Stunning views over the plains below — especially at sunrise.</p>
    <ul>
      <li>Cozy lodge-style accommodation</li>
      <li>Scenic terraces overlooking the valley</li>
    </ul>
"""

@Preview(showBackground = true)
@Composable
internal fun RichTextEditorSmokeTestPreview() {
    NyasaTheme {
        val state = rememberRichTextState()
        LaunchedEffect(Unit) { state.setHtml(SAMPLE_HTML.trimIndent()) }
        RichTextEditor(
            state = state,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp)
        )
    }
}
