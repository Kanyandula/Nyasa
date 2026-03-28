package com.kanyandula.nyasa.ui.auth

import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.repository.auth.AuthRepository
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.DataState
import com.kanyandula.nyasa.ui.Loading
import com.kanyandula.nyasa.ui.auth.state.AuthStateEvent
import com.kanyandula.nyasa.ui.auth.state.AuthStateEvent.CheckPreviousAuthEvent
import com.kanyandula.nyasa.ui.auth.state.AuthStateEvent.LoginAttemptEvent
import com.kanyandula.nyasa.ui.auth.state.AuthStateEvent.None
import com.kanyandula.nyasa.ui.auth.state.AuthStateEvent.RegisterAttemptEvent
import com.kanyandula.nyasa.ui.auth.state.AuthViewState
import com.kanyandula.nyasa.ui.auth.state.LoginFields
import com.kanyandula.nyasa.ui.auth.state.RegistrationFields
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject
constructor(
    private val authRepository: AuthRepository
) : BaseViewModel<AuthStateEvent, AuthViewState>() {
    override fun handleStateEvent(stateEvent: AuthStateEvent): Flow<DataState<AuthViewState>> {
        return when (stateEvent) {
            is LoginAttemptEvent -> {
                authRepository.attemptLogin(
                    stateEvent.email,
                    stateEvent.password
                )
            }

            is RegisterAttemptEvent -> {
                authRepository.attemptRegistration(
                    stateEvent.email,
                    stateEvent.username,
                    stateEvent.password,
                    stateEvent.confirm_password
                )
            }

            is CheckPreviousAuthEvent -> {
                authRepository.checkPreviousAuthUser()
            }

            is None -> {
                flowOf(DataState(null, Loading(false), null))
            }
        }
    }

    override fun initNewViewState(): AuthViewState {
        return AuthViewState()
    }

    fun setRegistrationFields(registrationFields: RegistrationFields) {
        val update = getCurrentViewStateOrNew()
        if (update.registrationFields == registrationFields) {
            return
        }
        update.registrationFields = registrationFields
        setViewState(update)
    }

    fun setLoginFields(loginFields: LoginFields) {
        val update = getCurrentViewStateOrNew()
        if (update.loginFields == loginFields) {
            return
        }
        update.loginFields = loginFields
        setViewState(update)
    }

    fun setAuthToken(authToken: AuthToken) {
        val update = getCurrentViewStateOrNew()
        if (update.authToken == authToken) {
            return
        }
        update.authToken = authToken
        setViewState(update)
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
