package com.kanyandula.nyasa.ui.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.kanyandula.nyasa.models.AuthToken

fun AuthToken?.isValid(): Boolean =
    this != null && account_pk != -1 && token != null

/**
 * Whether [this] destination is inside the graph keyed by route type [T], at any nesting depth.
 * Walks the full destination hierarchy (self + all ancestors), so it is unaffected by how deeply
 * the graph sits — unlike the previous fixed two-level parent walk.
 */
inline fun <reified T : Any> NavDestination?.isInGraph(): Boolean =
    this != null && hierarchy.any { it.hasRoute<T>() }

/**
 * Whether this entry is the active (RESUMED) screen. NavController drops a leaving entry below
 * RESUMED the instant a navigation commits, so this stays true only for the screen currently
 * driving navigation — the basis of the single-action-per-screen gate below.
 */
fun NavBackStackEntry.isResumed(): Boolean =
    lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)

/**
 * Pops the back stack only if [entry] is still RESUMED, returning whether a pop occurred.
 *
 * Prevents the double-pop where fast back-arrow taps on a deep stack (e.g. Feed → Bookmarks →
 * Detail) skip an intermediate screen: the first tap commits the pop and drops [entry] below
 * RESUMED, so any tap landing during the exit animation is filtered out at the source.
 */
fun NavController.popBackStackOnce(entry: NavBackStackEntry): Boolean =
    if (entry.isResumed()) popBackStack() else false
