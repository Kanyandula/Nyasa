package com.kanyandula.nyasa.ui.main.account

import androidx.lifecycle.viewModelScope
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.repository.main.AccountRepository
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.ui.main.account.state.AccountUiEvent
import com.kanyandula.nyasa.ui.main.account.state.AccountViewState
import com.kanyandula.nyasa.util.SuccessHandling.RESPONSE_PASSWORD_UPDATE_SUCCESS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val accountRepository: AccountRepository
) : BaseViewModel<AccountViewState>(AccountViewState()) {

    fun getAccountProperties() {
        val authToken = sessionManager.cachedToken.value ?: return
        viewModelScope.launch {
            accountRepository.getAccountProperties(authToken).collect { resource ->
                handleResource(
                    resource,
                    onLoading = { data -> data?.let { setAccountPropertiesData(it) } },
                    onSuccess = { data -> setAccountPropertiesData(data) }
                )
            }
        }
    }

    fun saveAccountProperties(email: String, username: String) {
        val authToken = sessionManager.cachedToken.value ?: return
        val pk = authToken.account_pk ?: return
        val newAccountProperties = AccountProperties(pk, email, username)
        viewModelScope.launch {
            accountRepository.saveAccountProperties(newAccountProperties).collect { resource ->
                handleResource(
                    resource,
                    onSuccess = { message -> sendEvent(UiEvent.ShowToast(message)) }
                )
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmNewPassword: String) {
        viewModelScope.launch {
            accountRepository.updatePassword(currentPassword, newPassword, confirmNewPassword)
                .collect { resource ->
                    handleResource(
                        resource,
                        onSuccess = { message ->
                            if (message == RESPONSE_PASSWORD_UPDATE_SUCCESS) {
                                sendEvent(AccountUiEvent.PasswordChanged)
                            }
                            sendEvent(UiEvent.ShowToast(message))
                        }
                    )
                }
        }
    }

    fun setAccountPropertiesData(accountProperties: AccountProperties) {
        val current = viewState.value
        if (current.accountProperties == accountProperties) return
        updateState { copy(accountProperties = accountProperties) }
    }

    fun logout() {
        sessionManager.logout()
    }
}
