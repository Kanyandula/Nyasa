package com.kanyandula.nyasa.repository.main

import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.domain.repository.AccountRepository
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.persistance.AccountPropertiesDao
import com.kanyandula.nyasa.repository.networkApiFlow
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class AccountRepositoryImpl
@Inject
constructor(
    private val nyasaBlogApiMainService: NyasaBlogApiMainService,
    private val accountPropertiesDao: AccountPropertiesDao,
    private val sessionManager: SessionManager,
    private val connectivityObserver: ConnectivityObserver
) : AccountRepository {

    override fun getAccountProperties(): Flow<Resource<AccountProperties>> = flow {
        val authToken = sessionManager.cachedToken.value
        if (authToken == null) {
            emit(Resource.Error(AppError.Unauthorized))
            return@flow
        }

        emit(Resource.Loading())

        val cachedAccount = authToken.account_pk?.let { accountPropertiesDao.searchByPk(it) }
        if (cachedAccount != null) {
            emit(Resource.Loading(cachedAccount))
        }

        if (connectivityObserver.isConnected.value) {
            when (val result = safeApiCall { nyasaBlogApiMainService.getAccountProperties() }) {
                is Resource.Success -> {
                    accountPropertiesDao.updateAccountProperties(
                        result.data.pk,
                        result.data.email,
                        result.data.username
                    )
                    emit(Resource.Success(result.data))
                }
                is Resource.Error -> emit(result)
                is Resource.Loading -> Unit
            }
        } else {
            if (cachedAccount != null) {
                emit(Resource.Success(cachedAccount))
            } else {
                emit(Resource.Error(AppError.Offline))
            }
        }
    }.flowOn(Dispatchers.IO)

    override fun saveAccountProperties(
        accountProperties: AccountProperties
    ): Flow<Resource<String>> = networkApiFlow(
        connectivityObserver = connectivityObserver,
        apiCall = {
            nyasaBlogApiMainService.saveAccountProperties(
                accountProperties.email,
                accountProperties.username
            )
        },
        onSuccess = { body ->
            accountPropertiesDao.updateAccountProperties(
                accountProperties.pk,
                accountProperties.email,
                accountProperties.username
            )
            Resource.Success(body.response)
        }
    )

    override fun updatePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): Flow<Resource<String>> = networkApiFlow(
        connectivityObserver = connectivityObserver,
        apiCall = {
            nyasaBlogApiMainService.updatePassword(currentPassword, newPassword, confirmNewPassword)
        },
        onSuccess = { body -> Resource.Success(body.response) }
    )
}
