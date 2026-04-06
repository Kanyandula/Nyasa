package com.kanyandula.nyasa.domain.usecase.createblog

import android.net.Uri
import com.kanyandula.nyasa.domain.repository.CreateBlogRepository
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateBlogPostUseCase
@Inject
constructor(private val createBlogRepository: CreateBlogRepository) {
    operator fun invoke(
        title: String,
        body: String,
        image: Uri?,
        category: String? = null,
        tags: List<String>? = null
    ): Flow<Resource<String>> =
        createBlogRepository.createNewBlogPost(title, body, image, category, tags)
}
