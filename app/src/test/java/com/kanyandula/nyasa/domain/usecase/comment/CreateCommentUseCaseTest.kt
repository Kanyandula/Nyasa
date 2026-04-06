package com.kanyandula.nyasa.domain.usecase.comment

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.fakes.FakeCommentRepository
import com.kanyandula.nyasa.models.Comment
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CreateCommentUseCaseTest {

    private lateinit var fakeRepository: FakeCommentRepository
    private lateinit var useCase: CreateCommentUseCase

    @Before
    fun setup() {
        fakeRepository = FakeCommentRepository()
        useCase = CreateCommentUseCase(fakeRepository)
    }

    @Test
    fun `invoke emits loading then success with created comment`() = runTest {
        val comment = Comment(pk = 1, body = "My comment", username = "testuser", dateCreated = 1000L)
        fakeRepository.createCommentResult = Resource.Success(comment)

        useCase("test-slug", "My comment").test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val success = awaitItem() as Resource.Success
            assertThat(success.data.body).isEqualTo("My comment")
            assertThat(success.data.username).isEqualTo("testuser")
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits loading then error on failure`() = runTest {
        fakeRepository.createCommentResult = Resource.Error("Unauthorized")

        useCase("test-slug", "My comment").test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val error = awaitItem() as Resource.Error
            assertThat(error.message).isEqualTo("Unauthorized")
            awaitComplete()
        }
    }
}
