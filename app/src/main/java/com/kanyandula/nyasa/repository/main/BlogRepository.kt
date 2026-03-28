package com.kanyandula.nyasa.repository.main

import android.util.Log
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.BlogListSearchResponse
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.persistance.returnOrderedBlogQuery
import com.kanyandula.nyasa.repository.emitApiError
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.DataState
import com.kanyandula.nyasa.ui.Response
import com.kanyandula.nyasa.ui.ResponseType
import com.kanyandula.nyasa.ui.main.blog.state.BlogViewState
import com.kanyandula.nyasa.ui.main.blog.state.BlogViewState.BlogFields
import com.kanyandula.nyasa.ui.main.blog.state.BlogViewState.ViewBlogFields
import com.kanyandula.nyasa.util.ApiSuccessResponse
import com.kanyandula.nyasa.util.Constants.NETWORK_TIMEOUT
import com.kanyandula.nyasa.util.Constants.PAGINATION_PAGE_SIZE
import com.kanyandula.nyasa.util.DateUtils
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_UNKNOWN
import com.kanyandula.nyasa.util.ErrorHandling.UNABLE_TODO_OPERATION_WO_INTERNET
import com.kanyandula.nyasa.util.GenericApiResponse
import com.kanyandula.nyasa.util.SuccessHandling.RESPONSE_HAS_PERMISSION_TO_EDIT
import com.kanyandula.nyasa.util.SuccessHandling.RESPONSE_NO_PERMISSION_TO_EDIT
import com.kanyandula.nyasa.util.SuccessHandling.SUCCESS_BLOG_DELETED
import com.kanyandula.nyasa.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class BlogRepository
@Inject
constructor(
    private val nyasaBlogApiMainService: NyasaBlogApiMainService,
    private val blogPostDao: BlogPostDao,
    private val sessionManager: SessionManager
) {

    fun searchBlogPosts(
        query: String,
        filterAndOrder: String,
        page: Int
    ): Flow<DataState<BlogViewState>> = flow {
        emit(DataState.loading<BlogViewState>(isLoading = true))

        val cachedPosts = blogPostDao.returnOrderedBlogQuery(query, filterAndOrder, page)
        emit(
            DataState.loading<BlogViewState>(
                isLoading = true,
                cachedData = BlogViewState(BlogFields(blogList = cachedPosts))
            )
        )

        if (sessionManager.isConnectedToTheInternet()) {
            val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
                safeApiCall {
                    nyasaBlogApiMainService.searchListBlogPosts(
                        query = query,
                        ordering = filterAndOrder,
                        page = page
                    )
                }
            }
            emitSearchResponse(response, query, filterAndOrder, page)
        } else {
            emitBlogListResult(cachedPosts, page)
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun FlowCollector<DataState<BlogViewState>>.emitSearchResponse(
        response: GenericApiResponse<BlogListSearchResponse>?,
        query: String,
        filterAndOrder: String,
        page: Int
    ) {
        when (response) {
            is ApiSuccessResponse -> {
                val blogPostList = response.body.results.map { r ->
                    BlogPost(
                        pk = r.pk,
                        title = r.title,
                        slug = r.slug,
                        body = r.body,
                        image = r.image,
                        date_updated = DateUtils.convertServerStringDateToLong(r.date_updated),
                        username = r.username
                    )
                }
                blogPostDao.insertAll(blogPostList)
                val updatedPosts = blogPostDao.returnOrderedBlogQuery(query, filterAndOrder, page)
                emitBlogListResult(updatedPosts, page)
            }
            else -> this.emitApiError<BlogViewState>(response)
        }
    }

    private suspend fun FlowCollector<DataState<BlogViewState>>.emitBlogListResult(
        posts: List<BlogPost>,
        page: Int
    ) {
        val isExhausted = page * PAGINATION_PAGE_SIZE > posts.size
        emit(
            DataState.data(
                data = BlogViewState(
                    BlogFields(
                        blogList = posts,
                        isQueryInProgress = false,
                        isQueryExhausted = isExhausted
                    )
                )
            )
        )
    }

    fun isAuthorOfBlogPost(
        slug: String
    ): Flow<DataState<BlogViewState>> = flow {
        emit(DataState.loading<BlogViewState>(isLoading = true))

        if (!sessionManager.isConnectedToTheInternet()) {
            emit(DataState.apiError<BlogViewState>(UNABLE_TODO_OPERATION_WO_INTERNET))
            return@flow
        }

        val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
            safeApiCall { nyasaBlogApiMainService.isAuthorOfBlogPost(slug) }
        }

        when (response) {
            is ApiSuccessResponse -> {
                Log.d(TAG, "handleApiSuccessResponse: ${response.body.response}")
                when {
                    response.body.response == RESPONSE_NO_PERMISSION_TO_EDIT -> {
                        emit(
                            DataState.data(
                                data = BlogViewState(
                                    viewBlogFields = ViewBlogFields(isAuthorOfBlogPost = false)
                                )
                            )
                        )
                    }
                    response.body.response == RESPONSE_HAS_PERMISSION_TO_EDIT -> {
                        emit(
                            DataState.data(
                                data = BlogViewState(
                                    viewBlogFields = ViewBlogFields(isAuthorOfBlogPost = true)
                                )
                            )
                        )
                    }
                    else -> emit(DataState.apiError<BlogViewState>(ERROR_UNKNOWN, shouldUseDialog = true))
                }
            }
            else -> this.emitApiError<BlogViewState>(response)
        }
    }.flowOn(Dispatchers.IO)

    fun deleteBlogPost(
        blogPost: BlogPost
    ): Flow<DataState<BlogViewState>> = flow {
        emit(DataState.loading<BlogViewState>(isLoading = true))

        if (!sessionManager.isConnectedToTheInternet()) {
            emit(DataState.apiError<BlogViewState>(UNABLE_TODO_OPERATION_WO_INTERNET))
            return@flow
        }

        val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
            safeApiCall { nyasaBlogApiMainService.deleteBlogPost(blogPost.slug) }
        }

        when (response) {
            is ApiSuccessResponse -> {
                if (response.body.response == SUCCESS_BLOG_DELETED) {
                    blogPostDao.deleteBlogPost(blogPost)
                    emit(
                        DataState.data<BlogViewState>(null, Response(SUCCESS_BLOG_DELETED, ResponseType.Toast()))
                    )
                } else {
                    emit(DataState.error<BlogViewState>(Response(ERROR_UNKNOWN, ResponseType.Dialog())))
                }
            }
            else -> this.emitApiError<BlogViewState>(response)
        }
    }.flowOn(Dispatchers.IO)

    fun updateBlogPost(
        slug: String,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ): Flow<DataState<BlogViewState>> = flow {
        emit(DataState.loading<BlogViewState>(isLoading = true))

        if (!sessionManager.isConnectedToTheInternet()) {
            emit(DataState.apiError<BlogViewState>(UNABLE_TODO_OPERATION_WO_INTERNET))
            return@flow
        }

        val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
            safeApiCall { nyasaBlogApiMainService.updateBlog(slug, title, body, image) }
        }

        when (response) {
            is ApiSuccessResponse -> {
                val updatedBlogPost = BlogPost(
                    response.body.pk,
                    response.body.title,
                    response.body.slug,
                    response.body.body,
                    response.body.image,
                    DateUtils.convertServerStringDateToLong(response.body.date_updated),
                    response.body.username
                )
                blogPostDao.updateBlogPost(
                    updatedBlogPost.pk,
                    updatedBlogPost.title,
                    updatedBlogPost.body,
                    updatedBlogPost.image
                )
                emit(
                    DataState.data(
                        BlogViewState(viewBlogFields = ViewBlogFields(blogPost = updatedBlogPost)),
                        Response(response.body.response, ResponseType.Toast())
                    )
                )
            }
            else -> this.emitApiError<BlogViewState>(response)
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        private const val TAG = "AppDebug"
    }
}
