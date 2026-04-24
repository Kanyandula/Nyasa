package com.kanyandula.nyasa.fakes

import com.kanyandula.nyasa.util.analytics.AnalyticsEvent
import com.kanyandula.nyasa.util.analytics.AnalyticsTracker

class FakeAnalyticsTracker : AnalyticsTracker {
    val events = mutableListOf<AnalyticsEvent>()
    val screens = mutableListOf<String>()
    var currentUserId: String? = null
    val errors = mutableListOf<Throwable>()

    override fun trackEvent(event: AnalyticsEvent) { events.add(event) }
    override fun trackScreen(screenName: String) { screens.add(screenName) }
    override fun setUserId(userId: String?) { currentUserId = userId }
    override fun logError(throwable: Throwable) { errors.add(throwable) }
}
