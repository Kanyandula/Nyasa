package com.kanyandula.nyasa.domain.usecase.blog

import com.kanyandula.nyasa.domain.model.BlogSearchResult
import com.kanyandula.nyasa.domain.repository.BlogRepository
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchBlogPostsUseCase
@Inject
constructor(private val blogRepository: BlogRepository) {
    operator fun invoke(
        query: String,
        filterAndOrder: String,
        page: Int
    ): Flow<Resource<BlogSearchResult>> =
        blogRepository.searchBlogPosts(query, filterAndOrder, page)
}
