package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.window.core.layout.WindowWidthSizeClass
import com.kanyandula.nyasa.ui.theme.window.LocalWindow

/**
 * Adaptive list-detail wrapper for the blog feed and search routes.
 *
 * On Small windows, the scaffold is not mounted: the list pane runs as today and clicks
 * fire [onNavigateToDetailFullScreen]. On Medium windows, mounts a
 * [NavigableListDetailPaneScaffold] and routes selection through its internal
 * `ThreePaneScaffoldNavigator`.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun BlogListDetailScaffold(
    onNavigateToDetailFullScreen: (slug: String) -> Unit,
    visibleSlugs: Set<String>,
    mode: FeedMode,
    listPane: @Composable (onBlogClicked: (String) -> Unit) -> Unit,
    detailPane: @Composable (slug: String, onClose: () -> Unit) -> Unit,
    expandedListPane: (@Composable (onBlogClicked: (String) -> Unit) -> Unit)? = null
) {
    val isMedium by LocalWindow.current.isMediumWindowAsState()

    if (!isMedium) {
        listPane(onNavigateToDetailFullScreen)
        return
    }

    val adaptiveInfo = currentWindowAdaptiveInfo()
    val scaffoldDirective = remember(adaptiveInfo) {
        calculatePaneScaffoldDirective(adaptiveInfo)
    }
    val navigator = rememberListDetailPaneScaffoldNavigator<Any>(
        scaffoldDirective = scaffoldDirective
    )
    val onClose: () -> Unit = { navigator.navigateBack() }

    val isExpanded = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
    val hasSelection = navigator.currentDestination?.content != null

    // Expanded-class bypass: when the caller supplies a full-width Phase 3 layout for the
    // no-selection state, render it directly instead of mounting NavigableListDetailPaneScaffold.
    // The navigator is already constructed above so its Saver state survives the structural
    // swap when the user taps a post and the scaffold mounts on the next recomposition.
    if (isExpanded && !hasSelection && expandedListPane != null) {
        expandedListPane { slug ->
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, slug)
        }
        return
    }

    // Q4 D rule: when the user changes the search query and the selected post drops out of
    // the visible result set, collapse the detail pane. Operates on the scaffold navigator
    // only — never touches NavController. Inactive in Home mode (paging churn from scrolling
    // shouldn't clear the detail pane just because the selected post left the visible window).
    val selected = navigator.currentDestination?.content as? String
    LaunchedEffect(visibleSlugs, selected, mode) {
        if (mode == FeedMode.Search && selected != null && selected !in visibleSlugs) {
            navigator.navigateBack()
        }
    }

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        listPane = {
            AnimatedPane {
                listPane { slug -> navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, slug) }
            }
        },
        detailPane = {
            AnimatedPane {
                val slug = navigator.currentDestination?.content as? String
                if (slug != null) {
                    detailPane(slug, onClose)
                } else {
                    DetailPanePlaceholder()
                }
            }
        }
    )
}
