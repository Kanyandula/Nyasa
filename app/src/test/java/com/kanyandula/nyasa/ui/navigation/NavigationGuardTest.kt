package com.kanyandula.nyasa.ui.navigation

import android.app.Application
import androidx.navigation.NavGraph
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.ui.theme.ThemePreference
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Phase 0 guard, graph-level: tab navigation must be allowed from a MAIN_GRAPH destination, ignored
 * from an AUTH_GRAPH destination (session-swap race), and a tap must land on a leaf — never a
 * NavGraph wrapper. Destinations are sourced from the real production graph builders so the topology
 * under test is the production topology. (The pure null-recovery case is covered in [MainNavItemTest].)
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class NavigationGuardTest {

    private lateinit var navController: TestNavHostController

    @Before
    fun setUp() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        // Mirror RootNavHost: an unnamed root graph hosting authGraph + mainGraph.
        // TODO(Phase 5): drive the real RootNavHost instead of mirroring its assembly here.
        navController.graph = navController.createGraph(startDestination = Routes.MAIN_GRAPH) {
            authGraph(navController)
            mainGraph(navController, ThemePreference.SYSTEM) { /* onThemeChanged: not under test */ }
        }
    }

    @Test
    fun `a destination inside MAIN_GRAPH allows tab navigation`() {
        // setGraph resolves MAIN_GRAPH down to its leaf start destination (BLOG_FEED).
        assertThat(navController.currentDestination.allowsTabNavigation()).isTrue()
    }

    @Test
    fun `a destination inside AUTH_GRAPH is ignored`() {
        navController.navigate(Routes.WELCOME)
        assertThat(navController.currentDestination.allowsTabNavigation()).isFalse()
    }

    @Test
    fun `tapping the Home tab lands on the feed leaf, never a graph wrapper`() {
        navController.navigateToMainNavItem(MainNavItem.Home)

        val landed = navController.currentDestination
        assertThat(landed).isNotInstanceOf(NavGraph::class.java)
        assertThat(landed?.route).isEqualTo(Routes.BLOG_FEED)
    }

    @Test
    fun `tapping the Profile tab lands on the profile leaf, never a graph wrapper`() {
        navController.navigateToMainNavItem(MainNavItem.Profile)

        val landed = navController.currentDestination
        assertThat(landed).isNotInstanceOf(NavGraph::class.java)
        assertThat(landed?.route).isEqualTo(Routes.ACCOUNT_PROFILE)
    }
}
