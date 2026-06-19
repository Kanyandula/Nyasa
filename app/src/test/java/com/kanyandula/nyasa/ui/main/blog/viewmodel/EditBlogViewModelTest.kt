package com.kanyandula.nyasa.ui.main.blog.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.domain.usecase.blog.GetBlogPostBySlugUseCase
import com.kanyandula.nyasa.domain.usecase.category.GetCategoriesUseCase
import com.kanyandula.nyasa.fakes.FakeBlogRepository
import com.kanyandula.nyasa.fakes.FakeCategoryRepository
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.models.Category
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.ui.main.blog.state.BlogNavigationEvent
import com.kanyandula.nyasa.util.MainDispatcherRule
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.work.BlogUploadEnqueuer
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditBlogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeBlogRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var uploadEnqueuer: BlogUploadEnqueuer
    private lateinit var viewModel: EditBlogViewModel

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
        fakeCategoryRepository = FakeCategoryRepository()
        uploadEnqueuer = mockk(relaxed = true)
        coEvery {
            uploadEnqueuer.enqueueUpdate(any(), any(), any(), any(), any(), any())
        } returns java.util.UUID.randomUUID()
        viewModel = createViewModel()
    }

    private fun createViewModel() = EditBlogViewModel(
        blogUploadEnqueuer = uploadEnqueuer,
        getCategoriesUseCase = GetCategoriesUseCase(fakeCategoryRepository),
        getBlogPostBySlugUseCase = GetBlogPostBySlugUseCase(fakeRepository)
    )

    @Test
    fun `loadBlogForEdit seeds editable state from the repository`() = runTest {
        fakeRepository.blogPostBySlug = createTestBlogPost(
            title = "Original Title",
            // HTML markup proves the body is parsed via setHtml (setText would leave the tags as
            // literal text, so toText() would not strip them).
            body = "<b>Body text</b>",
            image = "https://example.com/pic.jpg"
        ).copy(category = "Tech", tags = "a,b")

        viewModel.loadBlogForEdit("test-blog")
        advanceUntilIdle()

        val state = viewModel.viewState.value
        assertThat(state.updatedBlogTitle).isEqualTo("Original Title")
        assertThat(state.originalImageUrl).isEqualTo("https://example.com/pic.jpg")
        assertThat(state.updatedCategory).isEqualTo("Tech")
        assertThat(state.updatedTags).isEqualTo("a,b")
        assertThat(state.updatedImageUri).isNull()
        assertThat(viewModel.editBodyState.toText().trim()).isEqualTo("Body text")
    }

    @Test
    fun `loadBlogForEdit emits a single error when the same missing slug is retried`() = runTest {
        fakeRepository.blogPostBySlug = null

        viewModel.events.test {
            viewModel.loadBlogForEdit("missing")
            advanceUntilIdle()
            assertThat(awaitItem()).isInstanceOf(UiEvent.ShowErrorDialog::class.java)

            // A retry of the same missing slug must not re-fetch or re-emit the error.
            viewModel.loadBlogForEdit("missing")
            advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadBlogForEdit emits error when the slug is not found`() = runTest {
        fakeRepository.blogPostBySlug = null

        viewModel.events.test {
            viewModel.loadBlogForEdit("missing")
            advanceUntilIdle()

            assertThat(awaitItem()).isInstanceOf(UiEvent.ShowErrorDialog::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadBlogForEdit does not re-seed when the same slug reloads`() = runTest {
        fakeRepository.blogPostBySlug = createTestBlogPost(title = "First")
        viewModel.loadBlogForEdit("test-blog")
        advanceUntilIdle()

        // A reload of the same slug must not clobber in-progress edits (e.g. after rotation).
        fakeRepository.blogPostBySlug = createTestBlogPost(title = "Second")
        viewModel.loadBlogForEdit("test-blog")
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.updatedBlogTitle).isEqualTo("First")
    }

    @Test
    fun `setUpdatedBlogFields updates title`() {
        viewModel.setUpdatedBlogFields(title = "New Title")
        assertThat(viewModel.viewState.value.updatedBlogTitle).isEqualTo("New Title")
    }

    @Test
    fun `setUpdatedCategory updates category`() {
        viewModel.setUpdatedCategory("Sports")
        assertThat(viewModel.viewState.value.updatedCategory).isEqualTo("Sports")
    }

    @Test
    fun `setUpdatedTags updates tags`() {
        viewModel.setUpdatedTags("x,y")
        assertThat(viewModel.viewState.value.updatedTags).isEqualTo("x,y")
    }

    @Test
    fun `updateBlogPost enqueues upload and emits BlogUpdateSuccess`() = runTest {
        viewModel.events.test {
            viewModel.updateBlogPost("test-blog", "Updated Title", "Updated Body", null)
            advanceUntilIdle()

            assertThat(awaitItem()).isEqualTo(BlogNavigationEvent.BlogUpdateSuccess)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify {
            uploadEnqueuer.enqueueUpdate(
                slug = "test-blog",
                title = "Updated Title",
                body = "Updated Body",
                imageUri = null,
                category = any(),
                tagsCsv = any()
            )
        }
    }

    @Test
    fun `updateBlogPost ignores re-entrant calls while loading`() = runTest {
        viewModel.updateBlogPost("test-blog", "First", "First body", null)
        viewModel.updateBlogPost("test-blog", "Second", "Second body", null)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            uploadEnqueuer.enqueueUpdate(any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `categories are loaded into state on init`() = runTest {
        fakeCategoryRepository.categoriesResult = Resource.Success(
            listOf(Category(pk = 1, name = "Tech", slug = "tech"))
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.categories).hasSize(1)
    }
}
