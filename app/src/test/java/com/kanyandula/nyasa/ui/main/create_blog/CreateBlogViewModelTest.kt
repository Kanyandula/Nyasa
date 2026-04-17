@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.domain.usecase.category.GetCategoriesUseCase
import com.kanyandula.nyasa.domain.usecase.createblog.CreateBlogPostUseCase
import com.kanyandula.nyasa.fakes.FakeCategoryRepository
import com.kanyandula.nyasa.fakes.FakeCreateBlogRepository
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.MainDispatcherRule
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.SuccessHandling.SUCCESS_BLOG_CREATED
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateBlogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeCreateBlogRepository
    private lateinit var fakeCategoryRepository: FakeCategoryRepository
    private lateinit var viewModel: CreateBlogViewModel

    @Before
    fun setup() {
        fakeRepository = FakeCreateBlogRepository()
        fakeCategoryRepository = FakeCategoryRepository()
        viewModel = CreateBlogViewModel(
            createBlogPostUseCase = CreateBlogPostUseCase(fakeRepository),
            getCategoriesUseCase = GetCategoriesUseCase(fakeCategoryRepository)
        )
    }

    @Test
    fun `createNewBlogPost success emits success dialog`() = runTest {
        fakeRepository.createResult = Resource.Success(SUCCESS_BLOG_CREATED)

        viewModel.events.test {
            viewModel.createNewBlogPost("Title", "Body", null)
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.ShowSuccessDialog::class.java)
            assertThat((event as UiEvent.ShowSuccessDialog).message).isEqualTo(SUCCESS_BLOG_CREATED)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `createNewBlogPost success clears fields when blog created`() = runTest {
        fakeRepository.createResult = Resource.Success(SUCCESS_BLOG_CREATED)

        viewModel.setNewBlogFields("Title", "Body", null)
        viewModel.createNewBlogPost("Title", "Body", null)
        advanceUntilIdle()

        val state = viewModel.viewState.value
        assertThat(state.blogFields.newBlogTitle).isNull()
        assertThat(state.blogFields.newBlogBody).isNull()
        assertThat(state.blogFields.newImageUri).isNull()
    }

    @Test
    fun `createNewBlogPost error emits error event`() = runTest {
        fakeRepository.createResult = Resource.Error(AppError.Unknown(RuntimeException("Failed to create")))

        viewModel.events.test {
            viewModel.createNewBlogPost("Title", "Body", null)
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.ShowErrorDialog::class.java)
            cancelAndIgnoreRemainingEvents()
        }
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

    @Test
    fun `loading state is false after successful create`() = runTest {
        fakeRepository.createResult = Resource.Success(SUCCESS_BLOG_CREATED)

        viewModel.createNewBlogPost("Title", "Body", null)
        advanceUntilIdle()

        assertThat(viewModel.isLoading.value).isFalse()
    }
}
