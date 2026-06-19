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
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Top-level navigation destinations surfaced by [NyasaBottomBar] (phone) and [NyasaSideRail] (tablet).
 *
 * [route] is the type-safe leaf route the tab navigates to. Typing it as [Routes.TabRoute] makes
 * "a tab points at a leaf, never a graph wrapper" a compile-time guarantee.
 */
enum class MainNavItem(
    val label: String,
    val route: Routes.TabRoute,
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
 * Maps the current [destination] to the active [MainNavItem]. Each tab is matched positively against
 * the routes it owns: Home covers the blog-browsing leaves (feed, detail, edit, author, create),
 * the Account graph maps to Profile. A `null` or otherwise unrecognized destination defaults to Home
 * (the app's landing tab), which also keeps the smart-cast that the leading `null` guard provides.
 */
fun mainNavItemForDestination(destination: NavDestination?): MainNavItem = when {
    destination == null -> MainNavItem.Home
    destination.hasRoute<Routes.BlogSearch>() -> MainNavItem.Search
    destination.hasRoute<Routes.Bookmarks>() -> MainNavItem.Bookmarks
    destination.isInGraph<Routes.AccountGraph>() -> MainNavItem.Profile
    destination.hasRoute<Routes.BlogFeed>() ||
        destination.hasRoute<Routes.BlogDetail>() ||
        destination.hasRoute<Routes.BlogEdit>() ||
        destination.hasRoute<Routes.AuthorProfile>() ||
        destination.hasRoute<Routes.Create>() -> MainNavItem.Home
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
 * Navigates to the given top-level [item] with a single linear back stack per tab.
 *
 * Pops up to the graph's start destination and uses `launchSingleTop`, so switching tabs resets the
 * destination tree to its root without stacking duplicate tab roots. As of Phase 3b the Material
 * multi-stack `saveState`/`restoreState` pattern is gone — it was the source of the
 * `IllegalStateException: Restore State failed` crashes and the tab-freeze, and required the
 * try/catch recovery that is no longer needed. Trade-off: scroll position no longer survives a tab
 * switch (accepted as a product decision).
 *
 * The [allowsTabNavigation] guard still ignores taps that race a session-driven graph swap (a tap in
 * `AuthGraph` the frame after logout) while letting a `null`/dead stack recover.
 */
fun NavController.navigateToMainNavItem(item: MainNavItem) {
    if (!currentDestination.allowsTabNavigation()) return
    navigate(item.route) {
        popUpTo(graph.findStartDestination().id) { inclusive = false }
        launchSingleTop = true
    }
}
