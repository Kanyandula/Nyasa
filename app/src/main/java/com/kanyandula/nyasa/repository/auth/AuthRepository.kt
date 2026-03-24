package com.kanyandula.nyasa.repository.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import com.kanyandula.nyasa.api.auth.NyasaBlogApiAuthService
import com.kanyandula.nyasa.api.auth.network_responses.LoginResponse
import com.kanyandula.nyasa.api.auth.network_responses.RegistrationResponse
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.persistance.AccountPropertiesDao
import com.kanyandula.nyasa.persistance.AuthTokenDao
import com.kanyandula.nyasa.repository.JobManager
import com.kanyandula.nyasa.repository.NetworkBoundResource
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.DataState
import com.kanyandula.nyasa.ui.Response
import com.kanyandula.nyasa.ui.ResponseType
import com.kanyandula.nyasa.ui.auth.state.AuthViewState
import com.kanyandula.nyasa.util.ApiEmptyResponse
import com.kanyandula.nyasa.util.ApiSuccessResponse
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_SAVE_ACCOUNT_PROPERTIES
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_SAVE_AUTH_TOKEN
import com.kanyandula.nyasa.util.ErrorHandling.GENERIC_AUTH_ERROR
import com.kanyandula.nyasa.util.GenericApiResponse
import com.kanyandula.nyasa.util.InputValidation
import com.kanyandula.nyasa.util.PreferenceKeys
import com.kanyandula.nyasa.util.SuccessHandling.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import com.kanyandula.nyasa.util.safeApiCall
import kotlinx.coroutines.Job
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val nyasaBlogApiAuthService: NyasaBlogApiAuthService,
    val sessionManager: SessionManager,
    val sharedPreferences: SharedPreferences,
    val sharedPrefsEditor: SharedPreferences.Editor

) : JobManager("AuthRepository") {

    private val TAG: String = "AppDebug"

    fun attemptLogin(email: String, password: String): LiveData<DataState<AuthViewState>> {
        val loginFieldError = InputValidation.validateLoginFields(email, password)
        if (loginFieldError != null) {
            return returnErrorResponse(loginFieldError, ResponseType.Dialog())
        }

        return object : NetworkBoundResource<LoginResponse, Any, AuthViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {

            override fun loadFromCache(): LiveData<AuthViewState> {
                return object : LiveData<AuthViewState>() {}
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                // no-op
            }

            override suspend fun createCacheRequestAndReturn() {
                // no-op
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                Log.d(TAG, "handleApiSuccessResponse: $response")

                if (response.body.response.equals(GENERIC_AUTH_ERROR)) {
                    return onErrorReturn(response.body.errorMessage, true, false)
                }

                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        ""
                    )
                )

                val result = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )
                if (result < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                        )
                    )
                }

                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )
            }

            override suspend fun createCall(): GenericApiResponse<LoginResponse> {
                return safeApiCall { nyasaBlogApiAuthService.login(email, password) }
            }

            override fun setJob(job: Job) {
                addJob("attemptLogin", job)
            }
        }.asLiveData()
    }

    fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<DataState<AuthViewState>> {
        val registrationFieldError = InputValidation.validateRegistrationFields(
            email,
            username,
            password,
            confirmPassword
        )
        if (registrationFieldError != null) {
            return returnErrorResponse(registrationFieldError, ResponseType.Dialog())
        }

        return object : NetworkBoundResource<RegistrationResponse, Any, AuthViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {
            override fun loadFromCache(): LiveData<AuthViewState> {
                return object : LiveData<AuthViewState>() {}
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                // no-op
            }

            override suspend fun createCacheRequestAndReturn() {
                // no-op
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {
                Log.d(TAG, "handleApiSuccessResponse: $response")

                if (response.body.response.equals(GENERIC_AUTH_ERROR)) {
                    return onErrorReturn(response.body.errorMessage, true, false)
                }

                val result1 = accountPropertiesDao.insertAndReplace(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        response.body.username
                    )
                )

                if (result1 < 0) {
                    onCompleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_ACCOUNT_PROPERTIES, ResponseType.Dialog())
                        )
                    )
                    return
                }

                val result2 = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )
                if (result2 < 0) {
                    onCompleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                        )
                    )
                    return
                }

                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )
            }

            override suspend fun createCall(): GenericApiResponse<RegistrationResponse> {
                return safeApiCall {
                    nyasaBlogApiAuthService.register(email, username, password, confirmPassword)
                }
            }

            override fun setJob(job: Job) {
                addJob("attemptRegistration", job)
            }
        }.asLiveData()
    }

    fun checkPreviousAuthUser(): LiveData<DataState<AuthViewState>> {
        val previousAuthUserEmail: String? =
            sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)

        if (previousAuthUserEmail.isNullOrBlank()) {
            Log.d(TAG, "checkPreviousAuthUser: No previously authenticated user found.")
            return returnNoTokenFound()
        } else {
            return object : NetworkBoundResource<Void, Any, AuthViewState>(
                sessionManager.isConnectedToTheInternet(),
                false,
                false,
                false
            ) {

                override fun loadFromCache(): LiveData<AuthViewState> {
                    return object : LiveData<AuthViewState>() {}
                }

                override suspend fun updateLocalDb(cacheObject: Any?) {
                    // no-op
                }

                override suspend fun createCacheRequestAndReturn() {
                    val accountProperties =
                        accountPropertiesDao.searchByEmail(previousAuthUserEmail)
                    Log.d(
                        TAG,
                        "createCacheRequestAndReturn: searching for token..." +
                            " account properties: $accountProperties"
                    )

                    if (accountProperties == null || accountProperties.pk <= -1) {
                        Log.d(TAG, "createCacheRequestAndReturn: AuthToken not found...")
                        onCompleteJob(
                            DataState.data(
                                null,
                                Response(
                                    RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                    ResponseType.None()
                                )
                            )
                        )
                        return
                    }

                    val authToken = authTokenDao.searchByPk(accountProperties.pk)
                    if (authToken?.token != null) {
                        onCompleteJob(
                            DataState.data(AuthViewState(authToken = authToken))
                        )
                        return
                    }

                    Log.d(TAG, "createCacheRequestAndReturn: AuthToken not found...")
                    onCompleteJob(
                        DataState.data(
                            null,
                            Response(
                                RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                ResponseType.None()
                            )
                        )
                    )
                }

                override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Void>) {
                    // no-op
                }

                override suspend fun createCall(): GenericApiResponse<Void> {
                    return ApiEmptyResponse()
                }

                override fun setJob(job: Job) {
                    addJob("checkPreviousAuthUser", job)
                }
            }.asLiveData()
        }
    }

    private fun saveAuthenticatedUserToPrefs(email: String) {
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
    }

    private fun returnNoTokenFound(): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.data(
                    null,
                    Response(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE, ResponseType.None())
                )
            }
        }
    }

    private fun returnErrorResponse(
        errorMessage: String,
        responseType: ResponseType
    ): LiveData<DataState<AuthViewState>> {
        Log.d(TAG, "returnErrorResponse: $errorMessage")

        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.error(
                    Response(
                        errorMessage,
                        responseType
                    )
                )
            }
        }
    }
}
