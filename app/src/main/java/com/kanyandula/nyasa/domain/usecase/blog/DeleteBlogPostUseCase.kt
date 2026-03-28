package com.kanyandula.nyasa.domain.usecase.blog

import com.kanyandula.nyasa.domain.repository.BlogRepository
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteBlogPostUseCase
@Inject
constructor(private val blogRepository: BlogRepository) {
    operator fun invoke(blogPost: BlogPost): Flow<Resource<String>> =
        blogRepository.deleteBlogPost(blogPost)
}
