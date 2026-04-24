package com.kanyandula.nyasa.util.analytics

class NoOpAnalyticsTracker : AnalyticsTracker {
    override fun trackEvent(event: AnalyticsEvent) = Unit
    override fun trackScreen(screenName: String) = Unit
    override fun setUserId(userId: String?) = Unit
    override fun logError(throwable: Throwable) = Unit
}
