package com.kanyandula.nyasa.session

import android.util.Log
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.persistance.AuthTokenDao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
@Inject
constructor(
    private val authTokenDao: AuthTokenDao
) {

    private val TAG: String = "AppDebug"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _cachedToken = MutableStateFlow<AuthToken?>(null)

    val cachedToken: StateFlow<AuthToken?> = _cachedToken.asStateFlow()

    fun login(newValue: AuthToken) {
        setValue(newValue)
    }

    fun invalidate() {
        _cachedToken.value = null
    }

    @Suppress("TooGenericExceptionCaught")
    fun logout() {
        Log.d(TAG, "logout: ")

        scope.launch(Dispatchers.IO) {
            var errorMessage: String? = null
            try {
                _cachedToken.value?.account_pk?.let {
                    authTokenDao.nullifyToken(it)
                } ?: throw CancellationException("Token Error. Logging out user.")
            } catch (e: CancellationException) {
                Log.e(TAG, "logout: ${e.message}")
                errorMessage = e.message
            } catch (e: Exception) {
                Log.e(TAG, "logout: ${e.message}")
                errorMessage = errorMessage + "\n" + e.message
            } finally {
                errorMessage?.let {
                    Log.e(TAG, "logout: $errorMessage")
                }
                Log.d(TAG, "logout: finally")
                setValue(null)
            }
        }
    }

    private fun setValue(newValue: AuthToken?) {
        _cachedToken.value = newValue
    }
}
