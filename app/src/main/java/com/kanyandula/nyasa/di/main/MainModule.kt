package com.kanyandula.nyasa.di.main

import android.content.SharedPreferences
import com.kanyandula.nyasa.api.auth.NyasaBlogApiAuthService
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.persistance.AccountPropertiesDao
import com.kanyandula.nyasa.persistance.AuthTokenDao
import com.kanyandula.nyasa.repository.auth.AuthRepository
import com.kanyandula.nyasa.repository.main.AccountRepository
import com.kanyandula.nyasa.session.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MainModule {

    @Singleton
    @Provides
    fun provideOpenApiMainService(retrofitBuilder: Retrofit.Builder): NyasaBlogApiMainService {
        return retrofitBuilder
            .build()
            .create(NyasaBlogApiMainService::class.java)
    }




    @Singleton
    @Provides
    fun provideMainRepository(
        openApiMainService: NyasaBlogApiMainService,
        accountPropertiesDao: AccountPropertiesDao,
        sessionManager: SessionManager
    ): AccountRepository {
        return AccountRepository(openApiMainService, accountPropertiesDao, sessionManager)
    }

}