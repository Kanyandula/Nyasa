package com.kanyandula.nyasa.repository.auth

import com.kanyandula.nyasa.api.auth.NyasaBlogAuthService
import com.kanyandula.nyasa.persistance.AccountPropertiesDao
import com.kanyandula.nyasa.persistance.AuthTokenDao
import com.kanyandula.nyasa.session.SessionManager

class AuthRepository
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val nyasaBlogAuthService: NyasaBlogAuthService,
    val sessionManager: SessionManager
) {
}