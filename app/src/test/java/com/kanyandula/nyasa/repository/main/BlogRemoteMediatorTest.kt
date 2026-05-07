package com.kanyandula.nyasa.repository.main

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.BlogListSearchResponse
import com.kanyandula.nyasa.api.main.responses.BlogSearchResponse
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.models.BlogRemoteKey
import com.kanyandula.nyasa.persistance.AppDatabase
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.persistance.BlogQueryUtils
import com.kanyandula.nyasa.persistance.BlogRemoteKeyDao
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalPagingApi::class)
class BlogRemoteMediatorTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var apiService: NyasaBlogApiMainService
    private lateinit var database: AppDatabase
    private lateinit var blogPostDao: BlogPostDao
    private lateinit var remoteKeyDao: BlogRemoteKeyDao
    private lateinit var connectivityObserver: ConnectivityObserver

    @Before
    fun setup() {
        apiService = mockk()
        blogPostDao = mockk(relaxed = true)
        remoteKeyDao = mockk(relaxed = true)
        database = mockk(relaxed = true)
        every { database.getBlogPostDao() } returns blogPostDao
        every { database.getBlogRemoteKeyDao() } returns remoteKeyDao
        connectivityObserver = mockk()
        every { connectivityObserver.isConnected } returns MutableStateFlow(true)

        // Run the withTransaction block inline so DAO writes (clearAll, insertAll,
        // remoteKey insert) execute against the mocks and can be coVerified.
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery {
            database.withTransaction(captureLambda<suspend () -> Unit>())
        } coAnswers {
            lambda<suspend () -> Unit>().captured.invoke()
        }
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    // Regression: original bug was `endOfPagination = results.size < PAGINATION_PAGE_SIZE`,
    // which short-circuited whenever the server's PAGE_SIZE was smaller than the client
    // constant — producing a "missing posts" symptom on the All filter.
    @Test
    fun `REFRESH continues pagination when next is non-null even if results are below client page size`() = runTest {
        stubSearch(List(5) { fakeResponse(it) }, next = "https://nyasablog.com/api/blog/list?page=2")

        val result = newMediator().load(LoadType.REFRESH, emptyState())

        val success = result as RemoteMediator.MediatorResult.Success
        assertThat(success.endOfPaginationReached).isFalse()
        coVerify { blogPostDao.clearAll() }
        coVerify { blogPostDao.insertAll(any()) }
    }

    @Test
    fun `APPEND continues pagination when next is non-null even if results are below client page size`() = runTest {
        coEvery { remoteKeyDao.getRemoteKey(any()) } returns BlogRemoteKey(
            queryKey = "search=&ordering=${BlogQueryUtils.ORDER_BY_DESC_DATE_UPDATED}",
            nextPage = 2,
            lastUpdated = System.currentTimeMillis()
        )
        stubSearch(List(4) { fakeResponse(it + 100) }, next = "https://nyasablog.com/api/blog/list?page=3")

        val result = newMediator().load(LoadType.APPEND, emptyState())

        val success = result as RemoteMediator.MediatorResult.Success
        assertThat(success.endOfPaginationReached).isFalse()
        // APPEND must NOT clear the cache — only REFRESH does.
        coVerify(exactly = 0) { blogPostDao.clearAll() }
        coVerify { blogPostDao.insertAll(any()) }
    }

    @Test
    fun `REFRESH reaches end of pagination when next is null`() = runTest {
        stubSearch(List(10) { fakeResponse(it) }, next = null)

        val result = newMediator().load(LoadType.REFRESH, emptyState())

        val success = result as RemoteMediator.MediatorResult.Success
        assertThat(success.endOfPaginationReached).isTrue()
    }

    @Test
    fun `REFRESH reaches end of pagination when next is blank`() = runTest {
        stubSearch(List(3) { fakeResponse(it) }, next = "")

        val result = newMediator().load(LoadType.REFRESH, emptyState())

        val success = result as RemoteMediator.MediatorResult.Success
        assertThat(success.endOfPaginationReached).isTrue()
    }

    private fun stubSearch(results: List<BlogSearchResponse>, next: String?) {
        coEvery {
            apiService.searchListBlogPosts(any(), any(), any(), any(), any())
        } returns Response.success(
            BlogListSearchResponse(results = results, next = next, detail = "")
        )
    }

    private fun newMediator() = BlogRemoteMediator(
        query = "",
        filterAndOrder = BlogQueryUtils.ORDER_BY_DESC_DATE_UPDATED,
        category = null,
        apiService = apiService,
        database = database,
        connectivityObserver = connectivityObserver
    )

    private fun emptyState() = PagingState<Int, BlogPost>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 10),
        leadingPlaceholderCount = 0
    )

    private fun fakeResponse(index: Int) = BlogSearchResponse(
        pk = index,
        title = "Title $index",
        slug = "slug-$index",
        body = "Body $index",
        image = "",
        date_updated = "2026-05-06T00:00:00Z",
        username = "user$index",
        category = null,
        tags = null,
        reading_time = null,
        view_count = null,
        like_count = null,
        comment_count = null,
        author_avatar = null
    )
}
