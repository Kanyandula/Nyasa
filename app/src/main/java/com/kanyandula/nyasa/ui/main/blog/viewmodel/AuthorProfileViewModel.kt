package com.kanyandula.nyasa.ui.main.blog.viewmodel

import androidx.lifecycle.viewModelScope
import com.kanyandula.nyasa.domain.usecase.profile.GetProfileUseCase
import com.kanyandula.nyasa.ui.BaseViewModel
import com.kanyandula.nyasa.ui.main.blog.state.AuthorProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthorProfileViewModel
@Inject
constructor(
    private val getProfileUseCase: GetProfileUseCase
) : BaseViewModel<AuthorProfileUiState>(AuthorProfileUiState()) {

    fun loadProfile(username: String) {
        if (viewState.value.profile?.username == username) return
        viewModelScope.launch {
            getProfileUseCase(username).collect { resource ->
                handleResource(
                    resource,
                    onSuccess = { profile ->
                        updateState { copy(profile = profile) }
                    }
                )
            }
        }
    }
}
