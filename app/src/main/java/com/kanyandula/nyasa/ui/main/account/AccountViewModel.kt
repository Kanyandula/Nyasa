package com.kanyandula.nyasa.ui.main.account

import androidx.lifecycle.viewModelScope
import com.kanyandula.nyasa.domain.usecase.account.ChangePasswordUseCase
import com.kanyandula.nyasa.domain.usecase.account.GetAccountPropertiesUseCase
import com.kanyandula.nyasa.domain.usecase.account.SaveAccountPropertiesUseCase
import com.kanyandula.nyasa.domain.usecase.profile.UpdateProfileUseCase
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.models.ProfileUpdateRequest
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.util.analytics.AnalyticsEvent
import com.kanyandula.nyasa.util.analytics.AnalyticsTracker
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
    private val getAccountPropertiesUseCase: GetAccountPropertiesUseCase,
    private val saveAccountPropertiesUseCase: SaveAccountPropertiesUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val analyticsTracker: AnalyticsTracker
) : BaseViewModel<AccountViewState>(AccountViewState()) {

    fun getAccountProperties() {
        viewModelScope.launch {
            getAccountPropertiesUseCase().collect { resource ->
                handleResource(
                    resource,
                    onLoading = { data -> data?.let { setAccountPropertiesData(it) } },
                    onSuccess = { data -> setAccountPropertiesData(data) }
                )
            }
        }
    }

    fun saveAccountProperties(email: String, username: String) {
        val accountProperties = viewState.value.accountProperties ?: return
        val newAccountProperties = AccountProperties(accountProperties.pk, email, username)
        viewModelScope.launch {
            saveAccountPropertiesUseCase(newAccountProperties).collect { resource ->
                handleResource(
                    resource,
                    onSuccess = { message -> sendEvent(UiEvent.ShowToast(message)) }
                )
            }
        }
    }

    fun updateProfile(request: ProfileUpdateRequest) {
        viewModelScope.launch {
            updateProfileUseCase(request).collect { resource ->
                handleResource(
                    resource,
                    onSuccess = {
                        sendEvent(UiEvent.ShowToast("Profile updated"))
                        getAccountProperties()
                        analyticsTracker.trackEvent(AnalyticsEvent.ProfileUpdated)
                    }
                )
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmNewPassword: String) {
        viewModelScope.launch {
            changePasswordUseCase(currentPassword, newPassword, confirmNewPassword)
                .collect { resource ->
                    handleResource(
                        resource,
                        onSuccess = { message ->
                            if (message == RESPONSE_PASSWORD_UPDATE_SUCCESS) {
                                sendEvent(AccountUiEvent.PasswordChanged)
                            }
                            sendEvent(UiEvent.ShowToast(message))
                            analyticsTracker.trackEvent(AnalyticsEvent.PasswordChanged)
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
        analyticsTracker.trackEvent(AnalyticsEvent.Logout)
        sessionManager.logout()
    }
}
