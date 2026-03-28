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
import androidx.navigation.fragment.findNavController
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.FragmentAccountBinding
import com.kanyandula.nyasa.models.AccountProperties
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountFragment : BaseAccountFragment<FragmentAccountBinding>(FragmentAccountBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()

        binding?.changePassword?.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_changePasswordFragment)
        }

        binding?.logoutButton?.setOnClickListener {
            viewModel.logout()
        }

        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { viewState ->
                    viewState.accountProperties?.let {
                        Log.d(TAG, "AccountFragment, ViewState: $it")
                        setAccountDataFields(it)
                    }
                }
            }
        }
    }

    private fun setAccountDataFields(accountProperties: AccountProperties) {
        binding?.email?.text = accountProperties.email
        binding?.username?.text = accountProperties.username
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAccountProperties()
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(
            object : MenuProvider {
                override fun onPrepareMenu(menu: Menu) { /* no-op */ }

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.edit_view_menu, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.edit -> {
                            findNavController().navigate(R.id.action_accountFragment_to_updateAccountFragment)
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
