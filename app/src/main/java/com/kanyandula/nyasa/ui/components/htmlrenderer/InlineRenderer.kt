package com.kanyandula.nyasa.ui.components.htmlrenderer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

object InlineRenderer {

    fun render(
        element: Element,
        linkColor: Color,
        codeBackground: Color
    ): AnnotatedString = buildAnnotatedString {
        renderChildren(element, linkColor, codeBackground)
    }

    private fun AnnotatedString.Builder.renderChildren(
        element: Element,
        linkColor: Color,
        codeBackground: Color
    ) {
        for (node in element.childNodes()) {
            when (node) {
                is TextNode -> append(node.wholeText)
                is Element -> renderElement(node, linkColor, codeBackground)
            }
        }
    }

    private fun AnnotatedString.Builder.renderElement(
        element: Element,
        linkColor: Color,
        codeBackground: Color
    ) {
        when (element.tagName().lowercase()) {
            "b", "strong" -> {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                renderChildren(element, linkColor, codeBackground)
                pop()
            }
            "i", "em" -> {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                renderChildren(element, linkColor, codeBackground)
                pop()
            }
            "u" -> {
                pushStyle(
                    SpanStyle(
                        textDecoration = TextDecoration.Underline
                    )
                )
                renderChildren(element, linkColor, codeBackground)
                pop()
            }
            "a" -> {
                val href = element.attr("href")
                if (href.isNotBlank()) {
                    pushLink(LinkAnnotation.Url(href))
                    pushStyle(
                        SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                    renderChildren(element, linkColor, codeBackground)
                    pop()
                    pop()
                } else {
                    renderChildren(element, linkColor, codeBackground)
                }
            }
            "code" -> {
                pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = codeBackground
                    )
                )
                renderChildren(element, linkColor, codeBackground)
                pop()
            }
            "br" -> append("\n")
            "span" -> renderChildren(element, linkColor, codeBackground)
            else -> renderChildren(element, linkColor, codeBackground)
        }
    }
}
