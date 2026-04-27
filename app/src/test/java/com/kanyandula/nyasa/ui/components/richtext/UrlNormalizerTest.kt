package com.kanyandula.nyasa.ui.components.richtext

import org.junit.Assert.assertEquals
import org.junit.Test

class UrlNormalizerTest {

    @Test
    fun `https URL is preserved`() {
        assertEquals("https://example.com", normalizeUrl("https://example.com"))
    }

    @Test
    fun `http URL is preserved`() {
        assertEquals("http://example.com", normalizeUrl("http://example.com"))
    }

    @Test
    fun `mailto URL is preserved`() {
        assertEquals("mailto:foo@bar.com", normalizeUrl("mailto:foo@bar.com"))
    }

    @Test
    fun `mixed-case scheme is detected and original case is preserved`() {
        assertEquals("HTTPS://example.com", normalizeUrl("HTTPS://example.com"))
    }

    @Test
    fun `schemeless input is prefixed with https`() {
        assertEquals("https://example.com", normalizeUrl("example.com"))
    }

    @Test
    fun `whitespace is trimmed`() {
        assertEquals("https://example.com", normalizeUrl("  https://example.com  "))
    }

    @Test
    fun `blank input returns empty string`() {
        assertEquals("", normalizeUrl("   "))
    }

    @Test
    fun `ftp input is treated as schemeless and prefixed with https`() {
        assertEquals("https://ftp://example.com", normalizeUrl("ftp://example.com"))
    }
}
