package com.kanyandula.nyasa.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.FragmentLoginBinding
import com.kanyandula.nyasa.ui.auth.state.LoginFields
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseAuthFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "LoginFragment: $viewModel")
        subscribeObservers()

        binding?.loginButton?.setOnClickListener {
            login()
        }
        binding?.forgotPassword?.setOnClickListener {
            navForgotPassword()
        }
    }

    private fun navForgotPassword() {
        findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
    }

    private fun subscribeObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { state ->
                    state.loginFields?.let {
                        binding?.apply {
                            it.login_email?.let { inputEmail.setText(it) }
                        }
                    }
                }
            }
        }
    }

    private fun login() {
        viewModel.attemptLogin(
            binding?.inputEmail?.text.toString(),
            binding?.inputPassword?.text.toString()
        )
    }

    override fun onDestroyView() {
        viewModel.setLoginFields(
            LoginFields(
                binding?.inputEmail?.text.toString()
            )
        )
        super.onDestroyView()
    }
}
