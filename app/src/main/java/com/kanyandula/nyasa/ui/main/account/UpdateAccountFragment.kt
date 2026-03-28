package com.kanyandula.nyasa.ui.main.account

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.FragmentUpdateAccountBinding
import com.kanyandula.nyasa.models.AccountProperties
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UpdateAccountFragment : BaseAccountFragment<FragmentUpdateAccountBinding>(FragmentUpdateAccountBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { viewState ->
                    viewState.accountProperties?.let {
                        Log.d(TAG, "UpdateAccountFragment, ViewState: $it")
                        setAccountDataFields(it)
                    }
                }
            }
        }
    }

    private fun setAccountDataFields(accountProperties: AccountProperties) {
        if (binding?.inputEmail?.text.isNullOrBlank()) {
            binding?.inputEmail?.setText(accountProperties.email)
        }
        if (binding?.inputUsername?.text.isNullOrBlank()) {
            binding?.inputUsername?.setText(accountProperties.username)
        }
    }

    private fun saveChanges() {
        viewModel.saveAccountProperties(
            binding?.inputEmail?.text.toString(),
            binding?.inputUsername?.text.toString()
        )
        stateChangeListener.hideSoftKeyboard()
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(
            object : MenuProvider {
                override fun onPrepareMenu(menu: Menu) { /* no-op */ }

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.update_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.save -> {
                            saveChanges()
                            return true
                        }
                    }
                    return true
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }
}
