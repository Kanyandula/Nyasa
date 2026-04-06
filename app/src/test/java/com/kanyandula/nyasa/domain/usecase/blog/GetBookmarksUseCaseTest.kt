package com.kanyandula.nyasa.domain.usecase.blog

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.fakes.FakeBlogRepository
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetBookmarksUseCaseTest {

    private lateinit var fakeRepository: FakeBlogRepository
    private lateinit var useCase: GetBookmarksUseCase

    @Before
    fun setup() {
        fakeRepository = FakeBlogRepository()
        useCase = GetBookmarksUseCase(fakeRepository)
    }

    @Test
    fun `invoke emits loading then success with bookmarks list`() = runTest {
        val bookmarks = listOf(
            BlogPost(1, "Title", "slug", "body", "img", 0L, "user")
        )
        fakeRepository.bookmarksResult = Resource.Success(bookmarks)

        useCase().test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val success = awaitItem() as Resource.Success
            assertThat(success.data).hasSize(1)
            assertThat(success.data[0].title).isEqualTo("Title")
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits loading then success with empty list`() = runTest {
        fakeRepository.bookmarksResult = Resource.Success(emptyList())

        useCase().test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val success = awaitItem() as Resource.Success
            assertThat(success.data).isEmpty()
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits loading then error on failure`() = runTest {
        fakeRepository.bookmarksResult = Resource.Error("Network error")

        useCase().test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val error = awaitItem() as Resource.Error
            assertThat(error.message).isEqualTo("Network error")
            awaitComplete()
        }
    }
}
