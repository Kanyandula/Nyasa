package com.kanyandula.nyasa.di.main


import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.persistance.AccountPropertiesDao
import com.kanyandula.nyasa.persistance.AppDatabase
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.repository.main.AccountRepository
import com.kanyandula.nyasa.repository.main.BlogRepository
import com.kanyandula.nyasa.repository.main.CreateBlogRepository
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
    fun provideAccountMainRepository(
        blogApiMainService: NyasaBlogApiMainService,
        accountPropertiesDao: AccountPropertiesDao,
        sessionManager: SessionManager
    ): AccountRepository {
        return AccountRepository(blogApiMainService, accountPropertiesDao, sessionManager)
    }

    @Singleton
    @Provides
    fun provideBlogPostDao(db: AppDatabase): BlogPostDao {
        return db.getBlogPostDao()
    }

    @Singleton
    @Provides
    fun provideBlogRepository(
        nyasaBlogApiMainService: NyasaBlogApiMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): BlogRepository {
        return BlogRepository(nyasaBlogApiMainService, blogPostDao, sessionManager)
    }

    @Singleton
    @Provides
    fun provideCreateBlogRepository(
        nyasaBlogApiMainService: NyasaBlogApiMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): CreateBlogRepository {
        return CreateBlogRepository( nyasaBlogApiMainService, blogPostDao, sessionManager)
    }


}