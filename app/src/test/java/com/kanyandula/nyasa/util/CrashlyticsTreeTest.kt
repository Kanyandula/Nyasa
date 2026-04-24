package com.kanyandula.nyasa.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CrashlyticsTreeTest {

    private val tree = CrashlyticsTree()

    @Test
    fun `isLoggable returns false for VERBOSE`() {
        assertThat(tree.isLoggable(null, 2)).isFalse() // Log.VERBOSE = 2
    }

    @Test
    fun `isLoggable returns false for DEBUG`() {
        assertThat(tree.isLoggable(null, 3)).isFalse() // Log.DEBUG = 3
    }

    @Test
    fun `isLoggable returns false for INFO`() {
        assertThat(tree.isLoggable(null, 4)).isFalse() // Log.INFO = 4
    }

    @Test
    fun `isLoggable returns true for WARN`() {
        assertThat(tree.isLoggable(null, 5)).isTrue() // Log.WARN = 5
    }

    @Test
    fun `isLoggable returns true for ERROR`() {
        assertThat(tree.isLoggable(null, 6)).isTrue() // Log.ERROR = 6
    }

    @Test
    fun `isLoggable returns true for ASSERT`() {
        assertThat(tree.isLoggable(null, 7)).isTrue() // Log.ASSERT = 7
    }
}
