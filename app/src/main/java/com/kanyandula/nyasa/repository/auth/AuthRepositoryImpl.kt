package com.kanyandula.nyasa.repository.auth

import android.content.SharedPreferences
import android.util.Log
import com.kanyandula.nyasa.api.auth.NyasaBlogApiAuthService
import com.kanyandula.nyasa.domain.repository.AuthRepository
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.persistance.AccountPropertiesDao
import com.kanyandula.nyasa.persistance.AuthTokenDao
import com.kanyandula.nyasa.repository.apiErrorMessage
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.ApiSuccessResponse
import com.kanyandula.nyasa.util.Constants.NETWORK_TIMEOUT
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_SAVE_ACCOUNT_PROPERTIES
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_SAVE_AUTH_TOKEN
import com.kanyandula.nyasa.util.ErrorHandling.GENERIC_AUTH_ERROR
import com.kanyandula.nyasa.util.ErrorHandling.UNABLE_TODO_OPERATION_WO_INTERNET
import com.kanyandula.nyasa.util.InputValidation
import com.kanyandula.nyasa.util.PreferenceKeys
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class AuthRepositoryImpl
@Inject
constructor(
    private val authTokenDao: AuthTokenDao,
    private val accountPropertiesDao: AccountPropertiesDao,
    private val nyasaBlogApiAuthService: NyasaBlogApiAuthService,
    private val connectivityObserver: ConnectivityObserver,
    private val sharedPreferences: SharedPreferences,
    private val sharedPrefsEditor: SharedPreferences.Editor
) : AuthRepository {

    override fun attemptLogin(email: String, password: String): Flow<Resource<AuthToken>> = flow {
        emit(Resource.Loading())

        val loginFieldError = InputValidation.validateLoginFields(email, password)
        if (loginFieldError != null) {
            emit(Resource.Error(loginFieldError))
            return@flow
        }

        if (!connectivityObserver.isConnected.value) {
            emit(Resource.Error(UNABLE_TODO_OPERATION_WO_INTERNET))
            return@flow
        }

        val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
            safeApiCall { nyasaBlogApiAuthService.login(email, password) }
        }

        when (response) {
            is ApiSuccessResponse -> {
                Log.d(TAG, "handleApiSuccessResponse: $response")

                if (response.body.response == GENERIC_AUTH_ERROR) {
                    emit(Resource.Error(response.body.errorMessage))
                    return@flow
                }

                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(response.body.pk, response.body.email, "")
                )

                val result = authTokenDao.insert(
                    AuthToken(response.body.pk, response.body.token)
                )
                if (result < 0) {
                    emit(Resource.Error(ERROR_SAVE_AUTH_TOKEN))
                    return@flow
                }

                saveAuthenticatedUserToPrefs(email)
                emit(Resource.Success(AuthToken(response.body.pk, response.body.token)))
            }
            else -> emit(Resource.Error(apiErrorMessage(response)))
        }
    }.flowOn(Dispatchers.IO)

    override fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Flow<Resource<AuthToken>> = flow {
        emit(Resource.Loading())

        val registrationFieldError = InputValidation.validateRegistrationFields(
            email,
            username,
            password,
            confirmPassword
        )
        if (registrationFieldError != null) {
            emit(Resource.Error(registrationFieldError))
            return@flow
        }

        if (!connectivityObserver.isConnected.value) {
            emit(Resource.Error(UNABLE_TODO_OPERATION_WO_INTERNET))
            return@flow
        }

        val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
            safeApiCall { nyasaBlogApiAuthService.register(email, username, password, confirmPassword) }
        }

        when (response) {
            is ApiSuccessResponse -> {
                Log.d(TAG, "handleApiSuccessResponse: $response")

                if (response.body.response == GENERIC_AUTH_ERROR) {
                    emit(Resource.Error(response.body.errorMessage))
                    return@flow
                }

                val result1 = accountPropertiesDao.insertAndReplace(
                    AccountProperties(response.body.pk, response.body.email, response.body.username)
                )
                if (result1 < 0) {
                    emit(Resource.Error(ERROR_SAVE_ACCOUNT_PROPERTIES))
                    return@flow
                }

                val result2 = authTokenDao.insert(
                    AuthToken(response.body.pk, response.body.token)
                )
                if (result2 < 0) {
                    emit(Resource.Error(ERROR_SAVE_AUTH_TOKEN))
                    return@flow
                }

                saveAuthenticatedUserToPrefs(email)
                emit(Resource.Success(AuthToken(response.body.pk, response.body.token)))
            }
            else -> emit(Resource.Error(apiErrorMessage(response)))
        }
    }.flowOn(Dispatchers.IO)

    override fun checkPreviousAuthUser(): Flow<Resource<AuthToken?>> = flow {
        emit(Resource.Loading())

        val previousAuthUserEmail: String? =
            sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)

        if (previousAuthUserEmail.isNullOrBlank()) {
            Log.d(TAG, "checkPreviousAuthUser: No previously authenticated user found.")
            emit(Resource.Success(null))
            return@flow
        }

        val accountProperties = accountPropertiesDao.searchByEmail(previousAuthUserEmail)
        Log.d(TAG, "checkPreviousAuthUser: searching for token... account properties: $accountProperties")

        if (accountProperties == null || accountProperties.pk <= -1) {
            Log.d(TAG, "checkPreviousAuthUser: AuthToken not found...")
            emit(Resource.Success(null))
            return@flow
        }

        val authToken = authTokenDao.searchByPk(accountProperties.pk)
        if (authToken?.token != null) {
            emit(Resource.Success(authToken))
        } else {
            Log.d(TAG, "checkPreviousAuthUser: AuthToken not found...")
            emit(Resource.Success(null))
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
