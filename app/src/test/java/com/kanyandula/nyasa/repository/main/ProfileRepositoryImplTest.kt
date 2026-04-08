package com.kanyandula.nyasa.repository.main

import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.UserProfileResponse
import com.kanyandula.nyasa.models.ProfileUpdateRequest
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.MainDispatcherRule
import com.kanyandula.nyasa.util.Resource
import io.mockk.coEvery
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
class ProfileRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var apiService: NyasaBlogApiMainService
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var repository: ProfileRepositoryImpl

    @Before
    fun setup() {
        apiService = mockk()
        connectivityObserver = mockk()
        every { connectivityObserver.isConnected } returns MutableStateFlow(true)
        repository = ProfileRepositoryImpl(apiService, connectivityObserver)
    }

    @Test
    fun `getProfile success returns mapped profile`() = runTest {
        val response = UserProfileResponse(
            username = "author1",
            bio = "Writer from Malawi"
        )
        coEvery { apiService.getProfile("author1") } returns Response.success(response)

        val results = repository.getProfile("author1").toList()

        val success = results.last() as Resource.Success
        assertThat(success.data.username).isEqualTo("author1")
        assertThat(success.data.bio).isEqualTo("Writer from Malawi")
    }

    @Test
    fun `getProfile offline returns error`() = runTest {
        every { connectivityObserver.isConnected } returns MutableStateFlow(false)

        val results = repository.getProfile("author1").toList()

        assertThat(results.last()).isInstanceOf(Resource.Error::class.java)
    }

    @Test
    fun `updateProfile success returns updated profile`() = runTest {
        val response = UserProfileResponse(
            username = "author1",
            bio = "Updated bio"
        )
        coEvery {
            apiService.updateProfile(
                bio = "Updated bio",
                location = null,
                website = null,
                twitter = null,
                facebook = null,
                instagram = null,
                linkedin = null
            )
        } returns Response.success(response)

        val request = ProfileUpdateRequest(bio = "Updated bio")
        val results = repository.updateProfile(request).toList()

        val success = results.last() as Resource.Success
        assertThat(success.data.bio).isEqualTo("Updated bio")
    }

    @Test
    fun `updateProfile offline returns error`() = runTest {
        every { connectivityObserver.isConnected } returns MutableStateFlow(false)

        val request = ProfileUpdateRequest(bio = "Updated bio")
        val results = repository.updateProfile(request).toList()

        assertThat(results.last()).isInstanceOf(Resource.Error::class.java)
    }
}
