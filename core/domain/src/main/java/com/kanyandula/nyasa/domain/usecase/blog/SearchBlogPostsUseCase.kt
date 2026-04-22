package com.kanyandula.nyasa.domain.usecase.blog

import androidx.paging.PagingData
import com.kanyandula.nyasa.domain.repository.BlogRepository
import com.kanyandula.nyasa.models.BlogPost
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchBlogPostsUseCase
@Inject
constructor(private val blogRepository: BlogRepository) {
    operator fun invoke(
        query: String,
        filterAndOrder: String,
        category: String? = null
    ): Flow<PagingData<BlogPost>> =
        blogRepository.getBlogPagingData(query, filterAndOrder, category)
}
