package com.kanyandula.nyasa.ui.main.account

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.ui.main.account.state.AccountStateEvent
import com.kanyandula.nyasa.util.SuccessHandling.Companion.RESPONSE_PASSWORD_UPDATE_SUCCESS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_change_password.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class ChangePasswordFragment : BaseAccountFragment(){


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        update_password_button.setOnClickListener {
            viewModel.setStateEvent(
                AccountStateEvent.ChangePasswordEvent(
                    input_current_password.text.toString(),
                    input_new_password.text.toString(),
                    input_confirm_new_password.text.toString()
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
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        })
    }
}







