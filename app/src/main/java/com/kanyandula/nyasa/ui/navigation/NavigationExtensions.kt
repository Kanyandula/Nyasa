package com.kanyandula.nyasa.ui.navigation

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
