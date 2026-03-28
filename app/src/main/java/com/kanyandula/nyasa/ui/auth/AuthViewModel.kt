package com.kanyandula.nyasa.ui.auth

import androidx.lifecycle.viewModelScope
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.repository.auth.AuthRepository
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.auth.state.AuthUiEvent
import com.kanyandula.nyasa.ui.auth.state.AuthViewState
import com.kanyandula.nyasa.ui.auth.state.LoginFields
import com.kanyandula.nyasa.ui.auth.state.RegistrationFields
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject
constructor(
    private val authRepository: AuthRepository
) : BaseViewModel<AuthViewState>(AuthViewState()) {

    private var hasCheckedPreviousUser = false

    fun attemptLogin(email: String, password: String) {
        viewModelScope.launch {
            authRepository.attemptLogin(email, password).collect { resource ->
                handleResource(
                    resource,
                    onSuccess = { authToken -> setAuthToken(authToken) }
                )
            }
        }
    }

    fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ) {
        viewModelScope.launch {
            authRepository.attemptRegistration(email, username, password, confirmPassword)
                .collect { resource ->
                    handleResource(
                        resource,
                        onSuccess = { authToken -> setAuthToken(authToken) }
                    )
                }
        }
    }

    fun checkPreviousAuthUser() {
        if (hasCheckedPreviousUser) return
        hasCheckedPreviousUser = true
        viewModelScope.launch {
            authRepository.checkPreviousAuthUser().collect { resource ->
                handleResource(
                    resource,
                    onSuccess = { authToken ->
                        if (authToken != null) {
                            setAuthToken(authToken)
                        } else {
                            sendEvent(AuthUiEvent.CheckPreviousAuthDone)
                        }
                    }
                )
            }
        }
    }

    fun setRegistrationFields(registrationFields: RegistrationFields) {
        val current = viewState.value
        if (current.registrationFields == registrationFields) return
        updateState { copy(registrationFields = registrationFields) }
    }

    fun setLoginFields(loginFields: LoginFields) {
        val current = viewState.value
        if (current.loginFields == loginFields) return
        updateState { copy(loginFields = loginFields) }
    }

    fun setAuthToken(authToken: AuthToken) {
        val current = viewState.value
        if (current.authToken == authToken) return
        updateState { copy(authToken = authToken) }
    }
}
