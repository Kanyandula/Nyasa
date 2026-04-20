package com.kanyandula.nyasa.domain.usecase.blog

import com.kanyandula.nyasa.domain.repository.BlogRepository
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BookmarkBlogPostUseCase
@Inject
constructor(private val blogRepository: BlogRepository) {
    operator fun invoke(slug: String): Flow<Resource<Boolean>> =
        blogRepository.bookmarkBlogPost(slug)
}
