package com.kanyandula.nyasa.ui.auth


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.FragmentLauncherBinding
import com.kanyandula.nyasa.databinding.FragmentLoginBinding
import com.kanyandula.nyasa.ui.auth.state.AuthStateEvent.*
import com.kanyandula.nyasa.ui.auth.state.LoginFields
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class LoginFragment : BaseAuthFragment() {

    private var _binding: FragmentLoginBinding? =null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)
        Log.d(TAG, "LoginFragment: ${viewModel}")
        subscribeObservers()

        binding.loginButton.setOnClickListener {
            login()
        }
    }


    fun subscribeObservers(){
        viewModel.viewState.observe(viewLifecycleOwner, Observer{
            it.loginFields?.let{
                it.login_email?.let{binding.inputEmail.setText(it)}
                it.login_password?.let{binding.inputPassword.setText(it)}
            }
        })
    }

    fun login(){
        viewModel.setStateEvent(
            LoginAttemptEvent(
                binding.inputEmail.text.toString(),
                binding.inputPassword.text.toString()

            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.setLoginFields(
            LoginFields(
                binding.inputEmail.text.toString(),
                binding.inputPassword.text.toString()
            )
        )
    }





}