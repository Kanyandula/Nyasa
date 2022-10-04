package com.kanyandula.nyasa.persistance


import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.util.Constants.Companion.PAGINATION_PAGE_SIZE

@Dao
interface BlogPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(blogPost: BlogPost): Long

    @Query("""
        SELECT * FROM blog_post 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        OR username LIKE '%' || :query || '%' 
        LIMIT (:page * :pageSize)
        """)
    fun getAllBlogPosts(
        query: String,
        page: Int,
        pageSize: Int = PAGINATION_PAGE_SIZE
    ): LiveData<List<BlogPost>>
}



