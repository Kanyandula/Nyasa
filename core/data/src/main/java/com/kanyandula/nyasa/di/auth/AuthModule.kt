package com.kanyandula.nyasa.di.auth

import com.kanyandula.nyasa.api.auth.NyasaBlogApiAuthService
import com.kanyandula.nyasa.domain.repository.AuthRepository
import com.kanyandula.nyasa.repository.auth.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    companion object {

        @Singleton
        @Provides
        fun provideNyasaBlogAuthService(retrofitBuilder: Retrofit.Builder): NyasaBlogApiAuthService {
            return retrofitBuilder
                .build()
                .create(NyasaBlogApiAuthService::class.java)
        }
    }
}
