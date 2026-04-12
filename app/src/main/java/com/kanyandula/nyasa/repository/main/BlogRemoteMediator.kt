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
    private val category: String? = null,
    private val apiService: NyasaBlogApiMainService,
    private val database: AppDatabase,
    private val connectivityObserver: ConnectivityObserver
) : RemoteMediator<Int, BlogPost>() {

    companion object {
        private const val CACHE_TIMEOUT_MINUTES = 5L
    }

    private val blogPostDao = database.getBlogPostDao()
    private val remoteKeyDao = database.getBlogRemoteKeyDao()

    private val cachedQueryKey: String = buildString {
        append("search=$query&ordering=$filterAndOrder")
        if (!category.isNullOrBlank()) append("&category=$category")
    }

    override suspend fun initialize(): InitializeAction {
        val remoteKey = remoteKeyDao.getRemoteKey(cachedQueryKey)
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
        return try {
            val page = resolvePageOrEarlyReturn(loadType)
                ?: return MediatorResult.Success(endOfPaginationReached = true)

            if (!connectivityObserver.isConnected.value) {
                return if (loadType == LoadType.REFRESH) {
                    MediatorResult.Success(endOfPaginationReached = false)
                } else {
                    MediatorResult.Error(Exception(UNABLE_TODO_OPERATION_WO_INTERNET))
                }
            }

            fetchAndCachePage(page, loadType)
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun resolvePageOrEarlyReturn(loadType: LoadType): Int? {
        return when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> null
            LoadType.APPEND -> {
                val remoteKey = remoteKeyDao.getRemoteKey(cachedQueryKey)
                remoteKey?.nextPage
            }
        }
    }

    private suspend fun fetchAndCachePage(
        page: Int,
        loadType: LoadType
    ): MediatorResult {
        val response = apiService.searchListBlogPosts(
            query = query,
            ordering = filterAndOrder,
            page = page,
            category = category
        )

        if (!response.isSuccessful) {
            return handleErrorResponse(response)
        }

        val body = response.body()
            ?: return MediatorResult.Error(Exception("Empty response body"))
        val blogPosts = body.results.map { it.toBlogPost() }
        val endOfPagination = blogPosts.size < PAGINATION_PAGE_SIZE

        database.withTransaction {
            if (loadType == LoadType.REFRESH) {
                blogPostDao.clearAll()
                remoteKeyDao.deleteByQuery(cachedQueryKey)
            }
            blogPostDao.insertAll(blogPosts)
            remoteKeyDao.insertOrReplace(
                BlogRemoteKey(
                    queryKey = cachedQueryKey,
                    nextPage = if (endOfPagination) null else page + 1,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }

        return MediatorResult.Success(endOfPaginationReached = endOfPagination)
    }

    private suspend fun handleErrorResponse(
        response: retrofit2.Response<*>
    ): MediatorResult {
        val errorBody = response.errorBody()?.string()
        if (ErrorHandling.isPaginationDone(errorBody)) {
            remoteKeyDao.insertOrReplace(
                BlogRemoteKey(
                    cachedQueryKey,
                    nextPage = null,
                    lastUpdated = System.currentTimeMillis()
                )
            )
            return MediatorResult.Success(endOfPaginationReached = true)
        }
        return MediatorResult.Error(Exception(response.message()))
    }
}
