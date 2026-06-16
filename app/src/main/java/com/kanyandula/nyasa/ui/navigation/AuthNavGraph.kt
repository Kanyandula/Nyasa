package com.kanyandula.nyasa.ui.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.kanyandula.nyasa.ui.auth.AuthViewModel
import com.kanyandula.nyasa.ui.auth.composables.ForgotPasswordScreen
import com.kanyandula.nyasa.ui.auth.composables.LoginAction
import com.kanyandula.nyasa.ui.auth.composables.LoginScreen
import com.kanyandula.nyasa.ui.auth.composables.RegisterScreen
import com.kanyandula.nyasa.ui.auth.composables.WelcomeScreen
import com.kanyandula.nyasa.ui.auth.state.LoginFields
import com.kanyandula.nyasa.ui.auth.state.RegistrationFields
import com.kanyandula.nyasa.ui.components.LoadingOverlay

/**
 * Auth screens deliberately do NOT collect [AuthViewModel.events] — that
 * job is hoisted to [AuthEventHandler], invoked once from `MainActivity`.
 * Adding a new auth screen requires nothing extra for event handling.
 */
@Suppress("LongMethod")
fun NavGraphBuilder.authGraph(navController: NavController) {
    navigation<Routes.AuthGraph>(startDestination = Routes.Welcome) {
        composable<Routes.Welcome> { entry ->
            val parentEntry = remember(entry) {
                navController.getBackStackEntry<Routes.AuthGraph>()
            }
            val viewModel: AuthViewModel = hiltViewModel(parentEntry)
            val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) { viewModel.checkPreviousAuthUser() }

            Box(modifier = Modifier.fillMaxSize()) {
                WelcomeScreen(
                    onLoginClick = { navController.navigate(Routes.Login) },
                    onRegisterClick = { navController.navigate(Routes.Register) },
                    onForgotPasswordClick = {
                        navController.navigate(Routes.ForgotPassword)
                    }
                )
                LoadingOverlay(isLoading = isLoading)
            }
        }

        composable<Routes.Login> { entry ->
            val parentEntry = remember(entry) {
                navController.getBackStackEntry<Routes.AuthGraph>()
            }
            val viewModel: AuthViewModel = hiltViewModel(parentEntry)
            val state by viewModel.viewState.collectAsStateWithLifecycle()
            val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

            Box(modifier = Modifier.fillMaxSize()) {
                LoginScreen(
                    initialEmail = state.loginFields?.login_email.orEmpty(),
                    isLoading = isLoading,
                    onAction = { action ->
                        when (action) {
                            is LoginAction.Login ->
                                viewModel.attemptLogin(action.email, action.password)
                            is LoginAction.ForgotPassword ->
                                navController.navigate(Routes.ForgotPassword)
                            is LoginAction.NavigateToRegister ->
                                navController.navigate(Routes.Register)
                            is LoginAction.EmailChanged ->
                                viewModel.setLoginFields(LoginFields(action.email))
                            is LoginAction.NavigateBack ->
                                navController.popBackStack()
                        }
                    }
                )
                LoadingOverlay(isLoading = isLoading)
            }
        }

        composable<Routes.Register> { entry ->
            val parentEntry = remember(entry) {
                navController.getBackStackEntry<Routes.AuthGraph>()
            }
            val viewModel: AuthViewModel = hiltViewModel(parentEntry)
            val state by viewModel.viewState.collectAsStateWithLifecycle()
            val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

            Box(modifier = Modifier.fillMaxSize()) {
                RegisterScreen(
                    initialEmail = state.registrationFields
                        ?.registration_email.orEmpty(),
                    initialUsername = state.registrationFields
                        ?.registration_username.orEmpty(),
                    isLoading = isLoading,
                    onRegister = { email, username, password, confirmPassword ->
                        viewModel.attemptRegistration(
                            email,
                            username,
                            password,
                            confirmPassword
                        )
                    },
                    onNavigateToLogin = { navController.popBackStack() },
                    onFieldsChanged = { email, username ->
                        viewModel.setRegistrationFields(
                            RegistrationFields(email, username)
                        )
                    }
                )
                LoadingOverlay(isLoading = isLoading)
            }
        }

        composable<Routes.ForgotPassword> {
            val context = LocalContext.current
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onError = { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                },
                onLoadingChanged = { /* loading handled by overlay */ }
            )
        }
    }
}
