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
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import timber.log.Timber

/**
 * Top-level navigation destinations surfaced by [NyasaBottomBar] (phone) and [NyasaSideRail] (tablet).
 *
 * [route] is the type-safe leaf route the tab navigates to (a `@Serializable` route key).
 *
 * TODO(Phase 3): once the graph is flat, replace `Any` with a sealed route supertype so the
 * "tab points at a leaf, never a graph wrapper" invariant is compiler-checked, not test-checked.
 */
enum class MainNavItem(
    val label: String,
    val route: Any,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Home("Home", Routes.BlogFeed, Icons.Filled.Home, Icons.Outlined.Home),
    Search("Search", Routes.BlogSearch, Icons.Filled.Search, Icons.Outlined.Search),
    Bookmarks(
        "Bookmarks",
        Routes.Bookmarks,
        Icons.Filled.Bookmark,
        Icons.Outlined.BookmarkBorder
    ),
    Profile("Profile", Routes.AccountProfile, Icons.Filled.Person, Icons.Outlined.Person)
}

/**
 * Maps the current [destination] to the active [MainNavItem]. Home is the catch-all default: every
 * blog-browsing leaf (feed, detail, edit, author, create) highlights Home.
 *
 * TODO(Phase 3): when typed routes + a flat graph land, express this as an exhaustive
 * `when (route) { is BlogFeed/BlogDetail/... -> Home }` so Home is matched positively rather than
 * by elimination.
 */
fun mainNavItemForDestination(destination: NavDestination?): MainNavItem = when {
    destination == null -> MainNavItem.Home
    destination.hasRoute<Routes.BlogSearch>() -> MainNavItem.Search
    destination.hasRoute<Routes.Bookmarks>() -> MainNavItem.Bookmarks
    destination.isInGraph<Routes.AccountGraph>() -> MainNavItem.Profile
    else -> MainNavItem.Home
}

/**
 * Whether a bottom-bar tab tap should be allowed to navigate, given the current destination.
 *
 * A destination outside `MainGraph` (e.g. `AuthGraph` the frame after a logout-driven graph swap)
 * is ignored — navigating would race the swap and crash. A `null` destination, however, means the
 * back stack is momentarily empty/dead (observed after process-death restore, and during the same
 * swap); in that case the tap must be allowed through so `navigate()` recovers the user instead of
 * silently early-returning and stranding them on a blank screen.
 *
 * @see navigateToMainNavItem — prefer that; this predicate is exposed for testing.
 */
internal fun NavDestination?.allowsTabNavigation(): Boolean =
    this == null || isInGraph<Routes.MainGraph>()

/**
 * Navigates to the given top-level [item], preserving sibling back stacks (Material multi-stack pattern).
 *
 * Anchored on [Routes.BlogFeed] — the deepest start destination of `MainGraph`, always present in
 * the back stack while the user is in `MainGraph`. Anchoring on `MainGraph` instead would collapse
 * Home and Search into a single save bucket because they share `BlogGraph`, causing tab switches to
 * restore each other and freeze navigation.
 *
 * Guards against two failure modes that surface as `IllegalStateException: Restore State failed` from
 * [NavController.navigate]:
 * - A tap that races a session-driven graph swap (bottom bar still visible the frame after logout).
 * - Stale saved back-stack IDs from a prior graph instance (e.g. after process-death restore), where
 *   `restoreState = true` cannot resolve a saved destination in the current graph.
 */
fun NavController.navigateToMainNavItem(item: MainNavItem) {
    if (!currentDestination.allowsTabNavigation()) return
    try {
        navigate(item.route) {
            popUpTo(Routes.BlogFeed) { saveState = true }
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
                popUpTo(Routes.BlogFeed) { saveState = true }
                launchSingleTop = true
            }
        } catch (retryFailure: IllegalStateException) {
            Timber.e(retryFailure, "Recovery navigate to %s also failed", item.route)
        }
    }
}
