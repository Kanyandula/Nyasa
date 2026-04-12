package com.kanyandula.nyasa.ui.navigation

import androidx.navigation.NavDestination
import com.kanyandula.nyasa.models.AuthToken

fun AuthToken?.isValid(): Boolean =
    this != null && account_pk != -1 && token != null

fun NavDestination?.isInGraph(graphRoute: String): Boolean =
    this?.route == graphRoute ||
        this?.parent?.route == graphRoute ||
        this?.parent?.parent?.route == graphRoute
