package com.kanyandula.nyasa.domain.usecase.comment

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.fakes.FakeCommentRepository
import com.kanyandula.nyasa.models.Comment
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetCommentsUseCaseTest {

    private lateinit var fakeRepository: FakeCommentRepository
    private lateinit var useCase: GetCommentsUseCase

    @Before
    fun setup() {
        fakeRepository = FakeCommentRepository()
        useCase = GetCommentsUseCase(fakeRepository)
    }

    @Test
    fun `invoke emits loading then success with comments`() = runTest {
        val comments = listOf(
            Comment(pk = 1, body = "Great post", username = "user1", dateCreated = 1000L),
            Comment(pk = 2, body = "Nice", username = "user2", dateCreated = 2000L)
        )
        fakeRepository.commentsResult = Resource.Success(comments)

        useCase("test-slug").test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val success = awaitItem() as Resource.Success
            assertThat(success.data).hasSize(2)
            assertThat(success.data[0].body).isEqualTo("Great post")
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits loading then error on failure`() = runTest {
        fakeRepository.commentsResult = Resource.Error("Not found")

        useCase("test-slug").test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val error = awaitItem() as Resource.Error
            assertThat(error.message).isEqualTo("Not found")
            awaitComplete()
        }
    }
}
