package com.kanyandula.nyasa.persistance

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.models.BlogRemoteKey

@Database(
    entities = [AuthToken::class, AccountProperties::class, BlogPost::class, BlogRemoteKey::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getAuthTokenDao(): AuthTokenDao

    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    abstract fun getBlogPostDao(): BlogPostDao

    abstract fun getBlogRemoteKeyDao(): BlogRemoteKeyDao

    companion object {
        const val DATABASE_NAME: String = "app_db"
    }
}
