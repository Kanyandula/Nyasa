package com.kanyandula.nyasa.repository.main

import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.api.GenericResponse
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.CommentResponse
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.persistance.CommentDao
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.MainDispatcherRule
import com.kanyandula.nyasa.util.Resource
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class CommentRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var apiService: NyasaBlogApiMainService
    private lateinit var commentDao: CommentDao
    private lateinit var blogPostDao: BlogPostDao
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var repository: CommentRepositoryImpl

    @Before
    fun setup() {
        apiService = mockk()
        commentDao = mockk()
        blogPostDao = mockk()
        connectivityObserver = mockk()
        every { connectivityObserver.isConnected } returns MutableStateFlow(true)
        every { commentDao.getBySlug(any()) } returns flowOf(emptyList())
        coJustRun { commentDao.clearBySlug(any()) }
        coJustRun { commentDao.insertAll(any()) }
        coJustRun { commentDao.insert(any()) }
        coJustRun { commentDao.deleteByPk(any()) }
        coJustRun { blogPostDao.updateCommentCount(any(), any()) }
        repository = CommentRepositoryImpl(apiService, commentDao, blogPostDao, connectivityObserver)
    }

    @Test
    fun `getComments success returns mapped comments`() = runTest {
        val responses = listOf(
            CommentResponse(pk = 1, body = "Great post!", username = "reader1"),
            CommentResponse(pk = 2, body = "Thanks!", username = "reader2")
        )
        coEvery { apiService.getComments("test-slug") } returns Response.success(responses)

        val results = repository.getComments("test-slug").toList()

        assertThat(results.first()).isInstanceOf(Resource.Loading::class.java)
        val success = results.last() as Resource.Success
        assertThat(success.data).hasSize(2)
        assertThat(success.data[0].body).isEqualTo("Great post!")
        assertThat(success.data[1].username).isEqualTo("reader2")
    }

    @Test
    fun `getComments offline returns error`() = runTest {
        every { connectivityObserver.isConnected } returns MutableStateFlow(false)

        val results = repository.getComments("test-slug").toList()

        val last = results.last()
        assertThat(last).isInstanceOf(Resource.Error::class.java)
        assertThat((last as Resource.Error).error).isEqualTo(AppError.Offline)
    }

    @Test
    fun `createComment success returns mapped comment`() = runTest {
        val response = CommentResponse(pk = 3, body = "New comment", username = "author")
        coEvery { apiService.createComment("slug", "New comment") } returns Response.success(response)

        val results = repository.createComment("slug", "New comment").toList()

        val success = results.last() as Resource.Success
        assertThat(success.data.pk).isEqualTo(3)
        assertThat(success.data.body).isEqualTo("New comment")
    }

    @Test
    fun `createComment offline returns error`() = runTest {
        every { connectivityObserver.isConnected } returns MutableStateFlow(false)

        val results = repository.createComment("slug", "comment").toList()

        assertThat(results.last()).isInstanceOf(Resource.Error::class.java)
    }

    @Test
    fun `deleteComment success returns response string`() = runTest {
        coEvery { apiService.deleteComment(1) } returns
            Response.success(GenericResponse("Comment deleted"))

        val results = repository.deleteComment(1, "test-slug").toList()

        val success = results.last() as Resource.Success
        assertThat(success.data).isEqualTo("Comment deleted")
    }

    @Test
    fun `deleteComment offline returns error`() = runTest {
        every { connectivityObserver.isConnected } returns MutableStateFlow(false)

        val results = repository.deleteComment(1, "test-slug").toList()

        assertThat(results.last()).isInstanceOf(Resource.Error::class.java)
    }
}
