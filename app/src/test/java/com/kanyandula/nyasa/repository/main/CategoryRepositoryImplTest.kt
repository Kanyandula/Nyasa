package com.kanyandula.nyasa.repository.main

import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.CategoryResponse
import com.kanyandula.nyasa.api.main.responses.TagResponse
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.MainDispatcherRule
import com.kanyandula.nyasa.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var apiService: NyasaBlogApiMainService
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setup() {
        apiService = mockk()
        connectivityObserver = mockk()
        every { connectivityObserver.isConnected } returns MutableStateFlow(true)
        repository = CategoryRepositoryImpl(apiService, connectivityObserver)
    }

    @Test
    fun `getCategories success returns mapped categories`() = runTest {
        val responses = listOf(
            CategoryResponse(pk = 1, name = "Travel", slug = "travel"),
            CategoryResponse(pk = 2, name = "Culture", slug = "culture")
        )
        coEvery { apiService.getCategories() } returns Response.success(responses)

        val results = repository.getCategories().toList()

        val success = results.last() as Resource.Success
        assertThat(success.data).hasSize(2)
        assertThat(success.data[0].name).isEqualTo("Travel")
        assertThat(success.data[1].slug).isEqualTo("culture")
    }

    @Test
    fun `getCategories caches the result and hits the api once across calls`() = runTest {
        coEvery { apiService.getCategories() } returns Response.success(
            listOf(CategoryResponse(pk = 1, name = "Travel", slug = "travel"))
        )

        val first = repository.getCategories().toList()
        val second = repository.getCategories().toList()

        assertThat((first.last() as Resource.Success).data[0].name).isEqualTo("Travel")
        assertThat((second.last() as Resource.Success).data[0].name).isEqualTo("Travel")
        coVerify(exactly = 1) { apiService.getCategories() }
    }

    @Test
    fun `getCategories does not cache an error result`() = runTest {
        val connected = MutableStateFlow(false)
        every { connectivityObserver.isConnected } returns connected

        // First call is offline -> error; must not be cached.
        assertThat(repository.getCategories().toList().last()).isInstanceOf(Resource.Error::class.java)

        connected.value = true
        coEvery { apiService.getCategories() } returns Response.success(
            listOf(CategoryResponse(pk = 1, name = "Travel", slug = "travel"))
        )
        val retry = repository.getCategories().toList()

        assertThat((retry.last() as Resource.Success).data[0].name).isEqualTo("Travel")
        coVerify(exactly = 1) { apiService.getCategories() }
    }

    @Test
    fun `getCategories offline returns error`() = runTest {
        every { connectivityObserver.isConnected } returns MutableStateFlow(false)

        val results = repository.getCategories().toList()

        assertThat(results.last()).isInstanceOf(Resource.Error::class.java)
    }

    @Test
    fun `getTags success returns mapped tags`() = runTest {
        val responses = listOf(
            TagResponse(pk = 1, name = "Malawi", slug = "malawi"),
            TagResponse(pk = 2, name = "Photography", slug = "photography")
        )
        coEvery { apiService.getTags() } returns Response.success(responses)

        val results = repository.getTags().toList()

        val success = results.last() as Resource.Success
        assertThat(success.data).hasSize(2)
        assertThat(success.data[0].name).isEqualTo("Malawi")
    }

    @Test
    fun `getTags offline returns error`() = runTest {
        every { connectivityObserver.isConnected } returns MutableStateFlow(false)

        val results = repository.getTags().toList()

        assertThat(results.last()).isInstanceOf(Resource.Error::class.java)
    }
}
