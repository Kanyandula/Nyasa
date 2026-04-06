package com.kanyandula.nyasa.domain.usecase.blog

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.fakes.FakeBlogRepository
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class BookmarkBlogPostUseCaseTest {

    private lateinit var fakeRepository: FakeBlogRepository
    private lateinit var useCase: BookmarkBlogPostUseCase

    @Before
    fun setup() {
        fakeRepository = FakeBlogRepository()
        useCase = BookmarkBlogPostUseCase(fakeRepository)
    }

    @Test
    fun `invoke emits loading then success with bookmarked true`() = runTest {
        fakeRepository.bookmarkResult = Resource.Success(true)

        useCase("test-slug").test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val success = awaitItem() as Resource.Success
            assertThat(success.data).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits loading then success with bookmarked false`() = runTest {
        fakeRepository.bookmarkResult = Resource.Success(false)

        useCase("test-slug").test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val success = awaitItem() as Resource.Success
            assertThat(success.data).isFalse()
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits loading then error on failure`() = runTest {
        fakeRepository.bookmarkResult = Resource.Error("Network error")

        useCase("test-slug").test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val error = awaitItem() as Resource.Error
            assertThat(error.message).isEqualTo("Network error")
            awaitComplete()
        }
    }
}
