package com.kanyandula.nyasa.repository.main

import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.domain.repository.AccountRepository
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.persistance.AccountPropertiesDao
import com.kanyandula.nyasa.repository.apiErrorMessage
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.util.ApiSuccessResponse
import com.kanyandula.nyasa.util.Constants.NETWORK_TIMEOUT
import com.kanyandula.nyasa.util.ErrorHandling.UNABLE_TODO_OPERATION_WO_INTERNET
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class AccountRepositoryImpl
@Inject
constructor(
    private val nyasaBlogApiMainService: NyasaBlogApiMainService,
    private val accountPropertiesDao: AccountPropertiesDao,
    private val sessionManager: SessionManager
) : AccountRepository {

    override fun getAccountProperties(): Flow<Resource<AccountProperties>> = flow {
        val authToken = sessionManager.cachedToken.value
        if (authToken == null) {
            emit(Resource.Error("Not authenticated"))
            return@flow
        }

        emit(Resource.Loading())

        val cachedAccount = authToken.account_pk?.let { accountPropertiesDao.searchByPk(it) }
        if (cachedAccount != null) {
            emit(Resource.Loading(cachedAccount))
        }

        if (sessionManager.isConnectedToTheInternet()) {
            val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
                safeApiCall { nyasaBlogApiMainService.getAccountProperties() }
            }

            when (response) {
                is ApiSuccessResponse -> {
                    accountPropertiesDao.updateAccountProperties(
                        response.body.pk,
                        response.body.email,
                        response.body.username
                    )
                    val updatedAccount = AccountProperties(
                        response.body.pk,
                        response.body.email,
                        response.body.username
                    )
                    emit(Resource.Success(updatedAccount))
                }
                else -> emit(Resource.Error(apiErrorMessage(response)))
            }
        } else {
            if (cachedAccount != null) {
                emit(Resource.Success(cachedAccount))
            } else {
                emit(Resource.Error(UNABLE_TODO_OPERATION_WO_INTERNET))
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun saveAccountProperties(
        accountProperties: AccountProperties
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading())

        if (!sessionManager.isConnectedToTheInternet()) {
            emit(Resource.Error(UNABLE_TODO_OPERATION_WO_INTERNET))
            return@flow
        }

        val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
            safeApiCall {
                nyasaBlogApiMainService.saveAccountProperties(
                    accountProperties.email,
                    accountProperties.username
                )
            }
        }

        when (response) {
            is ApiSuccessResponse -> {
                accountPropertiesDao.updateAccountProperties(
                    accountProperties.pk,
                    accountProperties.email,
                    accountProperties.username
                )
                emit(Resource.Success(response.body.response))
            }
            else -> emit(Resource.Error(apiErrorMessage(response)))
        }
    }.flowOn(Dispatchers.IO)

    override fun updatePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading())

        if (!sessionManager.isConnectedToTheInternet()) {
            emit(Resource.Error(UNABLE_TODO_OPERATION_WO_INTERNET))
            return@flow
        }

        val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
            safeApiCall {
                nyasaBlogApiMainService.updatePassword(currentPassword, newPassword, confirmNewPassword)
            }
        }

        when (response) {
            is ApiSuccessResponse -> {
                emit(Resource.Success(response.body.response))
            }
            else -> emit(Resource.Error(apiErrorMessage(response)))
        }
    }.flowOn(Dispatchers.IO)
}
