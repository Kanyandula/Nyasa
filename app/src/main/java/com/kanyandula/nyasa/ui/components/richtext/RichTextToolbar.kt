package com.kanyandula.nyasa.ui.components.richtext

internal fun normalizeUrl(input: String): String {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return trimmed
    val lower = trimmed.lowercase()
    return when {
        lower.startsWith("https://") || lower.startsWith("http://") -> trimmed
        lower.startsWith("mailto:") -> trimmed
        else -> "https://$trimmed"
    }
}
