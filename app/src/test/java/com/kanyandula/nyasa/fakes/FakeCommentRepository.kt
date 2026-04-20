package com.kanyandula.nyasa.fakes

import com.kanyandula.nyasa.domain.repository.CommentRepository
import com.kanyandula.nyasa.models.Comment
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeCommentRepository : CommentRepository {

    var commentsResult: Resource<List<Comment>> = Resource.Success(emptyList())

    override fun getComments(slug: String): Flow<Resource<List<Comment>>> =
        fakeResourceFlow { commentsResult }

    override fun getCommentsFlow(slug: String): Flow<List<Comment>> =
        flowOf(emptyList())

    var createCommentResult: Resource<Comment> = Resource.Success(
        Comment(pk = 1, body = "Test comment", username = "testuser", dateCreated = 0L)
    )

    override fun createComment(slug: String, body: String): Flow<Resource<Comment>> =
        fakeResourceFlow { createCommentResult }

    var deleteCommentResult: Resource<String> = Resource.Success("Deleted")

    override fun deleteComment(pk: Int, slug: String): Flow<Resource<String>> =
        fakeResourceFlow { deleteCommentResult }
}
