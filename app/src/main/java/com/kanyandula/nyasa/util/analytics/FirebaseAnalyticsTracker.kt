package com.kanyandula.nyasa.util.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAnalyticsTracker @Inject constructor(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics,
) : AnalyticsTracker {

    override fun trackEvent(event: AnalyticsEvent) {
        analytics.logEvent(event.name) {
            event.params.forEach { (key, value) -> param(key, value) }
        }
    }

    override fun trackScreen(screenName: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
    }

    override fun setUserId(userId: String?) {
        analytics.setUserId(userId)
        crashlytics.setUserId(userId ?: "")
    }

    override fun logError(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
}
