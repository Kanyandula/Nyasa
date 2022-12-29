package com.kanyandula.nyasa.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.FragmentLoginBinding
import com.kanyandula.nyasa.ui.auth.state.AuthStateEvent.*
import com.kanyandula.nyasa.ui.auth.state.LoginFields
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class LoginFragment : BaseAuthFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "LoginFragment: ${viewModel}")
        subscribeObservers()

        binding?.loginButton?.setOnClickListener {
            login()
        }
        binding?.forgotPassword?.setOnClickListener {
            navForgotPassword()
        }
    }

    fun navForgotPassword(){
        findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
    }




    fun subscribeObservers(){
        viewModel.viewState.observe(viewLifecycleOwner, Observer{
            it.loginFields?.let{
                binding?.apply {
                    it.login_email?.let{ inputEmail.setText(it) }
                    it.login_password?.let{ inputPassword.setText(it) }
                }

            }
        })
    }

    fun login(){

        viewModel.setStateEvent(

            LoginAttemptEvent(
                binding?.inputEmail?.text.toString(),
                binding?.inputPassword?.text.toString()

            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setLoginFields(
            LoginFields(
                binding?.inputEmail?.text.toString(),
                binding?.inputPassword?.text.toString()
            )
        )
    }



}