package com.kanyandula.nyasa.di.main

import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.domain.repository.AccountRepository
import com.kanyandula.nyasa.domain.repository.BlogRepository
import com.kanyandula.nyasa.domain.repository.CreateBlogRepository
import com.kanyandula.nyasa.persistance.AppDatabase
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.repository.main.AccountRepositoryImpl
import com.kanyandula.nyasa.repository.main.BlogRepositoryImpl
import com.kanyandula.nyasa.repository.main.CreateBlogRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MainModule {

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindBlogRepository(impl: BlogRepositoryImpl): BlogRepository

    @Binds
    @Singleton
    abstract fun bindCreateBlogRepository(impl: CreateBlogRepositoryImpl): CreateBlogRepository

    companion object {

        @Singleton
        @Provides
        fun provideOpenApiMainService(retrofitBuilder: Retrofit.Builder): NyasaBlogApiMainService {
            return retrofitBuilder
                .build()
                .create(NyasaBlogApiMainService::class.java)
        }

        @Singleton
        @Provides
        fun provideBlogPostDao(db: AppDatabase): BlogPostDao {
            return db.getBlogPostDao()
        }
    }
}
