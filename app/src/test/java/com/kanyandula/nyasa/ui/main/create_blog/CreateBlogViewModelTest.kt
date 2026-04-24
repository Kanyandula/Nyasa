@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.domain.usecase.category.GetCategoriesUseCase
import com.kanyandula.nyasa.fakes.FakeAnalyticsTracker
import com.kanyandula.nyasa.fakes.FakeCategoryRepository
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.util.MainDispatcherRule
import com.kanyandula.nyasa.work.BlogUploadEnqueuer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        every { enqueuer.enqueueCreate(any(), any(), any(), any(), any()) } returns UUID.randomUUID()
        viewModel = CreateBlogViewModel(
            blogUploadEnqueuer = enqueuer,
            getCategoriesUseCase = GetCategoriesUseCase(fakeCategoryRepository),
            analyticsTracker = FakeAnalyticsTracker()
        )
    }

    @Test
    fun `createNewBlogPost enqueues upload via BlogUploadEnqueuer`() {
        viewModel.createNewBlogPost("Title", "Body", null)

        verify {
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
    fun `createNewBlogPost emits ShowToast event`() = kotlinx.coroutines.test.runTest {
        viewModel.events.test {
            viewModel.createNewBlogPost("Title", "Body", null)

            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.ShowToast::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createNewBlogPost clears fields after enqueue`() {
        viewModel.setNewBlogFields("Title", "Body", null)
        viewModel.createNewBlogPost("Title", "Body", null)

        val state = viewModel.viewState.value
        assertThat(state.blogFields.newBlogTitle).isNull()
        assertThat(state.blogFields.newBlogBody).isNull()
        assertThat(state.blogFields.newImageUri).isNull()
    }

    @Test
    fun `setNewBlogFields updates state`() {
        viewModel.setNewBlogFields("Test Title", "Test Body", null)

        val state = viewModel.viewState.value
        assertThat(state.blogFields.newBlogTitle).isEqualTo("Test Title")
        assertThat(state.blogFields.newBlogBody).isEqualTo("Test Body")
    }

    @Test
    fun `clearNewBlogFields resets to defaults`() {
        viewModel.setNewBlogFields("Title", "Body", null)
        viewModel.clearNewBlogFields()

        val state = viewModel.viewState.value
        assertThat(state.blogFields.newBlogTitle).isNull()
        assertThat(state.blogFields.newBlogBody).isNull()
        assertThat(state.blogFields.newImageUri).isNull()
    }
}
