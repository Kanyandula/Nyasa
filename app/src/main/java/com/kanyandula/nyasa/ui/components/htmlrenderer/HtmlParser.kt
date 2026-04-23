package com.kanyandula.nyasa.ui.components.htmlrenderer

import androidx.compose.ui.graphics.Color
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object HtmlParser {

    fun parse(
        html: String,
        linkColor: Color,
        codeBackground: Color
    ): List<BlockNode> {
        val document = Jsoup.parse(html)
        val body = document.body()
        return parseChildren(body, linkColor, codeBackground)
    }

    private fun parseChildren(
        parent: Element,
        linkColor: Color,
        codeBackground: Color
    ): List<BlockNode> {
        val blocks = mutableListOf<BlockNode>()

        for (child in parent.children()) {
            val node = parseElement(child, linkColor, codeBackground)
            if (node != null) blocks.add(node)
        }

        if (blocks.isEmpty() && parent.hasText()) {
            val text = InlineRenderer.render(
                parent,
                linkColor,
                codeBackground
            )
            if (text.isNotBlank()) {
                blocks.add(BlockNode.Paragraph(text))
            }
        }

        return blocks
    }

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    private fun parseElement(
        element: Element,
        linkColor: Color,
        codeBackground: Color
    ): BlockNode? {
        return when (element.tagName().lowercase()) {
            "h1" -> heading(element, 1, linkColor, codeBackground)
            "h2" -> heading(element, 2, linkColor, codeBackground)
            "h3" -> heading(element, 3, linkColor, codeBackground)
            "h4" -> heading(element, 4, linkColor, codeBackground)
            "h5" -> heading(element, 5, linkColor, codeBackground)
            "h6" -> heading(element, 6, linkColor, codeBackground)

            "p", "div", "section", "article" -> {
                if (hasOnlyImage(element)) {
                    parseImage(element.selectFirst("img"))
                } else {
                    val content = InlineRenderer.render(
                        element,
                        linkColor,
                        codeBackground
                    )
                    if (content.isNotBlank()) {
                        BlockNode.Paragraph(content)
                    } else {
                        null
                    }
                }
            }

            "blockquote" -> {
                val children = parseChildren(
                    element,
                    linkColor,
                    codeBackground
                )
                if (children.isNotEmpty()) {
                    BlockNode.Blockquote(children)
                } else {
                    null
                }
            }

            "pre" -> {
                val codeEl = element.selectFirst("code")
                val code = codeEl?.wholeText() ?: element.wholeText()
                val lang = codeEl
                    ?.className()
                    ?.removePrefix("language-")
                    ?.takeIf { it.isNotBlank() }
                BlockNode.CodeBlock(code = code, language = lang)
            }

            "ul" -> {
                val items = element.children()
                    .filter { it.tagName() == "li" }
                    .map { li ->
                        BlockNode.ListItem(
                            InlineRenderer.render(
                                li,
                                linkColor,
                                codeBackground
                            )
                        )
                    }
                if (items.isNotEmpty()) {
                    BlockNode.UnorderedList(items)
                } else {
                    null
                }
            }

            "ol" -> {
                val items = element.children()
                    .filter { it.tagName() == "li" }
                    .map { li ->
                        BlockNode.ListItem(
                            InlineRenderer.render(
                                li,
                                linkColor,
                                codeBackground
                            )
                        )
                    }
                if (items.isNotEmpty()) {
                    BlockNode.OrderedList(items)
                } else {
                    null
                }
            }

            "img" -> parseImage(element)

            "hr" -> BlockNode.HorizontalRule()

            "iframe", "table" -> {
                BlockNode.WebViewFallback(
                    html = element.outerHtml()
                )
            }

            else -> {
                val content = InlineRenderer.render(
                    element,
                    linkColor,
                    codeBackground
                )
                if (content.isNotBlank()) {
                    BlockNode.Paragraph(content)
                } else {
                    null
                }
            }
        }
    }

    private fun heading(
        element: Element,
        level: Int,
        linkColor: Color,
        codeBackground: Color
    ): BlockNode.Heading {
        val content = InlineRenderer.render(
            element,
            linkColor,
            codeBackground
        )
        return BlockNode.Heading(level = level, content = content)
    }

    private fun hasOnlyImage(element: Element): Boolean {
        val children = element.children()
        return children.size == 1 &&
            children.first()?.tagName() == "img" &&
            element.ownText().isBlank()
    }

    private fun parseImage(element: Element?): BlockNode.Image? {
        val src = element?.attr("src") ?: return null
        if (src.isBlank()) return null
        return BlockNode.Image(
            src = src,
            alt = element.attr("alt").takeIf { it.isNotBlank() }
        )
    }
}
