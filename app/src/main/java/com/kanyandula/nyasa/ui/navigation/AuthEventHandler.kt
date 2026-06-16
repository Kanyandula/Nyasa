package com.kanyandula.nyasa.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kanyandula.nyasa.ui.auth.AuthViewModel

/** Single collector for [AuthViewModel.events] for the entire [Routes.AuthGraph]. */
@Composable
fun AuthEventHandler(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    if (!backStackEntry?.destination.isInGraph<Routes.AuthGraph>()) return

    val parentEntry = remember(navController) {
        navController.getBackStackEntry<Routes.AuthGraph>()
    }
    val viewModel: AuthViewModel = hiltViewModel(parentEntry)
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.events.collect { event -> handleStandardEvent(context, event) }
    }
}
