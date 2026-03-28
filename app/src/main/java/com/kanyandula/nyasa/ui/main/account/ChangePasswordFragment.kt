package com.kanyandula.nyasa.ui.main.account

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.kanyandula.nyasa.databinding.FragmentChangePasswordBinding
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.ui.main.account.state.AccountUiEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordFragment :
    BaseAccountFragment<FragmentChangePasswordBinding>(
        FragmentChangePasswordBinding::inflate
    ) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.updatePasswordButton?.setOnClickListener {
            viewModel.changePassword(
                binding!!.inputCurrentPassword.text.toString(),
                binding!!.inputNewPassword.text.toString(),
                binding!!.inputConfirmNewPassword.text.toString()
            )
        }
    }

    override fun handleUiEvent(event: UiEvent) {
        when (event) {
            is AccountUiEvent.PasswordChanged -> {
                stateChangeListener.hideSoftKeyboard()
                findNavController().popBackStack()
            }
            else -> super.handleUiEvent(event)
        }
    }
}
