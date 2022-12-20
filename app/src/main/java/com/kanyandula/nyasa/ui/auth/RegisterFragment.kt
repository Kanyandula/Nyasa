package com.kanyandula.nyasa.ui.auth


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.FragmentRegisterBinding
import com.kanyandula.nyasa.ui.auth.state.AuthStateEvent.*
import com.kanyandula.nyasa.ui.auth.state.RegistrationFields
import dagger.hilt.android.AndroidEntryPoint

import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class RegisterFragment : BaseAuthFragment<FragmentRegisterBinding>(FragmentRegisterBinding::inflate) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        Log.d(TAG, "RegisterFragment: $viewModel")
        binding?.registerButton
            ?.setOnClickListener {
                register()
            }

        subscribeObservers()

    }

    private fun subscribeObservers(){
        viewModel.viewState.observe(viewLifecycleOwner, Observer{viewState ->
            viewState.registrationFields?.let {
                binding?.apply {
                    it.registration_email?.let{ inputEmail.setText(it) }
                    it.registration_username?.let{ inputUsername.setText(it) }
                    it.registration_password?.let{ inputPassword.setText(it) }
                    it.registration_confirm_password?.let{ inputPasswordConfirm.setText(it) }
                }

            }
        })
    }

    fun register(){
        viewModel.setStateEvent(
            RegisterAttemptEvent(

                binding?.inputEmail?.text.toString(),
                binding?.inputUsername?.text.toString(),
                binding?.inputPassword?.text.toString(),
                binding?.inputPasswordConfirm?.text.toString()

            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewModel.setRegistrationFields(
            RegistrationFields(
                binding?.inputEmail?.text.toString(),
                binding?.inputUsername?.text.toString(),
                binding?.inputPassword?.text.toString(),
                binding?.inputPasswordConfirm?.text.toString()
            )
        )
    }




}