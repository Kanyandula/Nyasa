package com.kanyandula.nyasa.domain.usecase.comment

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.fakes.FakeCommentRepository
import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DeleteCommentUseCaseTest {

    private lateinit var fakeRepository: FakeCommentRepository
    private lateinit var useCase: DeleteCommentUseCase

    @Before
    fun setup() {
        fakeRepository = FakeCommentRepository()
        useCase = DeleteCommentUseCase(fakeRepository)
    }

    @Test
    fun `invoke emits loading then success on delete`() = runTest {
        fakeRepository.deleteCommentResult = Resource.Success("Deleted")

        useCase(1).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val success = awaitItem() as Resource.Success
            assertThat(success.data).isEqualTo("Deleted")
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits loading then error on failure`() = runTest {
        fakeRepository.deleteCommentResult = Resource.Error(AppError.Unknown(RuntimeException("Forbidden")))

        useCase(1).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val error = awaitItem() as Resource.Error
            awaitComplete()
        }
    }
}
