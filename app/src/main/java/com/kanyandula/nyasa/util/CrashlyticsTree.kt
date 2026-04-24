package com.kanyandula.nyasa.util

import androidx.annotation.VisibleForTesting
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashlyticsTree : Timber.Tree() {

    private val crashlytics by lazy { FirebaseCrashlytics.getInstance() }

    @VisibleForTesting
    public override fun isLoggable(tag: String?, priority: Int): Boolean =
        priority >= WARN_PRIORITY

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        crashlytics.log("${priorityChar(priority)}/$tag: $message")
        if (t != null) {
            crashlytics.recordException(t)
        }
    }

    private fun priorityChar(priority: Int): Char = when (priority) {
        WARN_PRIORITY -> 'W'
        ERROR_PRIORITY -> 'E'
        ASSERT_PRIORITY -> 'A'
        else -> '?'
    }

    companion object {
        private const val WARN_PRIORITY = 5 // android.util.Log.WARN
        private const val ERROR_PRIORITY = 6 // android.util.Log.ERROR
        private const val ASSERT_PRIORITY = 7 // android.util.Log.ASSERT
    }
}
