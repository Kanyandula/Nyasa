package com.kanyandula.nyasa.domain.repository

import android.net.Uri
import androidx.paging.PagingData
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.models.LikeResult
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow

interface BlogRepository {
    fun getBlogPagingData(
        query: String,
        filterAndOrder: String
    ): Flow<PagingData<BlogPost>>
    fun isAuthorOfBlogPost(slug: String): Flow<Resource<Boolean>>
    fun deleteBlogPost(blogPost: BlogPost): Flow<Resource<String>>
    fun updateBlogPost(
        slug: String,
        title: String,
        body: String,
        image: Uri?,
        category: String? = null,
        tags: List<String>? = null
    ): Flow<Resource<BlogPost>>
    suspend fun getBlogPostBySlug(slug: String): BlogPost?
    fun likeBlogPost(slug: String): Flow<Resource<LikeResult>>
    fun bookmarkBlogPost(slug: String): Flow<Resource<Boolean>>
    fun getBookmarks(): Flow<Resource<List<BlogPost>>>
}
