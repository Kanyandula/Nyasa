package com.kanyandula.nyasa.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.components.NyasaBottomBar
import com.kanyandula.nyasa.ui.navigation.Routes
import com.kanyandula.nyasa.ui.navigation.authGraph
import com.kanyandula.nyasa.ui.navigation.isInGraph
import com.kanyandula.nyasa.ui.navigation.isValid
import com.kanyandula.nyasa.ui.navigation.mainGraph
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.ui.theme.ThemePreference
import com.kanyandula.nyasa.ui.theme.ThemePreferenceManager
import com.kanyandula.nyasa.util.analytics.AnalyticsTracker
import com.kanyandula.nyasa.util.analytics.LocalAnalyticsTracker
import com.kanyandula.nyasa.work.UploadKeys
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val AUTH_TOKEN_BUNDLE_KEY = "auth_token"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var themeDataStore: DataStore<Preferences>

    @Inject
    lateinit var analyticsTracker: AnalyticsTracker

    @Suppress("LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreSession(savedInstanceState)

        setContent {
            val themePreference by ThemePreferenceManager
                .themeFlow(themeDataStore)
                .collectAsStateWithLifecycle(initialValue = ThemePreference.SYSTEM)

            val darkTheme = when (themePreference) {
                ThemePreference.SYSTEM -> isSystemInDarkTheme()
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }

            CompositionLocalProvider(LocalAnalyticsTracker provides analyticsTracker) {
                NyasaTheme(darkTheme = darkTheme) {
                    val navController = rememberNavController()
                    val themeScope = rememberCoroutineScope()
                    val token by sessionManager.cachedToken.collectAsStateWithLifecycle()

                    val startDestination = remember {
                        if (sessionManager.cachedToken.value.isValid()) {
                            Routes.MAIN_GRAPH
                        } else {
                            Routes.AUTH_GRAPH
                        }
                    }

                    LaunchedEffect(token) {
                        analyticsTracker.setUserId(token?.account_pk?.toString())
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

                    UploadCompletionToasts()

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
                            mainGraph(
                                navController = navController,
                                currentTheme = themePreference,
                                onThemeChanged = { preference ->
                                    themeScope.launch {
                                        ThemePreferenceManager.setTheme(
                                            themeDataStore,
                                            preference
                                        )
                                    }
                                }
                            )
                        }
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

@Composable
private fun UploadCompletionToasts() {
    val context = LocalContext.current
    val reportedUploads = remember { mutableSetOf<UUID>() }
    LaunchedEffect(Unit) {
        var firstEmission = true
        WorkManager.getInstance(context)
            .getWorkInfosByTagFlow(UploadKeys.WORK_TAG_UPLOAD)
            .collect { infos ->
                infos.filter { it.state.isFinished }
                    .forEach { info ->
                        val seen = !reportedUploads.add(info.id)
                        if (seen || firstEmission) return@forEach
                        toastForUploadState(context, info.state)
                    }
                firstEmission = false
            }
    }
}

private fun toastForUploadState(
    context: android.content.Context,
    state: WorkInfo.State
) {
    val msg = when (state) {
        WorkInfo.State.SUCCEEDED -> "Post published"
        WorkInfo.State.FAILED -> "Upload failed — check notifications"
        WorkInfo.State.CANCELLED -> "Upload cancelled"
        else -> return
    }
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}
