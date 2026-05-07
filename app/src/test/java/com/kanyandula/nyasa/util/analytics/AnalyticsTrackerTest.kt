package com.kanyandula.nyasa.util.analytics

import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.fakes.FakeAnalyticsTracker
import org.junit.Test

class AnalyticsTrackerTest {

    // ── AnalyticsEvent contracts ──────────────────────────────────────

    @Test
    fun `Login name is login and params is empty`() {
        assertThat(AnalyticsEvent.Login.name).isEqualTo("login")
        assertThat(AnalyticsEvent.Login.params).isEmpty()
    }

    @Test
    fun `ViewPost params contains slug`() {
        val event = AnalyticsEvent.ViewPost("my-slug", isFeatured = false)
        assertThat(event.name).isEqualTo("view_post")
        assertThat(event.params).containsEntry("slug", "my-slug")
    }

    @Test
    fun `ViewPost params contains is_featured`() {
        val event = AnalyticsEvent.ViewPost("hero-slug", isFeatured = true)
        assertThat(event.params).containsEntry("is_featured", "true")
    }

    @Test
    fun `Search params contains has_query as string`() {
        val event = AnalyticsEvent.Search(true)
        assertThat(event.name).isEqualTo("search")
        assertThat(event.params).containsEntry("has_query", "true")
    }

    // ── NoOpAnalyticsTracker ─────────────────────────────────────────

    @Test
    fun `NoOp trackEvent does not throw`() {
        val tracker = NoOpAnalyticsTracker()
        tracker.trackEvent(AnalyticsEvent.Login)
    }

    @Test
    fun `NoOp logError does not throw`() {
        val tracker = NoOpAnalyticsTracker()
        tracker.logError(RuntimeException("test"))
    }

    // ── FakeAnalyticsTracker ─────────────────────────────────────────

    @Test
    fun `Fake records events in events list`() {
        val tracker = FakeAnalyticsTracker()

        tracker.trackEvent(AnalyticsEvent.Login)
        tracker.trackEvent(AnalyticsEvent.ViewPost("slug-1", isFeatured = false))

        assertThat(tracker.events).hasSize(2)
        assertThat(tracker.events[0]).isEqualTo(AnalyticsEvent.Login)
        assertThat(tracker.events[1]).isEqualTo(AnalyticsEvent.ViewPost("slug-1", isFeatured = false))
    }

    @Test
    fun `Fake records screens`() {
        val tracker = FakeAnalyticsTracker()
        tracker.trackScreen("home")
        assertThat(tracker.screens).containsExactly("home")
    }

    @Test
    fun `Fake records userId`() {
        val tracker = FakeAnalyticsTracker()
        tracker.setUserId("user-42")
        assertThat(tracker.currentUserId).isEqualTo("user-42")
    }

    @Test
    fun `Fake records errors`() {
        val tracker = FakeAnalyticsTracker()
        val ex = RuntimeException("boom")
        tracker.logError(ex)
        assertThat(tracker.errors).containsExactly(ex)
    }
}
