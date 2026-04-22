package com.kanyandula.nyasa.domain.repository

import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAccountProperties(): Flow<Resource<AccountProperties>>
    fun saveAccountProperties(accountProperties: AccountProperties): Flow<Resource<String>>
    fun updatePassword(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): Flow<Resource<String>>
}
