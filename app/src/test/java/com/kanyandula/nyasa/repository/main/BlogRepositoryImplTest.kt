package com.kanyandula.nyasa.repository.main

import androidx.room.withTransaction
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.api.GenericResponse
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.BlogSearchResponse
import com.kanyandula.nyasa.api.main.responses.BookmarkResponse
import com.kanyandula.nyasa.api.main.responses.LikeResponse
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.persistance.AppDatabase
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.MainDispatcherRule
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.SuccessHandling.RESPONSE_HAS_PERMISSION_TO_EDIT
import com.kanyandula.nyasa.util.SuccessHandling.RESPONSE_NO_PERMISSION_TO_EDIT
import com.kanyandula.nyasa.util.SuccessHandling.SUCCESS_BLOG_DELETED
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class BlogRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var apiService: NyasaBlogApiMainService
    private lateinit var database: AppDatabase
    private lateinit var blogPostDao: BlogPostDao
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var repository: BlogRepositoryImpl

    private val testPost = BlogPost(
        pk = 1,
        title = "Test",
        slug = "test",
        body = "Body",
        image = "",
        date_updated = 0L,
        username = "user"
    )

    @Before
    fun setup() {
        apiService = mockk()
        blogPostDao = mockk(relaxed = true)
        database = mockk(relaxed = true)
        every { database.getBlogPostDao() } returns blogPostDao
        connectivityObserver = mockk()
        every { connectivityObserver.isConnected } returns MutableStateFlow(true)

        // Run withTransaction blocks inline so DAO writes execute against the mocks.
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery {
            database.withTransaction(captureLambda<suspend () -> Unit>())
        } coAnswers {
            lambda<suspend () -> Unit>().captured.invoke()
        }

        repository = BlogRepositoryImpl(apiService, database, connectivityObserver)
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun `isAuthorOfBlogPost returns true when has permission`() = runTest {
        coEvery { apiService.isAuthorOfBlogPost("test") } returns
            Response.success(GenericResponse(RESPONSE_HAS_PERMISSION_TO_EDIT))

        val results = repository.isAuthorOfBlogPost("test").toList()

        val success = results.last() as Resource.Success
        assertThat(success.data).isTrue()
    }

    @Test
    fun `isAuthorOfBlogPost returns false when no permission`() = runTest {
        coEvery { apiService.isAuthorOfBlogPost("test") } returns
            Response.success(GenericResponse(RESPONSE_NO_PERMISSION_TO_EDIT))

        val results = repository.isAuthorOfBlogPost("test").toList()

        val success = results.last() as Resource.Success
        assertThat(success.data).isFalse()
    }

    @Test
    fun `isAuthorOfBlogPost offline returns error`() = runTest {
        every { connectivityObserver.isConnected } returns MutableStateFlow(false)

        val results = repository.isAuthorOfBlogPost("test").toList()

        assertThat(results.last()).isInstanceOf(Resource.Error::class.java)
    }

    @Test
    fun `deleteBlogPost success deletes from DAO`() = runTest {
        coEvery { apiService.deleteBlogPost("test") } returns
            Response.success(GenericResponse(SUCCESS_BLOG_DELETED))

        val results = repository.deleteBlogPost(testPost).toList()

        val success = results.last() as Resource.Success
        assertThat(success.data).isEqualTo(SUCCESS_BLOG_DELETED)
        coVerify { blogPostDao.deleteBlogPost(testPost) }
    }

    @Test
    fun `deleteBlogPost unknown response returns error`() = runTest {
        coEvery { apiService.deleteBlogPost("test") } returns
            Response.success(GenericResponse("Something else"))

        val results = repository.deleteBlogPost(testPost).toList()

        assertThat(results.last()).isInstanceOf(Resource.Error::class.java)
    }

    @Test
    fun `deleteBlogPost offline returns error`() = runTest {
        every { connectivityObserver.isConnected } returns MutableStateFlow(false)

        val results = repository.deleteBlogPost(testPost).toList()

        assertThat(results.last()).isInstanceOf(Resource.Error::class.java)
    }

    @Test
    fun `likeBlogPost success returns like result`() = runTest {
        coEvery { apiService.likeBlogPost("test") } returns
            Response.success(LikeResponse(liked = true, like_count = 5))

        val results = repository.likeBlogPost("test").toList()

        val success = results.last() as Resource.Success
        assertThat(success.data.liked).isTrue()
        assertThat(success.data.likeCount).isEqualTo(5)
    }

    @Test
    fun `bookmarkBlogPost success returns bookmarked state`() = runTest {
        coEvery { apiService.bookmarkBlogPost("test") } returns
            Response.success(BookmarkResponse(bookmarked = true))

        val results = repository.bookmarkBlogPost("test").toList()

        val success = results.last() as Resource.Success
        assertThat(success.data).isTrue()
    }

    @Test
    fun `getBookmarks success returns mapped blog posts`() = runTest {
        val searchResponse = mockk<BlogSearchResponse>(relaxed = true) {
            every { pk } returns 1
            every { title } returns "Bookmarked Post"
            every { slug } returns "bookmarked"
            every { body } returns "Body"
            every { image } returns ""
            every { date_updated } returns "2024-01-01T00:00:00Z"
            every { username } returns "user"
            every { category } returns null
            every { tags } returns emptyList()
            every { reading_time } returns 3
            every { like_count } returns 0
        }
        coEvery { apiService.getBookmarks() } returns
            Response.success(listOf(searchResponse))

        val results = repository.getBookmarks().toList()

        val success = results.last() as Resource.Success
        assertThat(success.data).hasSize(1)
        assertThat(success.data[0].title).isEqualTo("Bookmarked Post")
        coVerify { blogPostDao.insertAll(match { it.size == 1 && it[0].slug == "bookmarked" }) }
    }
}
