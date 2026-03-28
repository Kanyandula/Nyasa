package com.kanyandula.nyasa.domain.usecase.blog

import com.kanyandula.nyasa.domain.repository.BlogRepository
import com.kanyandula.nyasa.models.BlogPost
import javax.inject.Inject

class GetBlogPostBySlugUseCase
@Inject
constructor(private val blogRepository: BlogRepository) {
    suspend operator fun invoke(slug: String): BlogPost? =
        blogRepository.getBlogPostBySlug(slug)
}
