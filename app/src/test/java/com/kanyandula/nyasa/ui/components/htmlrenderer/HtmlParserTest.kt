package com.kanyandula.nyasa.ui.components.htmlrenderer

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlParserTest {

    private val linkColor = Color.Blue
    private val codeBg = Color.LightGray

    private fun parse(html: String) =
        HtmlParser.parse(html, linkColor, codeBg)

    @Test
    fun `paragraph renders as Paragraph node`() {
        val blocks = parse("<p>Hello world</p>")
        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is BlockNode.Paragraph)
        assertEquals(
            "Hello world",
            (blocks[0] as BlockNode.Paragraph).content.text
        )
    }

    @Test
    fun `headings render with correct level`() {
        val blocks = parse("<h1>Title</h1><h3>Subtitle</h3>")
        assertEquals(2, blocks.size)
        assertEquals(1, (blocks[0] as BlockNode.Heading).level)
        assertEquals(3, (blocks[1] as BlockNode.Heading).level)
    }

    @Test
    fun `unordered list renders items`() {
        val blocks = parse("<ul><li>One</li><li>Two</li></ul>")
        assertEquals(1, blocks.size)
        val list = blocks[0] as BlockNode.UnorderedList
        assertEquals(2, list.items.size)
        assertEquals("One", list.items[0].content.text)
        assertEquals("Two", list.items[1].content.text)
    }

    @Test
    fun `ordered list renders items`() {
        val blocks = parse("<ol><li>First</li><li>Second</li></ol>")
        assertEquals(1, blocks.size)
        val list = blocks[0] as BlockNode.OrderedList
        assertEquals(2, list.items.size)
    }

    @Test
    fun `blockquote renders children`() {
        val blocks = parse(
            "<blockquote><p>Quote text</p></blockquote>"
        )
        assertEquals(1, blocks.size)
        val bq = blocks[0] as BlockNode.Blockquote
        assertEquals(1, bq.children.size)
        assertTrue(bq.children[0] is BlockNode.Paragraph)
    }

    @Test
    fun `code block renders code text`() {
        val blocks = parse("<pre><code>val x = 1</code></pre>")
        assertEquals(1, blocks.size)
        val code = blocks[0] as BlockNode.CodeBlock
        assertEquals("val x = 1", code.code)
    }

    @Test
    fun `img renders as Image node`() {
        val blocks = parse(
            """<img src="https://example.com/img.jpg" alt="Photo">"""
        )
        assertEquals(1, blocks.size)
        val img = blocks[0] as BlockNode.Image
        assertEquals("https://example.com/img.jpg", img.src)
        assertEquals("Photo", img.alt)
    }

    @Test
    fun `iframe renders as WebViewFallback`() {
        val blocks = parse(
            """<iframe src="https://youtube.com/embed/abc"></iframe>"""
        )
        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is BlockNode.WebViewFallback)
    }

    @Test
    fun `table renders as WebViewFallback`() {
        val blocks = parse(
            "<table><tr><td>Cell</td></tr></table>"
        )
        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is BlockNode.WebViewFallback)
    }

    @Test
    fun `hr renders as HorizontalRule`() {
        val blocks = parse("<p>Above</p><hr><p>Below</p>")
        assertEquals(3, blocks.size)
        assertTrue(blocks[1] is BlockNode.HorizontalRule)
    }

    @Test
    fun `mixed content parses correctly`() {
        val html = """
            <h2>Title</h2>
            <p>Some <b>bold</b> text</p>
            <ul><li>Item</li></ul>
            <img src="https://example.com/img.jpg" alt="">
        """.trimIndent()
        val blocks = parse(html)
        assertEquals(4, blocks.size)
        assertTrue(blocks[0] is BlockNode.Heading)
        assertTrue(blocks[1] is BlockNode.Paragraph)
        assertTrue(blocks[2] is BlockNode.UnorderedList)
        assertTrue(blocks[3] is BlockNode.Image)
    }

    @Test
    fun `empty html returns empty list`() {
        val blocks = parse("")
        assertTrue(blocks.isEmpty())
    }

    @Test
    fun `plain text wraps in paragraph`() {
        val blocks = parse("Just plain text")
        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is BlockNode.Paragraph)
    }

    @Test
    fun `bold text preserves in paragraph content`() {
        val blocks = parse("<p>Hello <strong>world</strong></p>")
        val p = blocks[0] as BlockNode.Paragraph
        assertEquals("Hello world", p.content.text)
    }

    @Test
    fun `p wrapping only img becomes Image node`() {
        val blocks = parse(
            """<p><img src="https://example.com/a.jpg" alt=""></p>"""
        )
        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is BlockNode.Image)
    }
}
