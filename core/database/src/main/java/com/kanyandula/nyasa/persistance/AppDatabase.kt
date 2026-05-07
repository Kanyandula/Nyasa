package com.kanyandula.nyasa.persistance

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.models.BlogRemoteKey
import com.kanyandula.nyasa.models.CommentEntity

@Database(
    entities = [
        AuthToken::class,
        AccountProperties::class,
        BlogPost::class,
        BlogRemoteKey::class,
        CommentEntity::class
    ],
    version = 8
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getAuthTokenDao(): AuthTokenDao

    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    abstract fun getBlogPostDao(): BlogPostDao

    abstract fun getBlogRemoteKeyDao(): BlogRemoteKeyDao

    abstract fun getCommentDao(): CommentDao

    companion object {
        const val DATABASE_NAME: String = "app_db"

        val MIGRATION_3_4 = Migration(3, 4) {
            it.addBlogPostColumns()
            it.addAccountPropertiesColumns()
        }

        val MIGRATION_4_5 = Migration(4, 5) {
            it.execSQL(
                "ALTER TABLE blog_post ADD COLUMN comment_count INTEGER DEFAULT NULL"
            )
        }

        val MIGRATION_5_6 = Migration(5, 6) {
            it.execSQL(
                "ALTER TABLE blog_post ADD COLUMN author_avatar TEXT DEFAULT NULL"
            )
        }

        val MIGRATION_6_7 = Migration(6, 7) {
            it.execSQL(
                """
                CREATE TABLE IF NOT EXISTS comments (
                    pk INTEGER NOT NULL PRIMARY KEY,
                    post_slug TEXT NOT NULL,
                    body TEXT NOT NULL,
                    username TEXT NOT NULL,
                    date_created INTEGER NOT NULL,
                    FOREIGN KEY (post_slug) REFERENCES blog_post(slug) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            it.execSQL(
                "CREATE INDEX IF NOT EXISTS index_comments_post_slug ON comments(post_slug)"
            )
        }

        val MIGRATION_7_8 = Migration(7, 8) {
            it.execSQL(
                "ALTER TABLE blog_post ADD COLUMN is_featured INTEGER NOT NULL DEFAULT 0"
            )
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
