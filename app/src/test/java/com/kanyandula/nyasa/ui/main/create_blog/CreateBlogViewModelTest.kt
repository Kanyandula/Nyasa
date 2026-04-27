@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.domain.usecase.category.GetCategoriesUseCase
import com.kanyandula.nyasa.fakes.FakeAnalyticsTracker
import com.kanyandula.nyasa.fakes.FakeCategoryRepository
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogNavigationEvent
import com.kanyandula.nyasa.util.MainDispatcherRule
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
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class CreateBlogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var enqueuer: BlogUploadEnqueuer
    private lateinit var viewModel: CreateBlogViewModel

    @Before
    fun setup() {
        fakeCategoryRepository = FakeCategoryRepository()
        enqueuer = mockk(relaxed = true)
        coEvery { enqueuer.enqueueCreate(any(), any(), any(), any(), any()) } returns UUID.randomUUID()
        viewModel = CreateBlogViewModel(
            blogUploadEnqueuer = enqueuer,
            getCategoriesUseCase = GetCategoriesUseCase(fakeCategoryRepository),
            analyticsTracker = FakeAnalyticsTracker()
        )
    }

    @Test
    fun `createNewBlogPost enqueues upload via BlogUploadEnqueuer`() = runTest {
        viewModel.createNewBlogPost("Title", "Body", null)
        advanceUntilIdle()

        coVerify {
            enqueuer.enqueueCreate(
                title = "Title",
                body = "Body",
                imageUri = null,
                category = any(),
                tagsCsv = any()
            )
        }
    }

    @Test
    fun `createNewBlogPost emits BlogCreated nav event`() = runTest {
        viewModel.events.test {
            viewModel.createNewBlogPost("Title", "Body", null)
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isEqualTo(CreateBlogNavigationEvent.BlogCreated)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createNewBlogPost clears fields after enqueue`() = runTest {
        viewModel.setNewBlogFields(title = "Title")
        viewModel.createNewBlogPost("Title", "Body", null)
        advanceUntilIdle()

        val state = viewModel.viewState.value
        assertThat(state.blogFields.newBlogTitle).isNull()
        assertThat(state.blogFields.newImageUri).isNull()
    }

    @Test
    fun `createNewBlogPost ignores re-entrant calls while loading`() = runTest {
        viewModel.createNewBlogPost("Title", "Body", null)
        viewModel.createNewBlogPost("Other", "Other body", null)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            enqueuer.enqueueCreate(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `setNewBlogFields updates state`() {
        viewModel.setNewBlogFields(title = "Test Title")

        assertThat(viewModel.viewState.value.blogFields.newBlogTitle).isEqualTo("Test Title")
    }

    @Test
    fun `clearNewBlogFields resets to defaults`() {
        viewModel.setNewBlogFields(title = "Title")
        viewModel.clearNewBlogFields()

        val state = viewModel.viewState.value
        assertThat(state.blogFields.newBlogTitle).isNull()
        assertThat(state.blogFields.newImageUri).isNull()
    }

    @Test
    fun `clearNewBlogFields clears the rich-text editor body too`() {
        viewModel.createBodyState.setHtml("<p>some draft</p>")

        viewModel.clearNewBlogFields()

        assertThat(viewModel.createBodyState.toHtml()).doesNotContain("some draft")
    }
}
