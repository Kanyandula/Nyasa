package com.kanyandula.nyasa.ui.main.account

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
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
        setupMenu()
        subscribeObservers()
    }

    private fun subscribeObservers(){
        viewModel.dataState.observe(viewLifecycleOwner, Observer{ dataState ->
            if (dataState != null){
                stateChangeListener.onDataStateChange(dataState)
                Log.d(TAG, "UpdateAccountFragment, DataState: ${dataState}")
            }

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

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.update_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Validate and handle the selected menu item
                when(menuItem.itemId){
                    R.id.save -> {
                        saveChanges()
                        return true
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    
}