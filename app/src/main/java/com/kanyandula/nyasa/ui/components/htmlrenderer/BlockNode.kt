package com.kanyandula.nyasa.ui.components.htmlrenderer

import androidx.compose.ui.text.AnnotatedString

data class ListItem(
    val content: AnnotatedString
)

sealed interface BlockNode {

    data class Heading(
        val level: Int,
        val content: AnnotatedString
    ) : BlockNode

    data class Paragraph(
        val content: AnnotatedString
    ) : BlockNode

    data class Blockquote(
        val children: List<BlockNode>
    ) : BlockNode

    data class CodeBlock(
        val code: String,
        val language: String?
    ) : BlockNode

    data class UnorderedList(
        val items: List<ListItem>
    ) : BlockNode

    data class OrderedList(
        val items: List<ListItem>
    ) : BlockNode

    data class Image(
        val src: String,
        val alt: String?
    ) : BlockNode

    data class WebViewFallback(
        val html: String
    ) : BlockNode

    data object HorizontalRule : BlockNode
}
