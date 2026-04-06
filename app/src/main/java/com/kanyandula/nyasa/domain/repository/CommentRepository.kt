package com.kanyandula.nyasa.domain.repository

import com.kanyandula.nyasa.models.Comment
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun getComments(slug: String): Flow<Resource<List<Comment>>>
    fun createComment(slug: String, body: String): Flow<Resource<Comment>>
    fun deleteComment(pk: Int): Flow<Resource<String>>
}
