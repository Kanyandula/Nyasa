package com.kanyandula.nyasa.domain.usecase.blog

import com.kanyandula.nyasa.domain.repository.BlogRepository
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookmarksUseCase
@Inject
constructor(private val blogRepository: BlogRepository) {
    operator fun invoke(): Flow<Resource<List<BlogPost>>> =
        blogRepository.getBookmarks()
}
