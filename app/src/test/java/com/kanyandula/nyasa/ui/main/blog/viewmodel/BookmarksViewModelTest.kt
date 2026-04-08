package com.kanyandula.nyasa.ui.main.blog.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.domain.usecase.blog.BookmarkBlogPostUseCase
import com.kanyandula.nyasa.domain.usecase.blog.GetBookmarksUseCase
import com.kanyandula.nyasa.fakes.FakeBlogRepository
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.util.MainDispatcherRule
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookmarksViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeBlogRepository
    private lateinit var viewModel: BookmarksViewModel

    private val testPost1 = BlogPost(
        pk = 1,
        title = "Post 1",
        slug = "post-1",
        body = "Body 1",
        image = "",
        date_updated = 0L,
        username = "user1"
    )
    private val testPost2 = BlogPost(
        pk = 2,
        title = "Post 2",
        slug = "post-2",
        body = "Body 2",
        image = "",
        date_updated = 0L,
        username = "user2"
    )

    @Before
    fun setup() {
        fakeRepository = FakeBlogRepository()
        fakeRepository.bookmarksResult = Resource.Success(listOf(testPost1, testPost2))
        viewModel = BookmarksViewModel(
            getBookmarksUseCase = GetBookmarksUseCase(fakeRepository),
            bookmarkBlogPostUseCase = BookmarkBlogPostUseCase(fakeRepository)
        )
    }

    @Test
    fun `init loads bookmarks into state`() = runTest {
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.bookmarks).hasSize(2)
        assertThat(viewModel.viewState.value.bookmarks).containsExactly(testPost1, testPost2)
    }

    @Test
    fun `loadBookmarks success updates bookmarks list`() = runTest {
        advanceUntilIdle()

        val newPost = testPost1.copy(pk = 3, title = "Post 3", slug = "post-3")
        fakeRepository.bookmarksResult = Resource.Success(listOf(newPost))

        viewModel.loadBookmarks()
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.bookmarks).containsExactly(newPost)
    }

    @Test
    fun `loadBookmarks error emits error event`() = runTest {
        advanceUntilIdle()

        fakeRepository.bookmarksResult = Resource.Error("Network error")

        viewModel.events.test {
            viewModel.loadBookmarks()
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.ShowErrorDialog::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadBookmarks sets loading false after completion`() = runTest {
        advanceUntilIdle()

        viewModel.loadBookmarks()
        advanceUntilIdle()

        assertThat(viewModel.isLoading.value).isFalse()
    }

    @Test
    fun `removeBookmark success removes post from list`() = runTest {
        advanceUntilIdle()
        assertThat(viewModel.viewState.value.bookmarks).hasSize(2)

        fakeRepository.bookmarkResult = Resource.Success(false)

        viewModel.removeBookmark("post-1")
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.bookmarks).hasSize(1)
        assertThat(viewModel.viewState.value.bookmarks.first().slug).isEqualTo("post-2")
    }

    @Test
    fun `removeBookmark error emits error event`() = runTest {
        advanceUntilIdle()

        fakeRepository.bookmarkResult = Resource.Error("Failed to remove bookmark")

        viewModel.events.test {
            viewModel.removeBookmark("post-1")
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.ShowErrorDialog::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `removeBookmark error does not modify list`() = runTest {
        advanceUntilIdle()

        fakeRepository.bookmarkResult = Resource.Error("Failed")

        viewModel.removeBookmark("post-1")
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.bookmarks).hasSize(2)
    }
}
