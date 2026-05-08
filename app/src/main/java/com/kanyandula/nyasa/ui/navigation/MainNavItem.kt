package com.kanyandula.nyasa.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import timber.log.Timber

/** Top-level navigation destinations surfaced by [NyasaBottomBar] (phone) and [NyasaSideRail] (tablet). */
enum class MainNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Home("Home", Routes.BLOG_GRAPH, Icons.Filled.Home, Icons.Outlined.Home),
    Search("Search", Routes.BLOG_SEARCH, Icons.Filled.Search, Icons.Outlined.Search),
    Bookmarks(
        "Bookmarks",
        Routes.BOOKMARKS,
        Icons.Filled.Bookmark,
        Icons.Outlined.BookmarkBorder
    ),
    Profile("Profile", Routes.ACCOUNT_GRAPH, Icons.Filled.Person, Icons.Outlined.Person)
}

/** Maps the current route string to the active [MainNavItem]; defaults to [MainNavItem.Home]. */
fun mainNavItemForRoute(currentRoute: String?): MainNavItem = when {
    currentRoute?.startsWith(Routes.BLOG_SEARCH) == true -> MainNavItem.Search
    currentRoute?.startsWith(Routes.BOOKMARKS) == true -> MainNavItem.Bookmarks
    currentRoute?.startsWith(Routes.ACCOUNT_GRAPH) == true -> MainNavItem.Profile
    currentRoute?.startsWith(Routes.BLOG_GRAPH) == true -> MainNavItem.Home
    else -> MainNavItem.Home
}

/**
 * Navigates to the given top-level [item], preserving sibling back stacks (Material multi-stack pattern).
 *
 * Guards against two failure modes that surface as `IllegalStateException: Restore State failed` from
 * [NavController.navigate]:
 * - A tap that races a session-driven graph swap (bottom bar still visible the frame after logout).
 * - Stale saved back-stack IDs from a prior graph instance (e.g. after process-death restore), where
 *   `restoreState = true` cannot resolve a saved destination in the current graph.
 */
fun NavController.navigateToMainNavItem(item: MainNavItem) {
    if (!currentDestination.isInGraph(Routes.MAIN_GRAPH)) return
    try {
        navigate(item.route) {
            popUpTo(Routes.MAIN_GRAPH) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    } catch (restoreFailure: IllegalStateException) {
        Timber.w(
            restoreFailure,
            "Restore failed for %s; dropping saved back stack and retrying",
            item.route
        )
        try {
            clearBackStack(item.route)
        } catch (clearFailure: IllegalStateException) {
            Timber.w(clearFailure, "clearBackStack failed for %s", item.route)
        }
        try {
            navigate(item.route) {
                popUpTo(Routes.MAIN_GRAPH) { saveState = true }
                launchSingleTop = true
            }
        } catch (retryFailure: IllegalStateException) {
            Timber.e(retryFailure, "Recovery navigate to %s also failed", item.route)
        }
    }
}
