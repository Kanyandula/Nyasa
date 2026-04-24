package com.kanyandula.nyasa.util.analytics

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAnalyticsTracker = staticCompositionLocalOf<AnalyticsTracker> {
    error("No AnalyticsTracker provided")
}
