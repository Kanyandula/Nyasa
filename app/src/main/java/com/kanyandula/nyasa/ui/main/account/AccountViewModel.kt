package com.kanyandula.nyasa.ui.main.account

import androidx.lifecycle.LiveData
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.repository.main.AccountRepository
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.DataState
import com.kanyandula.nyasa.ui.Loading
import com.kanyandula.nyasa.ui.auth.state.AuthStateEvent
import com.kanyandula.nyasa.ui.main.account.state.AccountStateEvent
import com.kanyandula.nyasa.ui.main.account.state.AccountStateEvent.*
import com.kanyandula.nyasa.ui.main.account.state.AccountViewState
import com.kanyandula.nyasa.util.AbsentLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class AccountViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val accountRepository: AccountRepository
)
    : BaseViewModel<AccountStateEvent, AccountViewState>()
{
    override fun handleStateEvent(stateEvent: AccountStateEvent): LiveData<DataState<AccountViewState>> {
        when(stateEvent){

            is GetAccountPropertiesEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.getAccountProperties(authToken)
                }?: AbsentLiveData.create()
            }

            is UpdateAccountPropertiesEvent ->{
                return sessionManager.cachedToken.value?.let { authToken ->
                    authToken.account_pk?.let { pk ->
                        val newAccountProperties = AccountProperties(
                            pk,
                            stateEvent.email,
                            stateEvent.username
                        )
                        accountRepository.saveAccountProperties(
                            authToken,
                            newAccountProperties
                        )
                    }
                }?: AbsentLiveData.create()
            }

            is ChangePasswordEvent ->{
                return sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.updatePassword(
                        authToken,
                        stateEvent.currentPassword,
                        stateEvent.newPassword,
                        stateEvent.confirmNewPassword
                    )
                }?: AbsentLiveData.create()
            }

            is None ->{
                return object: LiveData<DataState<AccountViewState>>(){
                    override fun onActive() {
                        super.onActive()
                        value = DataState(null, Loading(false), null)
                    }
                }
            }
        }
    }

    fun setAccountPropertiesData(accountProperties: AccountProperties){
        val update = getCurrentViewStateOrNew()
        if(update.accountProperties == accountProperties){
            return
        }
        update.accountProperties = accountProperties
        setViewState(update)
    }

    override fun initNewViewState(): AccountViewState {
        return AccountViewState()
    }

    fun logout(){
        sessionManager.logout()
    }

    fun cancelActiveJobs(){
        accountRepository.cancelActiveJobs() // cancel active jobs
        handlePendingData() // hide progress bar
    }

    fun handlePendingData(){
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}


