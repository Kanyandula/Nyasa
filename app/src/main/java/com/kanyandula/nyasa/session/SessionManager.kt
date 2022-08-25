package com.kanyandula.nyasa.session

import android.app.Application
import com.kanyandula.nyasa.persistance.AuthTokenDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val application: Application

) {
}