package com.kanyandula.nyasa.domain.usecase.comment

import com.kanyandula.nyasa.domain.repository.CommentRepository
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeleteCommentUseCase
@Inject
constructor(private val commentRepository: CommentRepository) {
    operator fun invoke(pk: Int, slug: String): Flow<Resource<String>> =
        commentRepository.deleteComment(pk, slug)
}