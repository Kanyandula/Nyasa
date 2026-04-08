@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.repository.main

import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.BlogCreateUpdateResponse
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.Constants.RESPONSE_MUST_HAVE_NYASABLOG_UER
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
class CreateBlogRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var apiService: NyasaBlogApiMainService
    private lateinit var blogPostDao: BlogPostDao
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var repository: CreateBlogRepositoryImpl

    @Before
    fun setup() {
        apiService = mockk()
        blogPostDao = mockk(relaxed = true)
        connectivityObserver = mockk()
        every { connectivityObserver.isConnected } returns MutableStateFlow(true)
        repository = CreateBlogRepositoryImpl(apiService, blogPostDao, connectivityObserver)
    }

    private fun createResponse(response: String = "Success"): BlogCreateUpdateResponse {
        return mockk(relaxed = true) {
            every { this@mockk.response } returns response
            every { pk } returns 1
            every { title } returns "Test Blog"
            every { slug } returns "test-blog"
            every { body } returns "Test body"
            every { image } returns ""
            every { date_updated } returns "2024-01-01T00:00:00Z"
            every { username } returns "testuser"
            every { category } returns null
            every { tags } returns emptyList()
            every { reading_time } returns 5
        }
    }

    @Test
    fun `createNewBlogPost success inserts to DAO and returns response`() = runTest {
        val blogResponse = createResponse("Success")
        coEvery { apiService.createBlog(any(), any(), any(), any(), any()) } returns
            Response.success(blogResponse)

        val results = repository.createNewBlogPost(
            "Test Blog",
            "Test body",
            null,
            null,
            null
        ).toList()

        val success = results.last() as Resource.Success
        assertThat(success.data).isEqualTo("Success")
        coVerify { blogPostDao.insert(any()) }
    }

    @Test
    fun `createNewBlogPost with auth error does not insert to DAO`() = runTest {
        val blogResponse = createResponse(RESPONSE_MUST_HAVE_NYASABLOG_UER)
        coEvery { apiService.createBlog(any(), any(), any(), any(), any()) } returns
            Response.success(blogResponse)

        val results = repository.createNewBlogPost(
            "Test Blog",
            "Test body",
            null,
            null,
            null
        ).toList()

        val success = results.last() as Resource.Success
        assertThat(success.data).isEqualTo(RESPONSE_MUST_HAVE_NYASABLOG_UER)
        coVerify(exactly = 0) { blogPostDao.insert(any()) }
    }

    @Test
    fun `createNewBlogPost offline returns error`() = runTest {
        every { connectivityObserver.isConnected } returns MutableStateFlow(false)

        val results = repository.createNewBlogPost(
            "Test Blog",
            "Test body",
            null,
            null,
            null
        ).toList()

        assertThat(results.last()).isInstanceOf(Resource.Error::class.java)
    }
}
