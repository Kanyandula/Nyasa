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
 * Phase 0 guard + Phase 2 typed-route behaviour + Phase 5 coverage, graph-level: tab navigation is
 * allowed from a MainGraph destination, ignored from an AuthGraph destination (session-swap race), a
 * tap lands on a leaf (never a NavGraph wrapper), tab-selection resolves correctly for every
 * destination, saved state survives process death, and bottom-bar taps stay gated across a full
 * login→logout→login swap. Destinations come from the real production [rootNavGraph] builder so the
 * topology under test is the production topology. (The pure null-recovery case is in [MainNavItemTest].)
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class NavigationGuardTest {

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        navController = freshController().apply { graph = rootGraph(this) }
    }

    /**
     * A [TestNavHostController] wired the way the real host wires it: a RESUMED host lifecycle (which
     * drives the topmost back-stack entry to RESUMED — without it entries park at CREATED and the
     * double-pop gate can't be exercised) plus a registered [ComposeNavigator]. No graph is attached,
     * so callers either set a fresh [rootGraph] or restore saved state first (process-death case).
     */
    private fun freshController(): TestNavHostController {
        val controller = TestNavHostController(ApplicationProvider.getApplicationContext())
        val lifecycleOwner = object : LifecycleOwner {
            val registry = LifecycleRegistry.createUnsafe(this).apply {
                currentState = Lifecycle.State.RESUMED
            }
            override val lifecycle: Lifecycle get() = registry
        }
        controller.setLifecycleOwner(lifecycleOwner)
        controller.navigatorProvider.addNavigator(ComposeNavigator())
        return controller
    }

    /**
     * The production assembly under test: [rootNavGraph] is the exact builder `RootNavHost` uses, so
     * there is no mirrored copy to drift out of sync (Phase 5). Start destination is MainGraph — the
     * logged-in topology where bottom-bar navigation is exercised.
     */
    private fun rootGraph(controller: TestNavHostController) =
        controller.createGraph(startDestination = Routes.MainGraph) {
            rootNavGraph(controller, ThemePreference.SYSTEM) { /* onThemeChanged: not under test */ }
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
     * Drives the current (top) entry's enter transition to completion, the way the real [NavHost]
     * does after its animation settles, so it reaches RESUMED. Only the top entry is completed —
     * entries beneath it stay below RESUMED, mirroring real lifecycle. Without this [ComposeNavigator]
     * parks the entering entry mid-transition below RESUMED.
     */
    private fun settleCurrent() {
        val navigator = navController.navigatorProvider.getNavigator(ComposeNavigator::class.java)
        navController.currentBackStackEntry?.let { navigator.onTransitionComplete(it) }
    }

    @Test
    fun `switching tabs away and back resets the tab to its root`() {
        // Build a sub-stack under Home, then leave and return via the bottom bar.
        navController.navigate(Routes.BlogDetail("s"))
        assertThat(navController.currentDestination?.hasRoute<Routes.BlogDetail>()).isTrue()

        navController.navigateToMainNavItem(MainNavItem.Bookmarks)
        navController.navigateToMainNavItem(MainNavItem.Home)

        // Phase 3b dropped saveState/restoreState: the BlogDetail sub-stack is not restored — Home
        // lands back on its root leaf. Guards against a future re-introduction of restoreState.
        assertThat(navController.currentDestination?.hasRoute<Routes.BlogFeed>()).isTrue()
    }

    @Test
    fun `popBackStackOnce pops once then gates a repeat tap from the same entry`() {
        navController.navigate(Routes.Bookmarks)
        navController.navigate(Routes.BlogDetail("s"))
        settleCurrent()

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

    @Test
    fun `saved navigation state restores onto a valid leaf after process death`() {
        // Build a non-trivial stack, then simulate process death: save state, drop the controller,
        // and rehydrate a fresh one. restoreState must precede setGraph for the back stack to rebuild.
        navController.navigate(Routes.Bookmarks)
        navController.navigate(Routes.BlogDetail("s"))
        val savedState = navController.saveState()

        val restored = freshController()
        restored.restoreState(savedState)
        restored.graph = rootGraph(restored)

        val landed = restored.currentDestination
        assertThat(landed).isNotNull()
        assertThat(landed).isNotInstanceOf(NavGraph::class.java)
        // The user is put back exactly where they were, and the tab bar is live again — no blank stack.
        assertThat(landed?.hasRoute<Routes.BlogDetail>()).isTrue()
        assertThat(landed?.allowsTabNavigation()).isTrue()
    }

    @Test
    fun `bottom-bar taps are gated across a login-logout-login swap`() {
        // Logged in: a tab tap navigates normally.
        navController.navigateToMainNavItem(MainNavItem.Bookmarks)
        assertThat(navController.currentDestination?.hasRoute<Routes.Bookmarks>()).isTrue()

        // Logout lands on the auth start leaf (Welcome). A bottom-bar tap racing the swap must be
        // ignored — navigating into the just-removed MainGraph the next frame is what crashed pre-Phase-3.
        navController.navigate(Routes.Welcome)
        navController.navigateToMainNavItem(MainNavItem.Home)
        assertThat(navController.currentDestination?.allowsTabNavigation()).isFalse()
        assertThat(navController.currentDestination?.hasRoute<Routes.Welcome>()).isTrue()

        // Login returns to the MainGraph start leaf (BlogFeed): taps flow again, landing on the tab leaf.
        navController.navigate(Routes.BlogFeed)
        navController.navigateToMainNavItem(MainNavItem.Profile)
        assertThat(navController.currentDestination?.hasRoute<Routes.AccountProfile>()).isTrue()
    }
}
