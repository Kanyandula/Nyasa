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
}