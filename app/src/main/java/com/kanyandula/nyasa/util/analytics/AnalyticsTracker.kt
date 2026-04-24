package com.kanyandula.nyasa.util.analytics

interface AnalyticsTracker {
    /**
     * Track a user action. Call ONLY after the action has succeeded,
     * not on attempt. For errors, use [logError].
     */
    fun trackEvent(event: AnalyticsEvent)
    fun trackScreen(screenName: String)
    fun setUserId(userId: String?)
    fun logError(throwable: Throwable)
}
