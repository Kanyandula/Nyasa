package com.kanyandula.nyasa.ui.navigation

import androidx.navigation.NavDestination
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Pure-JVM contract tests for [MainNavItem] route values and the null-recovery predicate
 * (no Robolectric needed — graph-level tab-selection + guard behaviour lives in [NavigationGuardTest]).
 *
 * Phase 0 contract: every bottom-bar tab targets a real leaf screen, never a NavGraph wrapper.
 */
class MainNavItemTest {

    @Test
    fun `Home tab targets the blog feed leaf route`() {
        assertThat(MainNavItem.Home.route).isEqualTo(Routes.BlogFeed)
    }

    @Test
    fun `Profile tab targets the account profile leaf route`() {
        assertThat(MainNavItem.Profile.route).isEqualTo(Routes.AccountProfile)
    }

    @Test
    fun `no tab targets a NavGraph wrapper route`() {
        val graphWrappers = setOf(
            Routes.MainGraph,
            Routes.AuthGraph,
            Routes.AccountGraph
        )
        MainNavItem.entries.forEach { item ->
            assertThat(item.route).isNotIn(graphWrappers)
        }
    }

    @Test
    fun `null destination allows tab navigation so a dead stack recovers`() {
        assertThat((null as NavDestination?).allowsTabNavigation()).isTrue()
    }
}
