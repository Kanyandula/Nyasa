package com.kanyandula.nyasa.di

import com.kanyandula.nyasa.api.TokenProvider
import com.kanyandula.nyasa.session.SessionManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TokenProviderModule {

    @Singleton
    @Binds
    abstract fun bindTokenProvider(sessionManager: SessionManager): TokenProvider
}
