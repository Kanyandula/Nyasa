package com.kanyandula.nyasa.ui.main.account

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.kanyandula.nyasa.databinding.FragmentChangePasswordBinding
import com.kanyandula.nyasa.ui.main.account.state.AccountStateEvent
import com.kanyandula.nyasa.util.SuccessHandling.Companion.RESPONSE_PASSWORD_UPDATE_SUCCESS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class ChangePasswordFragment : BaseAccountFragment<FragmentChangePasswordBinding>(FragmentChangePasswordBinding::inflate){



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.updatePasswordButton
            ?.setOnClickListener {
                viewModel.setStateEvent(
                    AccountStateEvent.ChangePasswordEvent(
                        binding!!.inputCurrentPassword
                        .text.toString(),
                        binding!!.inputNewPassword
                        .text.toString(),
                        binding!!.inputConfirmNewPassword
                        .text.toString()
                    )
                )
            }

        subscribeObservers()
    }

    private fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer{ dataState ->
            stateChangeListener.onDataStateChange(dataState)
            Log.d(TAG, "ChangePasswordFragment, DataState: $dataState")
            if(dataState != null){
                dataState.data?.let { data ->
                    data.response?.let{ event ->
                        if(event.peekContent()
                                .message
                                .equals(RESPONSE_PASSWORD_UPDATE_SUCCESS)
                        ){
                            stateChangeListener.hideSoftKeyboard()
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        })
    }
}







