package com.kanyandula.nyasa.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.components.NyasaBottomBar
import com.kanyandula.nyasa.ui.components.NyasaSideRail
import com.kanyandula.nyasa.ui.navigation.AuthEventHandler
import com.kanyandula.nyasa.ui.navigation.Routes
import com.kanyandula.nyasa.ui.navigation.isInGraph
import com.kanyandula.nyasa.ui.navigation.isValid
import com.kanyandula.nyasa.ui.navigation.rootNavGraph
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import com.kanyandula.nyasa.ui.theme.ThemePreference
import com.kanyandula.nyasa.ui.theme.ThemePreferenceManager
import com.kanyandula.nyasa.ui.theme.window.LocalWindow
import com.kanyandula.nyasa.util.analytics.AnalyticsTracker
import com.kanyandula.nyasa.util.analytics.LocalAnalyticsTracker
import com.kanyandula.nyasa.work.UploadKeys
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import com.kanyandula.nyasa.R as AppR

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

                    val startDestination: Any = remember {
                        if (sessionManager.cachedToken.value.isValid()) {
                            Routes.MainGraph
                        } else {
                            Routes.AuthGraph
                        }
                    }

                    LaunchedEffect(token) {
                        analyticsTracker.setUserId(token?.account_pk?.toString())
                        val signedIn = token.isValid()
                        val target: Any = if (signedIn) Routes.MainGraph else Routes.AuthGraph
                        // Reified isInGraph<T> forces the branch on the type; reuse `target` for navigate().
                        val inTargetGraph = if (signedIn) {
                            navController.currentDestination.isInGraph<Routes.MainGraph>()
                        } else {
                            navController.currentDestination.isInGraph<Routes.AuthGraph>()
                        }
                        if (!inTargetGraph) {
                            navController.navigate(target) {
                                popUpTo(navController.graph.id) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }

                    UploadCompletionToasts()
                    AuthEventHandler(navController)

                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val showNav = navBackStackEntry?.destination.isInGraph<Routes.MainGraph>()

                    val onThemeChanged: (ThemePreference) -> Unit = { preference ->
                        themeScope.launch {
                            ThemePreferenceManager.setTheme(themeDataStore, preference)
                        }
                    }

                    val isMedium by LocalWindow.current.isMediumWindowAsState()
                    Scaffold(
                        bottomBar = {
                            if (showNav && !isMedium) NyasaBottomBar(navController = navController)
                        }
                    ) { padding ->
                        Row(
                            Modifier
                                .padding(padding)
                                .fillMaxSize()
                        ) {
                            if (showNav && isMedium) NyasaSideRail(navController = navController)
                            RootNavHost(
                                navController = navController,
                                startDestination = startDestination,
                                currentTheme = themePreference,
                                onThemeChanged = onThemeChanged,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RootNavHost(
    navController: NavHostController,
    startDestination: Any,
    currentTheme: ThemePreference,
    onThemeChanged: (ThemePreference) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        rootNavGraph(
            navController = navController,
            currentTheme = currentTheme,
            onThemeChanged = onThemeChanged
        )
    }
}

@Composable
private fun UploadCompletionToasts() {
    val context = LocalContext.current
    val reportedUploads = remember { mutableSetOf<UUID>() }
    LaunchedEffect(Unit) {
        WorkManager.getInstance(context)
            .getWorkInfosByTagFlow(UploadKeys.WORK_TAG_UPLOAD)
            .drop(1) // skip the historical snapshot WorkManager emits on subscription
            .collect { infos ->
                infos.asSequence()
                    .filter { it.state.isFinished && reportedUploads.add(it.id) }
                    .forEach { toastForUploadState(context, it.state) }
            }
    }
}

private fun toastForUploadState(
    context: android.content.Context,
    state: WorkInfo.State
) {
    val resId = when (state) {
        WorkInfo.State.SUCCEEDED -> AppR.string.upload_completed_success
        WorkInfo.State.FAILED -> AppR.string.upload_completed_failed
        WorkInfo.State.CANCELLED -> AppR.string.upload_completed_cancelled
        else -> return
    }
    Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show()
}
