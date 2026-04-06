package com.kanyandula.nyasa.domain.usecase.blog

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.fakes.FakeBlogRepository
import com.kanyandula.nyasa.models.LikeResult
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LikeBlogPostUseCaseTest {

    private lateinit var fakeRepository: FakeBlogRepository
    private lateinit var useCase: LikeBlogPostUseCase

    @Before
    fun setup() {
        fakeRepository = FakeBlogRepository()
        useCase = LikeBlogPostUseCase(fakeRepository)
    }

    @Test
    fun `invoke emits loading then success with like result`() = runTest {
        fakeRepository.likeResult = Resource.Success(LikeResult(liked = true, likeCount = 5))

        useCase("test-slug").test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val success = awaitItem() as Resource.Success
            assertThat(success.data.liked).isTrue()
            assertThat(success.data.likeCount).isEqualTo(5)
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits loading then error on failure`() = runTest {
        fakeRepository.likeResult = Resource.Error("Network error")

        useCase("test-slug").test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val error = awaitItem() as Resource.Error
            assertThat(error.message).isEqualTo("Network error")
            awaitComplete()
        }
    }
}
