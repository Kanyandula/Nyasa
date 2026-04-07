package com.kanyandula.nyasa.ui.main.blog.viewmodel

import android.content.SharedPreferences
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.domain.usecase.blog.BookmarkBlogPostUseCase
import com.kanyandula.nyasa.domain.usecase.blog.DeleteBlogPostUseCase
import com.kanyandula.nyasa.domain.usecase.blog.GetBlogPostBySlugUseCase
import com.kanyandula.nyasa.domain.usecase.blog.IsAuthorOfBlogPostUseCase
import com.kanyandula.nyasa.domain.usecase.blog.LikeBlogPostUseCase
import com.kanyandula.nyasa.domain.usecase.blog.SearchBlogPostsUseCase
import com.kanyandula.nyasa.domain.usecase.blog.UpdateBlogPostUseCase
import com.kanyandula.nyasa.domain.usecase.category.GetCategoriesUseCase
import com.kanyandula.nyasa.domain.usecase.comment.CreateCommentUseCase
import com.kanyandula.nyasa.domain.usecase.comment.DeleteCommentUseCase
import com.kanyandula.nyasa.domain.usecase.comment.GetCommentsUseCase
import com.kanyandula.nyasa.fakes.FakeBlogRepository
import com.kanyandula.nyasa.fakes.FakeCategoryRepository
import com.kanyandula.nyasa.fakes.FakeCommentRepository
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.persistance.BlogQueryUtils
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.ui.main.blog.state.BlogNavigationEvent
import com.kanyandula.nyasa.util.MainDispatcherRule
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.SuccessHandling.SUCCESS_BLOG_DELETED
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BlogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeBlogRepository
    private lateinit var fakeCommentRepository: FakeCommentRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var viewModel: BlogViewModel

    private fun createTestBlogPost(
        pk: Int = 1,
        title: String = "Test Blog",
        slug: String = "test-blog",
        body: String = "Test body",
        image: String = "https://example.com/image.jpg",
        dateUpdated: Long = 1000L,
        username: String = "testuser"
    ) = BlogPost(pk, title, slug, body, image, dateUpdated, username)

    @Before
    fun setup() {
        fakeRepository = FakeBlogRepository()
        fakeCommentRepository = FakeCommentRepository()
        fakeCategoryRepository = FakeCategoryRepository()
        sharedPreferences = mockk(relaxed = true)
        editor = mockk(relaxed = true)

        every { sharedPreferences.getString(any(), any()) } answers { secondArg() }

        viewModel = BlogViewModel(
            searchBlogPostsUseCase = SearchBlogPostsUseCase(fakeRepository),
            isAuthorOfBlogPostUseCase = IsAuthorOfBlogPostUseCase(fakeRepository),
            deleteBlogPostUseCase = DeleteBlogPostUseCase(fakeRepository),
            updateBlogPostUseCase = UpdateBlogPostUseCase(fakeRepository),
            getBlogPostBySlugUseCase = GetBlogPostBySlugUseCase(fakeRepository),
            likeBlogPostUseCase = LikeBlogPostUseCase(fakeRepository),
            bookmarkBlogPostUseCase = BookmarkBlogPostUseCase(fakeRepository),
            getCommentsUseCase = GetCommentsUseCase(fakeCommentRepository),
            createCommentUseCase = CreateCommentUseCase(fakeCommentRepository),
            deleteCommentUseCase = DeleteCommentUseCase(fakeCommentRepository),
            getCategoriesUseCase = GetCategoriesUseCase(fakeCategoryRepository),
            sharedPreferences = sharedPreferences,
            editor = editor,
            savedStateHandle = androidx.lifecycle.SavedStateHandle()
        )
    }

    // region Blog List

    @Test
    fun `setQuery updates search query in state`() {
        viewModel.setQuery("kotlin")
        assertThat(viewModel.viewState.value.searchQuery).isEqualTo("kotlin")
    }

    @Test
    fun `setBlogFilter updates filter in state`() {
        viewModel.setBlogFilter(BlogQueryUtils.BLOG_FILTER_USERNAME)
        assertThat(viewModel.viewState.value.filter).isEqualTo(BlogQueryUtils.BLOG_FILTER_USERNAME)
    }

    @Test
    fun `setBlogFilter with null does not change state`() {
        val original = viewModel.viewState.value.filter
        viewModel.setBlogFilter(null)
        assertThat(viewModel.viewState.value.filter).isEqualTo(original)
    }

    @Test
    fun `setBlogOrder updates order in state`() {
        viewModel.setBlogOrder(BlogQueryUtils.BLOG_ORDER_DESC)
        assertThat(viewModel.viewState.value.order).isEqualTo(BlogQueryUtils.BLOG_ORDER_DESC)
    }

    @Test
    fun `saveFilterOptions persists to shared preferences`() {
        viewModel.saveFilterOptions(BlogQueryUtils.BLOG_FILTER_USERNAME, BlogQueryUtils.BLOG_ORDER_DESC)

        verify { editor.putString(any(), BlogQueryUtils.BLOG_FILTER_USERNAME) }
        verify { editor.putString(any(), BlogQueryUtils.BLOG_ORDER_DESC) }
        verify { editor.apply() }
    }

    @Test
    fun `getFilter returns current filter`() {
        viewModel.setBlogFilter(BlogQueryUtils.BLOG_FILTER_USERNAME)
        assertThat(viewModel.getFilter()).isEqualTo(BlogQueryUtils.BLOG_FILTER_USERNAME)
    }

    @Test
    fun `getOrder returns current order`() {
        viewModel.setBlogOrder(BlogQueryUtils.BLOG_ORDER_DESC)
        assertThat(viewModel.getOrder()).isEqualTo(BlogQueryUtils.BLOG_ORDER_DESC)
    }

    // endregion

    // region View Blog

    @Test
    fun `loadBlogBySlug sets blog post when found`() = runTest {
        val blogPost = createTestBlogPost()
        fakeRepository.blogPostBySlug = blogPost

        viewModel.loadBlogBySlug("test-blog")
        advanceUntilIdle()

        assertThat(viewModel.viewBlogState.value.blogPost).isEqualTo(blogPost)
    }

    @Test
    fun `loadBlogBySlug emits error when not found`() = runTest {
        fakeRepository.blogPostBySlug = null

        viewModel.events.test {
            viewModel.loadBlogBySlug("nonexistent")
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.ShowErrorDialog::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadBlogBySlug skips if same slug already loaded`() = runTest {
        val blogPost = createTestBlogPost()
        fakeRepository.blogPostBySlug = blogPost

        viewModel.loadBlogBySlug("test-blog")
        advanceUntilIdle()

        fakeRepository.blogPostBySlug = createTestBlogPost(title = "Different")
        viewModel.loadBlogBySlug("test-blog")
        advanceUntilIdle()

        // Second call with same slug should be skipped, keeping original data
        assertThat(viewModel.viewBlogState.value.blogPost?.title).isEqualTo("Test Blog")
    }

    @Test
    fun `checkIsAuthorOfBlogPost updates isAuthor state`() = runTest {
        fakeRepository.isAuthorResult = Resource.Success(true)

        viewModel.checkIsAuthorOfBlogPost("test-blog")
        advanceUntilIdle()

        assertThat(viewModel.viewBlogState.value.isAuthorOfBlogPost).isTrue()
    }

    @Test
    fun `checkIsAuthorOfBlogPost sets false when not author`() = runTest {
        fakeRepository.isAuthorResult = Resource.Success(false)

        viewModel.checkIsAuthorOfBlogPost("test-blog")
        advanceUntilIdle()

        assertThat(viewModel.viewBlogState.value.isAuthorOfBlogPost).isFalse()
    }

    @Test
    fun `isAuthorOfBlogPost returns current value`() = runTest {
        fakeRepository.isAuthorResult = Resource.Success(true)

        viewModel.checkIsAuthorOfBlogPost("test-blog")
        advanceUntilIdle()

        assertThat(viewModel.isAuthorOfBlogPost()).isTrue()
    }

    @Test
    fun `getBlogPost returns current blog post`() = runTest {
        val blogPost = createTestBlogPost()
        fakeRepository.blogPostBySlug = blogPost

        viewModel.loadBlogBySlug("test-blog")
        advanceUntilIdle()

        assertThat(viewModel.getBlogPost()).isEqualTo(blogPost)
    }

    // endregion

    // region Delete Blog

    @Test
    fun `deleteBlogPost success emits toast and BlogDeleted`() = runTest {
        val blogPost = createTestBlogPost()
        fakeRepository.blogPostBySlug = blogPost
        fakeRepository.deleteResult = Resource.Success(SUCCESS_BLOG_DELETED)

        viewModel.loadBlogBySlug("test-blog")
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.deleteBlogPost()
            advanceUntilIdle()

            val events = mutableListOf<UiEvent>()
            events.add(awaitItem())
            events.add(awaitItem())

            assertThat(events.filterIsInstance<UiEvent.ShowToast>()).isNotEmpty()
            assertThat(events).contains(BlogNavigationEvent.BlogDeleted)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteBlogPost does nothing when no blog post loaded`() = runTest {
        // No blog post loaded, getBlogPost() returns null
        viewModel.deleteBlogPost()
        advanceUntilIdle()

        // Should not crash or emit events
        assertThat(viewModel.isLoading.value).isFalse()
    }

    @Test
    fun `deleteBlogPost error emits error event`() = runTest {
        val blogPost = createTestBlogPost()
        fakeRepository.blogPostBySlug = blogPost
        fakeRepository.deleteResult = Resource.Error("Delete failed")

        viewModel.loadBlogBySlug("test-blog")
        advanceUntilIdle()

        viewModel.events.test {
            viewModel.deleteBlogPost()
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.ShowErrorDialog::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Update Blog

    @Test
    fun `setUpdatedBlogFields updates update state`() {
        viewModel.setUpdatedBlogFields("New Title", "New Body", null)

        val state = viewModel.updateBlogState.value
        assertThat(state.updatedBlogTitle).isEqualTo("New Title")
        assertThat(state.updatedBlogBody).isEqualTo("New Body")
    }

    @Test
    fun `updateBlogPost success emits BlogUpdateSuccess and updates state`() = runTest {
        val updatedPost = createTestBlogPost(title = "Updated Title", body = "Updated Body")
        fakeRepository.updateResult = Resource.Success(updatedPost)

        viewModel.events.test {
            viewModel.updateBlogPost("test-blog", "Updated Title", "Updated Body", null)
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isEqualTo(BlogNavigationEvent.BlogUpdateSuccess)
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(viewModel.viewBlogState.value.blogPost).isEqualTo(updatedPost)
    }

    @Test
    fun `updateBlogPost error emits error event`() = runTest {
        fakeRepository.updateResult = Resource.Error("Update failed")

        viewModel.events.test {
            viewModel.updateBlogPost("test-blog", "Title", "Body", null)
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.ShowErrorDialog::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion
}
