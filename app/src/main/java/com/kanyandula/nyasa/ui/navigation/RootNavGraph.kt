package com.kanyandula.nyasa.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.kanyandula.nyasa.ui.theme.ThemePreference

/**
 * Registers [authGraph] and [mainGraph] into the receiver graph — the app's two top-level
 * destinations. The enclosing root graph and its `startDestination` are created by the caller
 * (`RootNavHost`'s `NavHost`), kept there because the start is session-gated (AuthGraph when logged
 * out, MainGraph when logged in).
 *
 * Extracted from `RootNavHost` so the assembly lives in exactly one place. Tests drive it via
 * `navController.createGraph(...) { rootNavGraph(...) }`, so the topology under test is the
 * production topology — a graph-shape bug can no longer hide behind a hand-mirrored test copy
 * (Phase 5 of NAVIGATION_REFACTOR.md).
 */
fun NavGraphBuilder.rootNavGraph(
    navController: NavController,
    currentTheme: ThemePreference,
    onThemeChanged: (ThemePreference) -> Unit
) {
    authGraph(navController)
    mainGraph(
        navController = navController,
        currentTheme = currentTheme,
        onThemeChanged = onThemeChanged
    )
}
