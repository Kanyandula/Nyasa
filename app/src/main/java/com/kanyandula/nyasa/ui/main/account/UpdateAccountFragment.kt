package com.kanyandula.nyasa.ui.main.account

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.kanyandula.nyasa.ui.DataStateChangeListener
import com.kanyandula.nyasa.ui.handleStandardUiEvent
import com.kanyandula.nyasa.ui.main.account.composables.EditAccountScreen
import com.kanyandula.nyasa.ui.theme.NyasaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpdateAccountFragment : Fragment() {

    private val viewModel: AccountViewModel by activityViewModels()
    lateinit var stateChangeListener: DataStateChangeListener

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

                    LaunchedEffect(Unit) {
                        viewModel.events.collect { event ->
                            handleStandardUiEvent(event, stateChangeListener)
                        }
                    }

                    EditAccountScreen(
                        initialEmail = state.accountProperties?.email.orEmpty(),
                        initialUsername = state.accountProperties?.username.orEmpty(),
                        isLoading = isLoading,
                        onSave = { email, username ->
                            viewModel.saveAccountProperties(email, username)
                            stateChangeListener.hideSoftKeyboard()
                        },
                        onNavigateBack = {
                            findNavController().popBackStack()
                        }
                    )
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            stateChangeListener = context as DataStateChangeListener
        } catch (e: ClassCastException) {
            Log.e("AppDebug", "$context must implement DataStateChangeListener")
        }
    }
}
