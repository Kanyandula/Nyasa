package com.kanyandula.nyasa.persistance

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.kanyandula.nyasa.models.BlogPost

@Dao
interface BlogPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blogPost: BlogPost): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(blogPosts: List<BlogPost>)

    @Delete
    suspend fun deleteBlogPost(blogPost: BlogPost)

    @Query("DELETE FROM blog_post")
    suspend fun clearAll()

    @Query(
        """
        UPDATE blog_post SET title = :title, body = :body, image = :image
        WHERE pk = :pk
        """
    )
    suspend fun updateBlogPost(pk: Int, title: String, body: String, image: String)

    @RawQuery(observedEntities = [BlogPost::class])
    fun getBlogPostsPagingSource(query: SupportSQLiteQuery): PagingSource<Int, BlogPost>

    @Query("SELECT * FROM blog_post WHERE slug = :slug LIMIT 1")
    suspend fun getBlogPostBySlug(slug: String): BlogPost?
}
