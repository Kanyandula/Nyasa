package com.kanyandula.nyasa.domain.repository

import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun attemptLogin(email: String, password: String): Flow<Resource<AuthToken>>
    fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Flow<Resource<AuthToken>>
    fun checkPreviousAuthUser(): Flow<Resource<AuthToken?>>
}
