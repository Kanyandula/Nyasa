package com.kanyandula.nyasa.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.auth.composables.ForgotPasswordScreen
import com.kanyandula.nyasa.ui.auth.composables.LoginScreen
import com.kanyandula.nyasa.ui.auth.composables.RegisterScreen
import com.kanyandula.nyasa.ui.auth.composables.WelcomeScreen
import com.kanyandula.nyasa.ui.auth.state.AuthUiEvent
import com.kanyandula.nyasa.ui.auth.state.LoginFields
import com.kanyandula.nyasa.ui.auth.state.RegistrationFields
import com.kanyandula.nyasa.ui.components.LoadingOverlay
import com.kanyandula.nyasa.ui.main.MainActivity
import com.kanyandula.nyasa.ui.navigation.Routes
import com.kanyandula.nyasa.ui.navigation.handleStandardEvent
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Suppress("LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NyasaTheme {
                val navController = rememberNavController()
                val viewModel: AuthViewModel = hiltViewModel()
                val state by viewModel.viewState.collectAsStateWithLifecycle()
                val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

                LaunchedEffect(Unit) {
                    viewModel.checkPreviousAuthUser()
                }

                LaunchedEffect(state.authToken) {
                    state.authToken?.let { sessionManager.login(it) }
                }

                LaunchedEffect(Unit) {
                    sessionManager.cachedToken.collect { authToken ->
                        if (authToken != null && authToken.account_pk != -1 && authToken.token != null) {
                            navMainActivity()
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    viewModel.events.collect { event ->
                        when (event) {
                            is AuthUiEvent.CheckPreviousAuthDone -> { /* UI now visible */ }
                            else -> handleStandardEvent(this@AuthActivity, event)
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = Routes.WELCOME
                    ) {
                        composable(Routes.WELCOME) {
                            WelcomeScreen(
                                onLoginClick = { navController.navigate(Routes.LOGIN) },
                                onRegisterClick = { navController.navigate(Routes.REGISTER) },
                                onForgotPasswordClick = {
                                    navController.navigate(Routes.FORGOT_PASSWORD)
                                }
                            )
                        }
                        composable(Routes.LOGIN) {
                            LoginScreen(
                                initialEmail = state.loginFields?.login_email.orEmpty(),
                                isLoading = isLoading,
                                onLogin = { email, password ->
                                    viewModel.attemptLogin(email, password)
                                },
                                onForgotPassword = {
                                    navController.navigate(Routes.FORGOT_PASSWORD)
                                },
                                onNavigateToRegister = {
                                    navController.navigate(Routes.REGISTER)
                                },
                                onEmailChanged = { email ->
                                    viewModel.setLoginFields(LoginFields(email))
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(Routes.REGISTER) {
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
                        }
                        composable(Routes.FORGOT_PASSWORD) {
                            ForgotPasswordScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onError = { message ->
                                    Toast.makeText(
                                        this@AuthActivity,
                                        message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onLoadingChanged = { /* loading handled by overlay */ }
                            )
                        }
                    }
                    LoadingOverlay(isLoading = isLoading)
                }
            }
        }
    }

    private fun navMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
