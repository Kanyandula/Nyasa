package com.kanyandula.nyasa.domain.usecase.blog

import com.kanyandula.nyasa.domain.repository.BlogRepository
import com.kanyandula.nyasa.models.LikeResult
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LikeBlogPostUseCase
@Inject
constructor(private val blogRepository: BlogRepository) {
    operator fun invoke(slug: String): Flow<Resource<LikeResult>> =
        blogRepository.likeBlogPost(slug)
}
