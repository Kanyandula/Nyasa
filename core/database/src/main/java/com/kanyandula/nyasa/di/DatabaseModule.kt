package com.kanyandula.nyasa.di

import android.app.Application
import androidx.room.Room
import com.kanyandula.nyasa.persistance.AccountPropertiesDao
import com.kanyandula.nyasa.persistance.AppDatabase
import com.kanyandula.nyasa.persistance.AppDatabase.Companion.DATABASE_NAME
import com.kanyandula.nyasa.persistance.AuthTokenDao
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.persistance.BlogRemoteKeyDao
import com.kanyandula.nyasa.persistance.CommentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDb(app: Application): AppDatabase {
        return Room
            .databaseBuilder(app, AppDatabase::class.java, DATABASE_NAME)
            .addMigrations(
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideAuthTokenDao(db: AppDatabase): AuthTokenDao {
        return db.getAuthTokenDao()
    }

    @Singleton
    @Provides
    fun provideAccountPropertiesDao(db: AppDatabase): AccountPropertiesDao {
        return db.getAccountPropertiesDao()
    }

    @Singleton
    @Provides
    fun provideBlogPostDao(db: AppDatabase): BlogPostDao {
        return db.getBlogPostDao()
    }

    @Singleton
    @Provides
    fun provideBlogRemoteKeyDao(db: AppDatabase): BlogRemoteKeyDao {
        return db.getBlogRemoteKeyDao()
    }

    @Singleton
    @Provides
    fun provideCommentDao(db: AppDatabase): CommentDao {
        return db.getCommentDao()
    }
}
