package com.kanyandula.nyasa.ui.auth

import androidx.lifecycle.viewModelScope
import com.kanyandula.nyasa.domain.usecase.auth.CheckPreviousAuthUseCase
import com.kanyandula.nyasa.domain.usecase.auth.LoginUseCase
import com.kanyandula.nyasa.domain.usecase.auth.RegisterUseCase
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.auth.state.AuthViewState
import com.kanyandula.nyasa.ui.auth.state.LoginFields
import com.kanyandula.nyasa.ui.auth.state.RegistrationFields
import com.kanyandula.nyasa.util.analytics.AnalyticsEvent
import com.kanyandula.nyasa.util.analytics.AnalyticsTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
@Inject
constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val checkPreviousAuthUseCase: CheckPreviousAuthUseCase,
    private val sessionManager: SessionManager,
    private val analyticsTracker: AnalyticsTracker
) : BaseViewModel<AuthViewState>(AuthViewState()) {

    private var hasCheckedPreviousUser = false

    fun attemptLogin(email: String, password: String) {
        viewModelScope.launch {
            loginUseCase(email, password).collect { resource ->
                handleResource(
                    resource,
                    onSuccess = { authToken ->
                        activateSession(authToken)
                        analyticsTracker.trackEvent(AnalyticsEvent.Login)
                    }
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
            registerUseCase(email, username, password, confirmPassword)
                .collect { resource ->
                    handleResource(
                        resource,
                        onSuccess = { authToken ->
                            activateSession(authToken)
                            analyticsTracker.trackEvent(AnalyticsEvent.Register)
                        }
                    )
                }
        }
    }

    fun checkPreviousAuthUser() {
        if (hasCheckedPreviousUser) return
        hasCheckedPreviousUser = true
        viewModelScope.launch {
            checkPreviousAuthUseCase().collect { resource ->
                handleResource(
                    resource,
                    onSuccess = { authToken -> authToken?.let(::activateSession) }
                )
            }
        }
    }

    /**
     * Pushes a freshly-issued [authToken] into [SessionManager] (which
     * `MainActivity` observes to navigate into the main graph) and mirrors
     * it into [AuthViewState] for any UI consumers. Owning the side effect
     * here makes it impossible for an auth screen to silently miss it —
     * the bug that previously kept LOGIN/REGISTER from navigating after
     * a successful sign-in.
     */
    private fun activateSession(authToken: AuthToken) {
        sessionManager.login(authToken)
        setAuthToken(authToken)
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
