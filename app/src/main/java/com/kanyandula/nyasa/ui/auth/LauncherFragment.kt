package com.kanyandula.nyasa.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.FragmentLauncherBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LauncherFragment :  Fragment(R.layout.fragment_launcher) {

    private var _binding: FragmentLauncherBinding? =null
    private val binding get() = _binding!!



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLauncherBinding.bind(view)


        binding.register.setOnClickListener {
            navRegistration()
        }

        binding.login.setOnClickListener {
            navLogin()
        }

        binding.forgotPassword.setOnClickListener {
            navForgotPassword()
        }

        binding.focusableView.requestFocus() // reset focus
    }

    fun navLogin(){
        findNavController().navigate(R.id.action_launcherFragment_to_loginFragment)
    }

    fun navRegistration(){
        findNavController().navigate(R.id.action_launcherFragment_to_registerFragment)
    }

    fun navForgotPassword(){
        findNavController().navigate(R.id.action_launcherFragment_to_forgotPasswordFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

