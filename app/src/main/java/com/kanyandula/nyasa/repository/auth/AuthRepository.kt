package com.kanyandula.nyasa.repository.auth

import android.content.SharedPreferences
import android.util.Log
import com.kanyandula.nyasa.api.auth.NyasaBlogApiAuthService
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.persistance.AccountPropertiesDao
import com.kanyandula.nyasa.persistance.AuthTokenDao
import com.kanyandula.nyasa.repository.emitApiError
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.DataState
import com.kanyandula.nyasa.ui.Response
import com.kanyandula.nyasa.ui.ResponseType
import com.kanyandula.nyasa.ui.auth.state.AuthViewState
import com.kanyandula.nyasa.util.ApiSuccessResponse
import com.kanyandula.nyasa.util.Constants.NETWORK_TIMEOUT
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_SAVE_ACCOUNT_PROPERTIES
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_SAVE_AUTH_TOKEN
import com.kanyandula.nyasa.util.ErrorHandling.GENERIC_AUTH_ERROR
import com.kanyandula.nyasa.util.ErrorHandling.UNABLE_TODO_OPERATION_WO_INTERNET
import com.kanyandula.nyasa.util.InputValidation
import com.kanyandula.nyasa.util.PreferenceKeys
import com.kanyandula.nyasa.util.SuccessHandling.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import com.kanyandula.nyasa.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    private val authTokenDao: AuthTokenDao,
    private val accountPropertiesDao: AccountPropertiesDao,
    private val nyasaBlogApiAuthService: NyasaBlogApiAuthService,
    private val sessionManager: SessionManager,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
) {

    fun attemptLogin(email: String, password: String): Flow<DataState<AuthViewState>> = flow {
        emit(DataState.loading<AuthViewState>(isLoading = true))

        val loginFieldError = InputValidation.validateLoginFields(email, password)
        if (loginFieldError != null) {
            emit(DataState.error<AuthViewState>(Response(loginFieldError, ResponseType.Dialog())))
            return@flow
        }

        if (!sessionManager.isConnectedToTheInternet()) {
            emit(DataState.apiError<AuthViewState>(UNABLE_TODO_OPERATION_WO_INTERNET))
            return@flow
        }

        val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
            safeApiCall { nyasaBlogApiAuthService.login(email, password) }
        }

        when (response) {
            is ApiSuccessResponse -> {
                Log.d(TAG, "handleApiSuccessResponse: $response")

                if (response.body.response == GENERIC_AUTH_ERROR) {
                    emit(DataState.apiError<AuthViewState>(response.body.errorMessage))
                    return@flow
                }

                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(response.body.pk, response.body.email, "")
                )

                val result = authTokenDao.insert(
                    AuthToken(response.body.pk, response.body.token)
                )
                if (result < 0) {
                    emit(DataState.error<AuthViewState>(Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())))
                    return@flow
                }

                saveAuthenticatedUserToPrefs(email)
                emit(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )
            }
            else -> this.emitApiError<AuthViewState>(response)
        }
    }.flowOn(Dispatchers.IO)

    fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Flow<DataState<AuthViewState>> = flow {
        emit(DataState.loading<AuthViewState>(isLoading = true))

        val registrationFieldError = InputValidation.validateRegistrationFields(
            email,
            username,
            password,
            confirmPassword
        )
        if (registrationFieldError != null) {
            emit(DataState.error<AuthViewState>(Response(registrationFieldError, ResponseType.Dialog())))
            return@flow
        }

        if (!sessionManager.isConnectedToTheInternet()) {
            emit(DataState.apiError<AuthViewState>(UNABLE_TODO_OPERATION_WO_INTERNET))
            return@flow
        }

        val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
            safeApiCall { nyasaBlogApiAuthService.register(email, username, password, confirmPassword) }
        }

        when (response) {
            is ApiSuccessResponse -> {
                Log.d(TAG, "handleApiSuccessResponse: $response")

                if (response.body.response == GENERIC_AUTH_ERROR) {
                    emit(DataState.apiError<AuthViewState>(response.body.errorMessage))
                    return@flow
                }

                val result1 = accountPropertiesDao.insertAndReplace(
                    AccountProperties(response.body.pk, response.body.email, response.body.username)
                )
                if (result1 < 0) {
                    emit(
                        DataState.error<AuthViewState>(
                            Response(ERROR_SAVE_ACCOUNT_PROPERTIES, ResponseType.Dialog())
                        )
                    )
                    return@flow
                }

                val result2 = authTokenDao.insert(
                    AuthToken(response.body.pk, response.body.token)
                )
                if (result2 < 0) {
                    emit(DataState.error<AuthViewState>(Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())))
                    return@flow
                }

                saveAuthenticatedUserToPrefs(email)
                emit(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )
            }
            else -> this.emitApiError<AuthViewState>(response)
        }
    }.flowOn(Dispatchers.IO)

    fun checkPreviousAuthUser(): Flow<DataState<AuthViewState>> = flow {
        emit(DataState.loading<AuthViewState>(isLoading = true))

        val previousAuthUserEmail: String? =
            sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)

        if (previousAuthUserEmail.isNullOrBlank()) {
            Log.d(TAG, "checkPreviousAuthUser: No previously authenticated user found.")
            emit(
                DataState.data<AuthViewState>(
                    null,
                    Response(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE, ResponseType.None())
                )
            )
            return@flow
        }

        val accountProperties = accountPropertiesDao.searchByEmail(previousAuthUserEmail)
        Log.d(TAG, "checkPreviousAuthUser: searching for token... account properties: $accountProperties")

        if (accountProperties == null || accountProperties.pk <= -1) {
            Log.d(TAG, "checkPreviousAuthUser: AuthToken not found...")
            emit(
                DataState.data<AuthViewState>(
                    null,
                    Response(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE, ResponseType.None())
                )
            )
            return@flow
        }

        val authToken = authTokenDao.searchByPk(accountProperties.pk)
        if (authToken?.token != null) {
            emit(DataState.data(AuthViewState(authToken = authToken)))
        } else {
            Log.d(TAG, "checkPreviousAuthUser: AuthToken not found...")
            emit(
                DataState.data<AuthViewState>(
                    null,
                    Response(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE, ResponseType.None())
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    private fun saveAuthenticatedUserToPrefs(email: String) {
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
    }

    companion object {
        private const val TAG = "AppDebug"
    }
}
