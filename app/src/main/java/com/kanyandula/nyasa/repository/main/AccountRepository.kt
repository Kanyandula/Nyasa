package com.kanyandula.nyasa.repository.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.kanyandula.nyasa.api.GenericResponse
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.persistance.AccountPropertiesDao
import com.kanyandula.nyasa.repository.JobManager
import com.kanyandula.nyasa.repository.NetworkBoundResource
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.DataState
import com.kanyandula.nyasa.ui.Response
import com.kanyandula.nyasa.ui.ResponseType
import com.kanyandula.nyasa.ui.main.account.state.AccountViewState
import com.kanyandula.nyasa.util.ApiSuccessResponse
import com.kanyandula.nyasa.util.GenericApiResponse
import com.kanyandula.nyasa.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AccountRepository
@Inject
constructor(
    val openApiMainService: NyasaBlogApiMainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager
) : JobManager("AccountRepository") {

    fun getAccountProperties(authToken: AuthToken): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            false,
            true
        ) {

            override suspend fun createCacheRequestAndReturn() {
                withContext(Dispatchers.Main) {
                    result.addSource(loadFromCache()) { viewState ->
                        onCompleteJob(DataState.data(viewState, null))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountProperties>) {
                updateLocalDb(response.body)
                createCacheRequestAndReturn()
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return accountPropertiesDao.searchByPk(authToken.account_pk!!)
                    .switchMap {
                        object : LiveData<AccountViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = AccountViewState(it)
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: AccountProperties?) {
                cacheObject?.let {
                    accountPropertiesDao.updateAccountProperties(
                        cacheObject.pk,
                        cacheObject.email,
                        cacheObject.username
                    )
                }
            }

            override suspend fun createCall(): GenericApiResponse<AccountProperties> {
                return safeApiCall { openApiMainService.getAccountProperties() }
            }

            override fun setJob(job: Job) {
                addJob("getAccountProperties", job)
            }
        }.asLiveData()
    }

    fun saveAccountProperties(accountProperties: AccountProperties): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {

            override suspend fun createCacheRequestAndReturn() {
                // no-op
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                updateLocalDb(null)

                withContext(Dispatchers.Main) {
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(response.body.response, ResponseType.Toast())
                        )
                    )
                }
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return object : LiveData<AccountViewState>() {}
            }

            override suspend fun createCall(): GenericApiResponse<GenericResponse> {
                return safeApiCall {
                    openApiMainService.saveAccountProperties(
                        accountProperties.email,
                        accountProperties.username
                    )
                }
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                return accountPropertiesDao.updateAccountProperties(
                    accountProperties.pk,
                    accountProperties.email,
                    accountProperties.username
                )
            }

            override fun setJob(job: Job) {
                addJob("saveAccountProperties", job)
            }
        }.asLiveData()
    }

    fun updatePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {

            override suspend fun createCacheRequestAndReturn() {
                // no-op
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                withContext(Dispatchers.Main) {
                    onCompleteJob(
                        DataState.data(
                            null,
                            Response(response.body.response, ResponseType.Toast())
                        )
                    )
                }
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return object : LiveData<AccountViewState>() {}
            }

            override suspend fun createCall(): GenericApiResponse<GenericResponse> {
                return safeApiCall {
                    openApiMainService.updatePassword(
                        currentPassword,
                        newPassword,
                        confirmNewPassword
                    )
                }
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                // no-op
            }

            override fun setJob(job: Job) {
                addJob("updatePassword", job)
            }
        }.asLiveData()
    }
}
