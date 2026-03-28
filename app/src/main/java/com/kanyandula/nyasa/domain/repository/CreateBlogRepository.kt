package com.kanyandula.nyasa.domain.repository

import android.net.Uri
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow

interface CreateBlogRepository {
    fun createNewBlogPost(
        title: String,
        body: String,
        image: Uri?
    ): Flow<Resource<String>>
}
