package com.kanyandula.nyasa.ui.navigation

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.kanyandula.nyasa.ui.theme.ThemePreference
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Phase 0 guard + Phase 2 typed-route behaviour, graph-level: tab navigation is allowed from a
 * MainGraph destination, ignored from an AuthGraph destination (session-swap race), a tap lands on
 * a leaf (never a NavGraph wrapper), and tab-selection resolves correctly for every destination.
 * Destinations come from the real production graph builders so the topology under test is the
 * production topology. (The pure null-recovery case is covered in [MainNavItemTest].)
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class NavigationGuardTest {

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        // A RESUMED host lifecycle is what drives the topmost back-stack entry to RESUMED — without
        // it TestNavHostController leaves entries at CREATED, so the double-pop gate can't be exercised.
        val lifecycleOwner = object : LifecycleOwner {
            val registry = LifecycleRegistry.createUnsafe(this).apply {
                currentState = Lifecycle.State.RESUMED
            }
            override val lifecycle: Lifecycle get() = registry
        }
        navController.setLifecycleOwner(lifecycleOwner)
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        // Mirror RootNavHost: an unnamed root graph hosting authGraph + mainGraph.
        // TODO(Phase 5): drive the real RootNavHost instead of mirroring its assembly here.
        navController.graph = navController.createGraph(startDestination = Routes.MainGraph) {
            authGraph(navController)
            mainGraph(navController, ThemePreference.SYSTEM) { /* onThemeChanged: not under test */ }
        }
    }

    @Test
    fun `a destination inside MainGraph allows tab navigation`() {
        // setGraph resolves MainGraph down to its leaf start destination (BlogFeed).
        assertThat(navController.currentDestination.allowsTabNavigation()).isTrue()
    }

    @Test
    fun `a destination inside AuthGraph is ignored`() {
        navController.navigate(Routes.Welcome)
        assertThat(navController.currentDestination.allowsTabNavigation()).isFalse()
    }

    @Test
    fun `tapping the Home tab lands on the feed leaf, never a graph wrapper`() {
        navController.navigateToMainNavItem(MainNavItem.Home)

        val landed = navController.currentDestination
        assertThat(landed).isNotInstanceOf(NavGraph::class.java)
        assertThat(landed?.hasRoute<Routes.BlogFeed>()).isTrue()
    }

    @Test
    fun `tapping the Profile tab lands on the profile leaf, never a graph wrapper`() {
        navController.navigateToMainNavItem(MainNavItem.Profile)

        val landed = navController.currentDestination
        assertThat(landed).isNotInstanceOf(NavGraph::class.java)
        assertThat(landed?.hasRoute<Routes.AccountProfile>()).isTrue()
    }

    /**
     * Drives every entry's enter transition to completion, the way the real [NavHost] does after its
     * animation settles. Without it [ComposeNavigator] parks entries mid-transition below RESUMED.
     */
    private fun settle() {
        val navigator = navController.navigatorProvider.getNavigator(ComposeNavigator::class.java)
        navigator.backStack.value.forEach { navigator.onTransitionComplete(it) }
    }

    @Test
    fun `popBackStackOnce pops once then gates a repeat tap from the same entry`() {
        navController.navigate(Routes.Bookmarks)
        navController.navigate(Routes.BlogDetail("s"))
        settle()

        val detailEntry = navController.currentBackStackEntry!!
        assertThat(detailEntry.isResumed()).isTrue()

        // First tap: the entry is the live screen, so the pop commits.
        assertThat(navController.popBackStackOnce(detailEntry)).isTrue()
        assertThat(navController.currentDestination?.hasRoute<Routes.Bookmarks>()).isTrue()

        // Second tap captured from the same (now non-resumed) entry is gated — no skipped screen.
        assertThat(navController.popBackStackOnce(detailEntry)).isFalse()
        assertThat(navController.currentDestination?.hasRoute<Routes.Bookmarks>()).isTrue()
    }

    @Test
    fun `destinations map to the expected highlighted tab`() {
        val cases = listOf(
            Routes.BlogFeed to MainNavItem.Home,
            Routes.BlogDetail("s") to MainNavItem.Home,
            Routes.BlogEdit("s") to MainNavItem.Home,
            Routes.AuthorProfile("u") to MainNavItem.Home,
            Routes.Create to MainNavItem.Home,
            Routes.BlogSearch to MainNavItem.Search,
            Routes.Bookmarks to MainNavItem.Bookmarks,
            Routes.AccountProfile to MainNavItem.Profile,
            Routes.AccountEdit to MainNavItem.Profile,
            Routes.AccountChangePassword to MainNavItem.Profile
        )
        cases.forEach { (route, expected) ->
            navController.navigate(route)
            assertWithMessage("route=%s", route)
                .that(mainNavItemForDestination(navController.currentDestination))
                .isEqualTo(expected)
        }
    }
}
