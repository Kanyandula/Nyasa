package com.kanyandula.nyasa.repository.auth

import androidx.lifecycle.LiveData
import com.codingwithmitch.openapi.util.GenericApiResponse
import com.kanyandula.nyasa.api.auth.NyasaBlogAuthService
import com.kanyandula.nyasa.api.auth.network_responses.LoginResponse
import com.kanyandula.nyasa.api.auth.network_responses.RegistrationResponse
import com.kanyandula.nyasa.persistance.AccountPropertiesDao
import com.kanyandula.nyasa.persistance.AuthTokenDao
import com.kanyandula.nyasa.session.SessionManager
import javax.inject.Inject


class AuthRepository @Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val nyasaBlogAuthService: NyasaBlogAuthService,
    val sessionManager: SessionManager

) {

    fun testLoginRequest(email: String, password: String): LiveData<GenericApiResponse<LoginResponse>> {
        return nyasaBlogAuthService.login(email, password)
    }

    fun testRegistrationRequest(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<GenericApiResponse<RegistrationResponse>> {
        return nyasaBlogAuthService.register(email, username, password, confirmPassword)
    }
}