package com.kanyandula.nyasa.persistance

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.models.BlogRemoteKey

@Database(
    entities = [AuthToken::class, AccountProperties::class, BlogPost::class, BlogRemoteKey::class],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getAuthTokenDao(): AuthTokenDao

    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    abstract fun getBlogPostDao(): BlogPostDao

    abstract fun getBlogRemoteKeyDao(): BlogRemoteKeyDao

    companion object {
        const val DATABASE_NAME: String = "app_db"

        val MIGRATION_3_4 = Migration(3, 4) {
            it.addBlogPostColumns()
            it.addAccountPropertiesColumns()
        }

        private fun SupportSQLiteDatabase.addBlogPostColumns() {
            execSQL("ALTER TABLE blog_post ADD COLUMN category TEXT DEFAULT NULL")
            execSQL("ALTER TABLE blog_post ADD COLUMN tags TEXT DEFAULT NULL")
            execSQL("ALTER TABLE blog_post ADD COLUMN reading_time INTEGER DEFAULT NULL")
            execSQL("ALTER TABLE blog_post ADD COLUMN view_count INTEGER DEFAULT NULL")
            execSQL("ALTER TABLE blog_post ADD COLUMN like_count INTEGER DEFAULT NULL")
        }

        private fun SupportSQLiteDatabase.addAccountPropertiesColumns() {
            execSQL("ALTER TABLE account_properties ADD COLUMN bio TEXT DEFAULT NULL")
            execSQL("ALTER TABLE account_properties ADD COLUMN location TEXT DEFAULT NULL")
            execSQL("ALTER TABLE account_properties ADD COLUMN website TEXT DEFAULT NULL")
            execSQL("ALTER TABLE account_properties ADD COLUMN twitter TEXT DEFAULT NULL")
            execSQL("ALTER TABLE account_properties ADD COLUMN facebook TEXT DEFAULT NULL")
            execSQL("ALTER TABLE account_properties ADD COLUMN instagram TEXT DEFAULT NULL")
            execSQL("ALTER TABLE account_properties ADD COLUMN linkedin TEXT DEFAULT NULL")
            execSQL("ALTER TABLE account_properties ADD COLUMN profile_image TEXT DEFAULT NULL")
        }
    }
}
