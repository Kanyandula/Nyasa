package com.kanyandula.nyasa.ui.navigation

import androidx.navigation.NavDestination
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

/**
 * Pure-JVM contract tests for [MainNavItem] route values and [mainNavItemForRoute] mapping
 * (no Robolectric needed — see [NavigationGuardTest] for graph-level guard behaviour).
 *
 * Phase 0 contract: every bottom-bar tab targets a real leaf screen, never a NavGraph wrapper,
 * and tab-selection still resolves correctly when the user is deep inside a sub-route.
 */
class MainNavItemTest {

    @Test
    fun `Home tab targets the blog feed leaf route`() {
        assertThat(MainNavItem.Home.route).isEqualTo(Routes.BLOG_FEED)
    }

    @Test
    fun `Profile tab targets the account profile leaf route`() {
        assertThat(MainNavItem.Profile.route).isEqualTo(Routes.ACCOUNT_PROFILE)
    }

    @Test
    fun `no tab targets a NavGraph wrapper route`() {
        val graphWrappers = setOf(
            Routes.MAIN_GRAPH,
            Routes.AUTH_GRAPH,
            Routes.BLOG_GRAPH,
            Routes.ACCOUNT_GRAPH
        )
        MainNavItem.entries.forEach { item ->
            assertThat(item.route).isNotIn(graphWrappers)
        }
    }

    @Test
    fun `routes map to the expected highlighted tab`() {
        val expectations = mapOf(
            Routes.BLOG_DETAIL to MainNavItem.Home,
            Routes.BLOG_EDIT to MainNavItem.Home,
            Routes.AUTHOR_PROFILE to MainNavItem.Home,
            Routes.ACCOUNT_EDIT to MainNavItem.Profile,
            Routes.BLOG_SEARCH to MainNavItem.Search,
            Routes.BOOKMARKS to MainNavItem.Bookmarks,
            null to MainNavItem.Home
        )
        expectations.forEach { (route, expected) ->
            assertWithMessage("route=%s", route)
                .that(mainNavItemForRoute(route))
                .isEqualTo(expected)
        }
    }

    @Test
    fun `null destination allows tab navigation so a dead stack recovers`() {
        assertThat((null as NavDestination?).allowsTabNavigation()).isTrue()
    }
}
