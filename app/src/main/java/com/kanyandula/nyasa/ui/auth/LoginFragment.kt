package com.kanyandula.nyasa.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.ui.auth.composables.LoginScreen
import com.kanyandula.nyasa.ui.auth.state.LoginFields
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                NyasaTheme {
                    val state by viewModel.viewState.collectAsStateWithLifecycle()
                    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

                    LoginScreen(
                        initialEmail = state.loginFields?.login_email.orEmpty(),
                        isLoading = isLoading,
                        onLogin = { email, password ->
                            viewModel.attemptLogin(email, password)
                        },
                        onForgotPassword = {
                            findNavController().navigate(
                                R.id.action_loginFragment_to_forgotPasswordFragment
                            )
                        },
                        onNavigateToRegister = {
                            findNavController().popBackStack()
                        },
                        onEmailChanged = { email ->
                            viewModel.setLoginFields(LoginFields(email))
                        }
                    )
                }
            }
        }
    }
}
