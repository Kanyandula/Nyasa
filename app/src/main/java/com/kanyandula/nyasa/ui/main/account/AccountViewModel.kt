package com.kanyandula.nyasa.ui.main.account

import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.repository.main.AccountRepository
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.DataState
import com.kanyandula.nyasa.ui.Loading
import com.kanyandula.nyasa.ui.main.account.state.AccountStateEvent
import com.kanyandula.nyasa.ui.main.account.state.AccountStateEvent.ChangePasswordEvent
import com.kanyandula.nyasa.ui.main.account.state.AccountStateEvent.GetAccountPropertiesEvent
import com.kanyandula.nyasa.ui.main.account.state.AccountStateEvent.None
import com.kanyandula.nyasa.ui.main.account.state.AccountStateEvent.UpdateAccountPropertiesEvent
import com.kanyandula.nyasa.ui.main.account.state.AccountViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class AccountViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val accountRepository: AccountRepository
) :
    BaseViewModel<AccountStateEvent, AccountViewState>() {
    override fun handleStateEvent(stateEvent: AccountStateEvent): Flow<DataState<AccountViewState>> {
        return when (stateEvent) {
            is GetAccountPropertiesEvent -> {
                sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.getAccountProperties(authToken)
                } ?: flowOf(DataState(null, Loading(false), null))
            }

            is UpdateAccountPropertiesEvent -> {
                sessionManager.cachedToken.value?.let { authToken ->
                    authToken.account_pk?.let { pk ->
                        val newAccountProperties = AccountProperties(
                            pk,
                            stateEvent.email,
                            stateEvent.username
                        )
                        accountRepository.saveAccountProperties(
                            newAccountProperties
                        )
                    }
                } ?: flowOf(DataState(null, Loading(false), null))
            }

            is ChangePasswordEvent -> {
                accountRepository.updatePassword(
                    stateEvent.currentPassword,
                    stateEvent.newPassword,
                    stateEvent.confirmNewPassword
                )
            }

            is None -> {
                flowOf(DataState(null, Loading(false), null))
            }
        }
    }

    fun setAccountPropertiesData(accountProperties: AccountProperties) {
        val update = getCurrentViewStateOrNew()
        if (update.accountProperties == accountProperties) {
            return
        }
        update.accountProperties = accountProperties
        setViewState(update)
    }

    override fun initNewViewState(): AccountViewState {
        return AccountViewState()
    }

    fun logout() {
        sessionManager.logout()
    }

    fun cancelActiveJobs() {
        handlePendingData()
    }

    fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}
