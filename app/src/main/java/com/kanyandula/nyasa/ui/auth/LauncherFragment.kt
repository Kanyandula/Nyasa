package com.kanyandula.nyasa.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.ui.auth.composables.WelcomeScreen
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LauncherFragment : Fragment() {

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
                    WelcomeScreen(
                        onLoginClick = {
                            findNavController().navigate(
                                R.id.action_launcherFragment_to_loginFragment
                            )
                        },
                        onRegisterClick = {
                            findNavController().navigate(
                                R.id.action_launcherFragment_to_registerFragment
                            )
                        },
                        onForgotPasswordClick = {
                            findNavController().navigate(
                                R.id.action_launcherFragment_to_forgotPasswordFragment
                            )
                        }
                    )
                }
            }
        }
    }
}
