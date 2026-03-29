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
import com.kanyandula.nyasa.ui.auth.composables.RegisterScreen
import com.kanyandula.nyasa.ui.auth.state.RegistrationFields
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

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

                    RegisterScreen(
                        initialEmail = state.registrationFields?.registration_email.orEmpty(),
                        initialUsername = state.registrationFields?.registration_username.orEmpty(),
                        isLoading = isLoading,
                        onRegister = { email, username, password, confirmPassword ->
                            viewModel.attemptRegistration(
                                email,
                                username,
                                password,
                                confirmPassword
                            )
                        },
                        onNavigateToLogin = {
                            activity?.onBackPressedDispatcher?.onBackPressed()
                        },
                        onFieldsChanged = { email, username ->
                            viewModel.setRegistrationFields(
                                RegistrationFields(email, username)
                            )
                        }
                    )
                }
            }
        }
    }
}
