package com.kanyandula.nyasa.util

object BlogUtils {

    fun parseTags(tags: String?): List<String> =
        tags?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

    fun formatReadingTime(readingTime: Int?): String =
        readingTime?.let { "$it min read" } ?: ""
}
