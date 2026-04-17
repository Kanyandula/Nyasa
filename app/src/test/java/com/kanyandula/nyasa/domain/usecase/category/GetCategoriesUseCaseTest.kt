package com.kanyandula.nyasa.domain.usecase.category

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.fakes.FakeCategoryRepository
import com.kanyandula.nyasa.models.Category
import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetCategoriesUseCaseTest {

    private lateinit var fakeRepository: FakeCategoryRepository
    private lateinit var useCase: GetCategoriesUseCase

    @Before
    fun setup() {
        fakeRepository = FakeCategoryRepository()
        useCase = GetCategoriesUseCase(fakeRepository)
    }

    @Test
    fun `invoke emits loading then success with categories`() = runTest {
        val categories = listOf(
            Category(pk = 1, name = "Tech", slug = "tech"),
            Category(pk = 2, name = "Culture", slug = "culture")
        )
        fakeRepository.categoriesResult = Resource.Success(categories)

        useCase().test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val success = awaitItem() as Resource.Success
            assertThat(success.data).hasSize(2)
            assertThat(success.data[0].name).isEqualTo("Tech")
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits loading then error on failure`() = runTest {
        fakeRepository.categoriesResult = Resource.Error(AppError.Unknown(RuntimeException("Network error")))

        useCase().test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val error = awaitItem() as Resource.Error
            awaitComplete()
        }
    }
}
