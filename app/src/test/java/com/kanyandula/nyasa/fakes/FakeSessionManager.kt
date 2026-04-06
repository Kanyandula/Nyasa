package com.kanyandula.nyasa.fakes

import com.kanyandula.nyasa.models.AuthToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSessionManager {

    private val _cachedToken = MutableStateFlow<AuthToken?>(null)
    val cachedToken: StateFlow<AuthToken?> = _cachedToken.asStateFlow()

    var logoutCallCount = 0
        private set

    fun login(newValue: AuthToken) {
        _cachedToken.value = newValue
    }

    fun logout() {
        logoutCallCount++
        _cachedToken.value = null
    }
}
