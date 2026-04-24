package com.kanyandula.nyasa.util.analytics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
fun TrackScreen(screenName: String) {
    val tracker = LocalAnalyticsTracker.current
    DisposableEffect(screenName) {
        tracker.trackScreen(screenName)
        onDispose { }
    }
}
