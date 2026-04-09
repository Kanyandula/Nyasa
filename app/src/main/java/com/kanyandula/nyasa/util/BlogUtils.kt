package com.kanyandula.nyasa.util

import androidx.core.text.HtmlCompat

object BlogUtils {

    fun stripHtml(html: String): String =
        HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
            .toString().trim()

    fun parseTags(tags: String?): List<String> =
        tags?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

    fun formatReadingTime(readingTime: Int?): String =
        readingTime?.let { "$it min read" } ?: ""
}
