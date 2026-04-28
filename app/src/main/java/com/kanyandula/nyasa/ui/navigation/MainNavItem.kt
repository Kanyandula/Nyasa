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

/** Navigates to the given top-level [item], preserving sibling back stacks (Material multi-stack pattern). */
fun NavController.navigateToMainNavItem(item: MainNavItem) {
    navigate(item.route) {
        popUpTo(Routes.MAIN_GRAPH) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
