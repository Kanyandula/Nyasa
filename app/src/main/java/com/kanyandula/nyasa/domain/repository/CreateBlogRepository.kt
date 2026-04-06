package com.kanyandula.nyasa.domain.repository

import android.net.Uri
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow

interface CreateBlogRepository {
    fun createNewBlogPost(
        title: String,
        body: String,
        image: Uri?,
        category: String? = null,
        tags: List<String>? = null
    ): Flow<Resource<String>>
}
