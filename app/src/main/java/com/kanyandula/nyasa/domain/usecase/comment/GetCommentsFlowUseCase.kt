package com.kanyandula.nyasa.domain.usecase.comment

import com.kanyandula.nyasa.domain.repository.CommentRepository
import com.kanyandula.nyasa.models.Comment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCommentsFlowUseCase
@Inject
constructor(private val commentRepository: CommentRepository) {
    operator fun invoke(slug: String): Flow<List<Comment>> =
        commentRepository.getCommentsFlow(slug)
}
