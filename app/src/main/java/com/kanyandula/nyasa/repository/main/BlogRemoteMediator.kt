package com.kanyandula.nyasa.repository.main

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.toBlogPost
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.models.BlogRemoteKey
import com.kanyandula.nyasa.persistance.AppDatabase
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.Constants.PAGINATION_PAGE_SIZE
import com.kanyandula.nyasa.util.ErrorHandling
import com.kanyandula.nyasa.util.ErrorHandling.UNABLE_TODO_OPERATION_WO_INTERNET
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class BlogRemoteMediator(
    private val query: String,
    private val filterAndOrder: String,
    private val apiService: NyasaBlogApiMainService,
    private val database: AppDatabase,
    private val connectivityObserver: ConnectivityObserver
) : RemoteMediator<Int, BlogPost>() {

    companion object {
        private const val CACHE_TIMEOUT_MINUTES = 5L
    }

    private val blogPostDao = database.getBlogPostDao()
    private val remoteKeyDao = database.getBlogRemoteKeyDao()

    private fun queryKey(): String = "search=$query&ordering=$filterAndOrder"

    override suspend fun initialize(): InitializeAction {
        val remoteKey = remoteKeyDao.getRemoteKey(queryKey())
        val cacheTimeout = TimeUnit.MINUTES.toMillis(CACHE_TIMEOUT_MINUTES)
        return if (remoteKey != null &&
            System.currentTimeMillis() - remoteKey.lastUpdated < cacheTimeout
        ) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    @Suppress("ReturnCount", "TooGenericExceptionCaught")
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, BlogPost>
    ): MediatorResult {
        val key = queryKey()
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND ->
                    return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKey = remoteKeyDao.getRemoteKey(key)
                    remoteKey?.nextPage
                        ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }

            if (!connectivityObserver.isConnected.value) {
                return if (loadType == LoadType.REFRESH) {
                    MediatorResult.Success(endOfPaginationReached = false)
                } else {
                    MediatorResult.Error(Exception(UNABLE_TODO_OPERATION_WO_INTERNET))
                }
            }

            val response = apiService.searchListBlogPosts(
                query = query,
                ordering = filterAndOrder,
                page = page
            )

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                if (ErrorHandling.isPaginationDone(errorBody)) {
                    remoteKeyDao.insertOrReplace(
                        BlogRemoteKey(key, nextPage = null, lastUpdated = System.currentTimeMillis())
                    )
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                return MediatorResult.Error(Exception(response.message()))
            }

            val body = response.body() ?: return MediatorResult.Error(Exception("Empty response body"))
            val blogPosts = body.results.map { it.toBlogPost() }
            val endOfPagination = blogPosts.size < PAGINATION_PAGE_SIZE

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    blogPostDao.clearAll()
                    remoteKeyDao.deleteByQuery(key)
                }
                blogPostDao.insertAll(blogPosts)
                remoteKeyDao.insertOrReplace(
                    BlogRemoteKey(
                        queryKey = key,
                        nextPage = if (endOfPagination) null else page + 1,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
            }

            MediatorResult.Success(endOfPaginationReached = endOfPagination)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
