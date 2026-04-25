package com.kanyandula.nyasa.work

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

private val markdownParser: Parser = Parser.builder().build()

// escapeHtml(true) makes the renderer treat raw HTML in the source as literal
// text instead of passing it through — basic XSS safety against `<script>` etc.
// typed (or pasted) into the editor. The toolbar only produces markdown, so
// users shouldn't lose anything they intended.
private val htmlRenderer: HtmlRenderer = HtmlRenderer.builder()
    .escapeHtml(true)
    .build()

private val htmlToMarkdownConverter: FlexmarkHtmlConverter =
    FlexmarkHtmlConverter.builder().build()

/**
 * Converts markdown produced by the post editor's formatting toolbar
 * (`**bold**`, `- bullets`, `> quotes`, `[links](...)`, etc.) into HTML the
 * read-side `PostBodyRenderer` can render. Plain-text input passes through
 * with paragraph breaks (`\n\n`) becoming `<p>` blocks — fixing the case
 * where users type prose and see it rendered as one wall of text.
 */
fun String.markdownToHtml(): String {
    if (isEmpty()) return this
    return htmlRenderer.render(markdownParser.parse(this))
}

/**
 * Reverse of [markdownToHtml] for the edit flow: when an existing post is
 * loaded into the editor, its stored body is HTML — we convert back to
 * markdown so the user sees `**bold**` instead of `<strong>bold</strong>`,
 * and so the next save round-trips cleanly through [markdownToHtml] without
 * double-escaping the tags.
 */
fun String.htmlToMarkdown(): String {
    if (isEmpty()) return this
    return htmlToMarkdownConverter.convert(this).trimEnd('\n')
}