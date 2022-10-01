package com.kanyandula.nyasa.di.auth


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
object AuthModule{



    @Singleton
    @Provides
    fun provideNyasaBlogAuthService(retrofitBuilder: Retrofit.Builder): NyasaBlogApiAuthService {
        return retrofitBuilder
            .build()
            .create(NyasaBlogApiAuthService::class.java)
    }

    @Singleton
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        openApiApiAuthService: NyasaBlogApiAuthService,
        preferences: SharedPreferences,
        editor: SharedPreferences.Editor
    ): AuthRepository {
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            openApiApiAuthService,
            sessionManager,
            preferences,
            editor
        )
    }

}