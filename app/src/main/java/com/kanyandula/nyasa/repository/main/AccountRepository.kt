package com.kanyandula.nyasa.repository.main

import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.persistance.AccountPropertiesDao
import com.kanyandula.nyasa.repository.emitApiError
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.DataState
import com.kanyandula.nyasa.ui.Response
import com.kanyandula.nyasa.ui.ResponseType
import com.kanyandula.nyasa.ui.main.account.state.AccountViewState
import com.kanyandula.nyasa.util.ApiSuccessResponse
import com.kanyandula.nyasa.util.Constants.NETWORK_TIMEOUT
import com.kanyandula.nyasa.util.ErrorHandling.UNABLE_TODO_OPERATION_WO_INTERNET
import com.kanyandula.nyasa.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class AccountRepository
@Inject
constructor(
    private val openApiMainService: NyasaBlogApiMainService,
    private val accountPropertiesDao: AccountPropertiesDao,
    private val sessionManager: SessionManager
) {

    fun getAccountProperties(authToken: AuthToken): Flow<DataState<AccountViewState>> = flow {
        emit(DataState.loading<AccountViewState>(isLoading = true))

        val cachedAccount = authToken.account_pk?.let { accountPropertiesDao.searchByPk(it) }
        if (cachedAccount != null) {
            emit(
                DataState.loading<AccountViewState>(isLoading = true, cachedData = AccountViewState(cachedAccount))
            )
        }

        if (sessionManager.isConnectedToTheInternet()) {
            val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
                safeApiCall { openApiMainService.getAccountProperties() }
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
                    emit(DataState.data(data = AccountViewState(updatedAccount)))
                }
                else -> this.emitApiError<AccountViewState>(response)
            }
        } else {
            if (cachedAccount != null) {
                emit(DataState.data(data = AccountViewState(cachedAccount)))
            } else {
                emit(DataState.apiError<AccountViewState>(UNABLE_TODO_OPERATION_WO_INTERNET))
            }
        }
    }.flowOn(Dispatchers.IO)

    fun saveAccountProperties(
        accountProperties: AccountProperties
    ): Flow<DataState<AccountViewState>> = flow {
        emit(DataState.loading<AccountViewState>(isLoading = true))

        if (!sessionManager.isConnectedToTheInternet()) {
            emit(DataState.apiError<AccountViewState>(UNABLE_TODO_OPERATION_WO_INTERNET))
            return@flow
        }

        val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
            safeApiCall {
                openApiMainService.saveAccountProperties(
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
                emit(
                    DataState.data<AccountViewState>(
                        data = null,
                        response = Response(response.body.response, ResponseType.Toast())
                    )
                )
            }
            else -> this.emitApiError<AccountViewState>(response)
        }
    }.flowOn(Dispatchers.IO)

    fun updatePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): Flow<DataState<AccountViewState>> = flow {
        emit(DataState.loading<AccountViewState>(isLoading = true))

        if (!sessionManager.isConnectedToTheInternet()) {
            emit(DataState.apiError<AccountViewState>(UNABLE_TODO_OPERATION_WO_INTERNET))
            return@flow
        }

        val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
            safeApiCall {
                openApiMainService.updatePassword(currentPassword, newPassword, confirmNewPassword)
            }
        }

        when (response) {
            is ApiSuccessResponse -> {
                emit(
                    DataState.data<AccountViewState>(
                        null,
                        Response(response.body.response, ResponseType.Toast())
                    )
                )
            }
            else -> this.emitApiError<AccountViewState>(response)
        }
    }.flowOn(Dispatchers.IO)
}
