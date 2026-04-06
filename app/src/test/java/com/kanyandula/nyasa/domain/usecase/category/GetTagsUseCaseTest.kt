package com.kanyandula.nyasa.domain.usecase.category

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.fakes.FakeCategoryRepository
import com.kanyandula.nyasa.models.Tag
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetTagsUseCaseTest {

    private lateinit var fakeRepository: FakeCategoryRepository
    private lateinit var useCase: GetTagsUseCase

    @Before
    fun setup() {
        fakeRepository = FakeCategoryRepository()
        useCase = GetTagsUseCase(fakeRepository)
    }

    @Test
    fun `invoke emits loading then success with tags`() = runTest {
        val tags = listOf(
            Tag(pk = 1, name = "Kotlin", slug = "kotlin"),
            Tag(pk = 2, name = "Android", slug = "android")
        )
        fakeRepository.tagsResult = Resource.Success(tags)

        useCase().test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val success = awaitItem() as Resource.Success
            assertThat(success.data).hasSize(2)
            assertThat(success.data[0].name).isEqualTo("Kotlin")
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits loading then error on failure`() = runTest {
        fakeRepository.tagsResult = Resource.Error("Network error")

        useCase().test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val error = awaitItem() as Resource.Error
            assertThat(error.message).isEqualTo("Network error")
            awaitComplete()
        }
    }
}
