package com.kanyandula.nyasa.work

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MarkdownExtTest {

    @Test
    fun `bold markdown becomes strong tag`() {
        assertThat("**bold**".markdownToHtml().trim())
            .isEqualTo("<p><strong>bold</strong></p>")
    }

    @Test
    fun `italic markdown becomes em tag`() {
        assertThat("*italic*".markdownToHtml().trim())
            .isEqualTo("<p><em>italic</em></p>")
    }

    @Test
    fun `bullet list becomes ul`() {
        val html = "- one\n- two".markdownToHtml().trim()
        assertThat(html).contains("<ul>")
        assertThat(html).contains("<li>one</li>")
        assertThat(html).contains("<li>two</li>")
    }

    @Test
    fun `blockquote becomes blockquote tag`() {
        assertThat("> quoted".markdownToHtml().trim())
            .contains("<blockquote>")
    }

    @Test
    fun `link markdown becomes anchor tag`() {
        assertThat("[Nyasa](https://nyasablog.com)".markdownToHtml().trim())
            .isEqualTo("<p><a href=\"https://nyasablog.com\">Nyasa</a></p>")
    }

    @Test
    fun `plain prose with double newline produces paragraphs`() {
        val html = "First paragraph.\n\nSecond paragraph.".markdownToHtml().trim()
        assertThat(html).contains("<p>First paragraph.</p>")
        assertThat(html).contains("<p>Second paragraph.</p>")
    }

    @Test
    fun `empty input passes through unchanged`() {
        assertThat("".markdownToHtml()).isEqualTo("")
    }

    @Test
    fun `inline raw script tag is escaped, not executed`() {
        // commonmark default config escapes raw HTML — basic XSS safety net.
        val html = "<script>alert(1)</script>".markdownToHtml()
        assertThat(html).doesNotContain("<script>")
    }

    @Test
    fun `htmlToMarkdown converts strong tag to bold markdown`() {
        assertThat("<p><strong>bold</strong></p>".htmlToMarkdown())
            .isEqualTo("**bold**")
    }

    @Test
    fun `htmlToMarkdown converts ul to bullet list`() {
        // flexmark emits `*` as the bullet marker; commonmark accepts both
        // `*` and `-`, so the round-trip through markdownToHtml is stable.
        val md = "<ul><li>one</li><li>two</li></ul>".htmlToMarkdown()
        assertThat(md).matches(Regex("""[*-] one\n[*-] two""").pattern)
    }

    @Test
    fun `htmlToMarkdown converts anchor to link`() {
        assertThat("<a href=\"https://nyasablog.com\">Nyasa</a>".htmlToMarkdown())
            .isEqualTo("[Nyasa](https://nyasablog.com)")
    }

    @Test
    fun `htmlToMarkdown empty input passes through`() {
        assertThat("".htmlToMarkdown()).isEqualTo("")
    }

    @Test
    fun `markdown round-trips through HTML and back`() {
        // The edit flow: load HTML from server → htmlToMarkdown → user edits →
        // markdownToHtml → save. Verify a representative post survives.
        val originalMarkdown = """
            # Heading

            Some **bold** and *italic* text.

            - first item
            - second item

            > A blockquote.

            [A link](https://example.com)
        """.trimIndent()

        val asHtml = originalMarkdown.markdownToHtml()
        val backToMarkdown = asHtml.htmlToMarkdown()
        val asHtmlAgain = backToMarkdown.markdownToHtml()

        // Round-trip should be stable on the second pass: once we've gone
        // markdown → html → markdown, going markdown → html again should match
        // the first html (i.e. flexmark's markdown output is well-formed for
        // commonmark to re-parse identically).
        assertThat(asHtmlAgain).isEqualTo(asHtml)
    }
}