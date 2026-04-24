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
import com.kanyandula.nyasa.ui.auth.state.AuthUiEvent
import com.kanyandula.nyasa.ui.auth.state.LoginFields
import com.kanyandula.nyasa.ui.auth.state.RegistrationFields
import com.kanyandula.nyasa.ui.components.LoadingOverlay

@Suppress("LongMethod")
fun NavGraphBuilder.authGraph(navController: NavController) {
    navigation(startDestination = Routes.WELCOME, route = Routes.AUTH_GRAPH) {
        composable(Routes.WELCOME) { entry ->
            val parentEntry = remember(entry) {
                navController.getBackStackEntry(Routes.AUTH_GRAPH)
            }
            val viewModel: AuthViewModel = hiltViewModel(parentEntry)
            val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

            val context = LocalContext.current
            LaunchedEffect(Unit) {
                viewModel.checkPreviousAuthUser()
                viewModel.events.collect { event ->
                    when (event) {
                        is AuthUiEvent.CheckPreviousAuthDone -> { /* UI now visible */ }
                        else -> handleStandardEvent(context, event)
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                WelcomeScreen(
                    onLoginClick = { navController.navigate(Routes.LOGIN) },
                    onRegisterClick = { navController.navigate(Routes.REGISTER) },
                    onForgotPasswordClick = {
                        navController.navigate(Routes.FORGOT_PASSWORD)
                    }
                )
                LoadingOverlay(isLoading = isLoading)
            }
        }

        composable(Routes.LOGIN) { entry ->
            val parentEntry = remember(entry) {
                navController.getBackStackEntry(Routes.AUTH_GRAPH)
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
                                navController.navigate(Routes.FORGOT_PASSWORD)
                            is LoginAction.NavigateToRegister ->
                                navController.navigate(Routes.REGISTER)
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

        composable(Routes.REGISTER) { entry ->
            val parentEntry = remember(entry) {
                navController.getBackStackEntry(Routes.AUTH_GRAPH)
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

        composable(Routes.FORGOT_PASSWORD) {
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
