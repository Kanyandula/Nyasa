package com.kanyandula.nyasa.domain.usecase.comment

import com.kanyandula.nyasa.domain.repository.CommentRepository
import com.kanyandula.nyasa.models.Comment
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCommentsUseCase
@Inject
constructor(private val commentRepository: CommentRepository) {
    operator fun invoke(slug: String): Flow<Resource<List<Comment>>> =
        commentRepository.getComments(slug)
}
