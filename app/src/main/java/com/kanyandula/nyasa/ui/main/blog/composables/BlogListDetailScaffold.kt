package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.kanyandula.nyasa.ui.theme.window.LocalWindow

/**
 * Adaptive list-detail wrapper for the blog feed and search routes.
 *
 * On Small windows, the scaffold is not mounted: the list pane runs as today and clicks
 * fire [onNavigateToDetailFullScreen]. On Medium windows, mounts a
 * `NavigableListDetailPaneScaffold` and routes selection through its internal
 * `ThreePaneScaffoldNavigator`. The Medium path is added in the next task.
 */
@Suppress("UnusedParameter")
@Composable
internal fun BlogListDetailScaffold(
    onNavigateToDetailFullScreen: (slug: String) -> Unit,
    visibleSlugs: Set<String>,
    mode: FeedMode,
    listPane: @Composable (onBlogClicked: (String) -> Unit) -> Unit,
    detailPane: @Composable (slug: String, onClose: () -> Unit) -> Unit
) {
    val isMedium by LocalWindow.current.isMediumWindowAsState()

    if (!isMedium) {
        listPane(onNavigateToDetailFullScreen)
        return
    }

    // Medium path lands in Task 5.
    listPane(onNavigateToDetailFullScreen)
}
