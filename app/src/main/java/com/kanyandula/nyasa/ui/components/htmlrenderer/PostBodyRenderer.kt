package com.kanyandula.nyasa.ui.components.htmlrenderer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.kanyandula.nyasa.ui.theme.NyasaTheme

@Composable
fun PostBodyRenderer(
    html: String,
    modifier: Modifier = Modifier
) {
    if (html.isBlank()) return

    val linkColor = MaterialTheme.colorScheme.primary
    val codeBg = MaterialTheme.colorScheme.surfaceContainerHigh

    val blocks = remember(html, linkColor, codeBg) {
        HtmlParser.parse(html, linkColor, codeBg)
    }

    // Block spacing lives in layout, not markup — empty <p> tags don't render
    // and the rich-text editor strips them at serialization.
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            NyasaTheme.spacing.m
        )
    ) {
        blocks.forEach { node ->
            RenderBlockNode(node)
        }
    }
}

@Composable
internal fun RenderBlockNode(node: BlockNode) {
    when (node) {
        is BlockNode.Heading -> TextBlock(
            content = node.content,
            style = headingStyle(node.level),
            modifier = Modifier.padding(
                top = NyasaTheme.spacing.m
            )
        )
        is BlockNode.Paragraph -> TextBlock(
            content = node.content,
            style = MaterialTheme.typography.bodyLarge
        )
        is BlockNode.Blockquote -> BlockquoteBlock(
            children = node.children
        )
        is BlockNode.CodeBlock -> CodeBlockComposable(
            code = node.code
        )
        is BlockNode.UnorderedList -> UnorderedListBlock(
            items = node.items
        )
        is BlockNode.OrderedList -> OrderedListBlock(
            items = node.items
        )
        is BlockNode.Image -> ImageBlock(
            src = node.src,
            alt = node.alt
        )
        is BlockNode.WebViewFallback -> WebViewBlock(
            html = node.html
        )
        is BlockNode.HorizontalRule -> HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
