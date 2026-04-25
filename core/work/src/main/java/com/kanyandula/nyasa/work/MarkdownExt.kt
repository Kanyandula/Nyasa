package com.kanyandula.nyasa.work

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

private val markdownParser: Parser = Parser.builder().build()

// escapeHtml(true) makes the renderer treat raw HTML in the source as literal
// text instead of passing it through — basic XSS safety against `<script>` etc.
// typed (or pasted) into the editor. The toolbar only produces markdown, so
// users shouldn't lose anything they intended to render.
private val htmlRenderer: HtmlRenderer = HtmlRenderer.builder()
    .escapeHtml(true)
    .build()

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