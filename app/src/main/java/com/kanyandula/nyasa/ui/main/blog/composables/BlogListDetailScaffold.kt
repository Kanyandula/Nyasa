package com.kanyandula.nyasa.ui.main.blog.composables

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
@Suppress("UnusedParameter") // visibleSlugs + mode wired in Task 6 (search detail-clear)
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

    val navigator = rememberListDetailPaneScaffoldNavigator<Any>(
        scaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
    )
    val onClose: () -> Unit = { navigator.navigateBack() }

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
