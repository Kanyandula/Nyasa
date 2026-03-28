package com.kanyandula.nyasa.domain.repository

import android.net.Uri
import com.kanyandula.nyasa.domain.model.BlogSearchResult
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow

interface BlogRepository {
    fun searchBlogPosts(
        query: String,
        filterAndOrder: String,
        page: Int
    ): Flow<Resource<BlogSearchResult>>
    fun isAuthorOfBlogPost(slug: String): Flow<Resource<Boolean>>
    fun deleteBlogPost(blogPost: BlogPost): Flow<Resource<String>>
    fun updateBlogPost(
        slug: String,
        title: String,
        body: String,
        image: Uri?
    ): Flow<Resource<BlogPost>>
}
