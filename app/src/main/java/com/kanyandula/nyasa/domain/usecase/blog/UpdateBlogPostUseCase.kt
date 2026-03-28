package com.kanyandula.nyasa.domain.usecase.blog

import android.net.Uri
import com.kanyandula.nyasa.domain.repository.BlogRepository
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateBlogPostUseCase
@Inject
constructor(private val blogRepository: BlogRepository) {
    operator fun invoke(
        slug: String,
        title: String,
        body: String,
        image: Uri?
    ): Flow<Resource<BlogPost>> =
        blogRepository.updateBlogPost(slug, title, body, image)
}
