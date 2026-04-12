package com.kanyandula.nyasa.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.components.NyasaBottomBar
import com.kanyandula.nyasa.ui.navigation.Routes
import com.kanyandula.nyasa.ui.navigation.authGraph
import com.kanyandula.nyasa.ui.navigation.isInGraph
import com.kanyandula.nyasa.ui.navigation.isValid
import com.kanyandula.nyasa.ui.navigation.mainGraph
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val AUTH_TOKEN_BUNDLE_KEY = "auth_token"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreSession(savedInstanceState)

        setContent {
            NyasaTheme {
                val navController = rememberNavController()
                val token by sessionManager.cachedToken.collectAsStateWithLifecycle()

                val startDestination = remember {
                    if (sessionManager.cachedToken.value.isValid()) {
                        Routes.MAIN_GRAPH
                    } else {
                        Routes.AUTH_GRAPH
                    }
                }

                LaunchedEffect(token) {
                    val target = if (token.isValid()) {
                        Routes.MAIN_GRAPH
                    } else {
                        Routes.AUTH_GRAPH
                    }
                    if (!navController.currentDestination.isInGraph(target)) {
                        navController.navigate(target) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val showBottomBar = remember(navBackStackEntry) {
                    navBackStackEntry?.destination.isInGraph(Routes.MAIN_GRAPH)
                        ?: false
                }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            NyasaBottomBar(navController = navController)
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(padding)
                    ) {
                        authGraph(navController, sessionManager)
                        mainGraph(navController)
                    }
                }
            }
        }
    }

    private fun restoreSession(savedInstanceState: Bundle?) {
        @Suppress("DEPRECATION")
        savedInstanceState?.getParcelable<AuthToken>(AUTH_TOKEN_BUNDLE_KEY)?.let {
            sessionManager.login(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(AUTH_TOKEN_BUNDLE_KEY, sessionManager.cachedToken.value)
    }
}
