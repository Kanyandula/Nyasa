package com.kanyandula.nyasa.di

import android.app.Application
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.kanyandula.nyasa.persistance.AccountPropertiesDao
import com.kanyandula.nyasa.persistance.AppDatabase
import com.kanyandula.nyasa.persistance.AppDatabase.Companion.DATABASE_NAME
import com.kanyandula.nyasa.persistance.AuthTokenDao
import com.kanyandula.nyasa.persistance.CommentDao
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.ui.theme.themeDataStore
import com.kanyandula.nyasa.util.PreferenceKeys
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideSharedPreferences(application: Application): SharedPreferences {
        val masterKey = MasterKey.Builder(application)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            application,
            PreferenceKeys.APP_PREFERENCES,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Singleton
    @Provides
    fun provideSharedPrefsEditor(sharedPreferences: SharedPreferences): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

    @Singleton
    @Provides
    fun provideImageLoader(
        application: Application,
        okHttpClient: OkHttpClient
    ): ImageLoader {
        return ImageLoader.Builder(application)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { okHttpClient }))
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(application, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(
                        application.cacheDir.resolve("img").toOkioPath()
                    )
                    .maxSizeBytes(100L * 1024 * 1024)
                    .build()
            }
            .build()
    }

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
            .fallbackToDestructiveMigration() // fallback if migration path not found
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
    fun provideCommentDao(db: AppDatabase): CommentDao = db.getCommentDao()

    @Singleton
    @Provides
    fun provideConnectivityObserver(application: Application): ConnectivityObserver =
        ConnectivityObserver(application)

    @Singleton
    @Provides
    fun provideThemeDataStore(application: Application): DataStore<Preferences> =
        application.themeDataStore
}
