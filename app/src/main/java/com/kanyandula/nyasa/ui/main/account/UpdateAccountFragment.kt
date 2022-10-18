package com.kanyandula.nyasa.ui.main.account

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer

import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.FragmentUpdateAccountBinding
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.ui.main.account.state.AccountStateEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class UpdateAccountFragment : BaseAccountFragment<FragmentUpdateAccountBinding>(FragmentUpdateAccountBinding::inflate){




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        subscribeObservers()
    }

    private fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer{ dataState ->
            stateChangeListener.onDataStateChange(dataState)
            Log.d(TAG, "UpdateAccountFragment, DataState: ${dataState}")
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer{ viewState ->
            if(viewState != null){
                viewState.accountProperties?.let{
                    Log.d(TAG, "UpdateAccountFragment, ViewState: ${it}")
                    setAccountDataFields(it)
                }
            }
        })
    }

    private fun setAccountDataFields(accountProperties: AccountProperties){
        if(
            binding?.inputEmail
                ?.text.isNullOrBlank()){
            binding?.inputEmail
                ?.setText(accountProperties.email)
        }
        if(
            binding?.inputUsername
                ?.text.isNullOrBlank()){
            binding?.inputUsername
                ?.setText(accountProperties.username)
        }
    }

    private fun saveChanges(){
        viewModel.setStateEvent(
            AccountStateEvent.UpdateAccountPropertiesEvent(
                binding?.inputEmail
                    ?.text.toString(),
                binding?.inputUsername
                    ?.text.toString()
            )
        )
        stateChangeListener.hideSoftKeyboard()
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_menu, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save -> {
                saveChanges()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}