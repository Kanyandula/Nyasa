package com.kanyandula.nyasa.di.auth


import com.kanyandula.nyasa.api.auth.NyasaBlogAuthService
import com.kanyandula.nyasa.persistance.AccountPropertiesDao
import com.kanyandula.nyasa.persistance.AuthTokenDao
import com.kanyandula.nyasa.repository.auth.AuthRepository
import com.kanyandula.nyasa.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class AuthModule{

    // TEMPORARY
    @AuthScope
    @Provides
    fun provideFakeApiService(): NyasaBlogAuthService{
        return Retrofit.Builder()
            .baseUrl("https://open-api.xyz")
            .build()
            .create(NyasaBlogAuthService::class.java)
    }

    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        openApiAuthService: NyasaBlogAuthService
        ): AuthRepository {
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            openApiAuthService,
            sessionManager
        )
    }

}