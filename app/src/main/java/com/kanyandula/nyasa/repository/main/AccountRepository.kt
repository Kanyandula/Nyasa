package com.kanyandula.nyasa.repository.main

import android.util.Log
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.persistance.AccountPropertiesDao
import com.kanyandula.nyasa.session.SessionManager

import kotlinx.coroutines.Job
import javax.inject.Inject

class AccountRepository
@Inject
constructor(
    val nyasaBlogApiMainService: NyasaBlogApiMainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager
)
{

    private val TAG: String = "AppDebug"

    private var repositoryJob: Job? = null


    fun cancelActiveJobs(){
        Log.d(TAG, "AuthRepository: Cancelling on-going jobs...")
        repositoryJob?.cancel()
    }
}












