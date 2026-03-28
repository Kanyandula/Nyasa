package com.kanyandula.nyasa.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kanyandula.nyasa.databinding.FragmentRegisterBinding
import com.kanyandula.nyasa.ui.auth.state.RegistrationFields
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : BaseAuthFragment<FragmentRegisterBinding>(FragmentRegisterBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "RegisterFragment: $viewModel")
        binding?.registerButton?.setOnClickListener {
            register()
        }

        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { viewState ->
                    viewState.registrationFields?.let {
                        binding?.apply {
                            it.registration_email?.let { inputEmail.setText(it) }
                            it.registration_username?.let { inputUsername.setText(it) }
                        }
                    }
                }
            }
        }
    }

    private fun register() {
        viewModel.attemptRegistration(
            binding?.inputEmail?.text.toString(),
            binding?.inputUsername?.text.toString(),
            binding?.inputPassword?.text.toString(),
            binding?.inputPasswordConfirm?.text.toString()
        )
    }

    override fun onDestroyView() {
        viewModel.setRegistrationFields(
            RegistrationFields(
                binding?.inputEmail?.text.toString(),
                binding?.inputUsername?.text.toString()
            )
        )
        super.onDestroyView()
    }
}
