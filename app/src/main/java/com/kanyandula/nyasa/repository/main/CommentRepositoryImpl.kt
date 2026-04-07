package com.kanyandula.nyasa.repository.main

import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.toComment
import com.kanyandula.nyasa.domain.repository.CommentRepository
import com.kanyandula.nyasa.models.Comment
import com.kanyandula.nyasa.repository.networkApiFlow
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CommentRepositoryImpl
@Inject
constructor(
    private val apiService: NyasaBlogApiMainService,
    private val connectivityObserver: ConnectivityObserver
) : CommentRepository {

    override fun getComments(
        slug: String
    ): Flow<Resource<List<Comment>>> = networkApiFlow(
        connectivityObserver = connectivityObserver,
        apiCall = { apiService.getComments(slug) },
        onSuccess = { body -> Resource.Success(body.map { it.toComment() }) }
    )

    override fun createComment(
        slug: String,
        body: String
    ): Flow<Resource<Comment>> = networkApiFlow(
        connectivityObserver = connectivityObserver,
        apiCall = { apiService.createComment(slug, body) },
        onSuccess = { responseBody -> Resource.Success(responseBody.toComment()) }
    )

    override fun deleteComment(
        pk: Int
    ): Flow<Resource<String>> = networkApiFlow(
        connectivityObserver = connectivityObserver,
        apiCall = { apiService.deleteComment(pk) },
        onSuccess = { body -> Resource.Success(body.response) }
    )
}
