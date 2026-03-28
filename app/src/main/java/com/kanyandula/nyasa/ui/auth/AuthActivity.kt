package com.kanyandula.nyasa.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.ActivityAuthBinding
import com.kanyandula.nyasa.ui.BaseActivity
import com.kanyandula.nyasa.ui.auth.state.AuthUiEvent
import com.kanyandula.nyasa.ui.handleStandardUiEvent
import com.kanyandula.nyasa.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity :
    BaseActivity(),
    NavController.OnDestinationChangedListener {

    private lateinit var binding: ActivityAuthBinding

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        findNavController(R.id.auth_nav_host_fragment).addOnDestinationChangedListener(this)

        subscribeObservers()
        checkPreviousAuthUser()
    }

    override fun onResume() {
        super.onResume()
        // checkPreviousAuthUser already called in onCreate;
        // ViewModel handles deduplication via hasCheckedPreviousUser flag
    }

    private fun subscribeObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.viewState.collect { viewState ->
                        Log.d(TAG, "AuthActivity, subscribeObservers: AuthViewState: $viewState")
                        viewState.authToken?.let {
                            sessionManager.login(it)
                        }
                    }
                }

                launch {
                    sessionManager.cachedToken.collect { authToken ->
                        Log.d(TAG, "AuthActivity, subscribeObservers: AuthToken: $authToken")
                        if (authToken != null && authToken.account_pk != -1 && authToken.token != null) {
                            navMainActivity()
                        }
                    }
                }

                launch {
                    viewModel.isLoading.collect { isLoading ->
                        displayProgressBar(isLoading)
                    }
                }

                launch {
                    viewModel.events.collect { event ->
                        if (event is AuthUiEvent.CheckPreviousAuthDone) {
                            onFinishCheckPreviousAuthUser()
                        } else {
                            handleStandardUiEvent(event, this@AuthActivity)
                        }
                    }
                }
            }
        }
    }

    private fun navMainActivity() {
        Log.d(TAG, "navMainActivity: called.")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun checkPreviousAuthUser() {
        viewModel.checkPreviousAuthUser()
    }

    private fun onFinishCheckPreviousAuthUser() {
        binding.fragmentContainer.visibility = View.VISIBLE
    }

    override fun displayProgressBar(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun expandAppBar() {
        // ignore
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        // no-op: active jobs now cancelled by viewModelScope
    }
}
