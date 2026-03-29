package com.kanyandula.nyasa.repository.main

import android.net.Uri
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.toBlogPost
import com.kanyandula.nyasa.domain.repository.BlogRepository
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.persistance.AppDatabase
import com.kanyandula.nyasa.persistance.getOrderedBlogPagingSource
import com.kanyandula.nyasa.repository.apiErrorMessage
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.ApiSuccessResponse
import com.kanyandula.nyasa.util.Constants.NETWORK_TIMEOUT
import com.kanyandula.nyasa.util.Constants.PAGINATION_PAGE_SIZE
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_UNKNOWN
import com.kanyandula.nyasa.util.ErrorHandling.UNABLE_TODO_OPERATION_WO_INTERNET
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.SuccessHandling.RESPONSE_HAS_PERMISSION_TO_EDIT
import com.kanyandula.nyasa.util.SuccessHandling.RESPONSE_NO_PERMISSION_TO_EDIT
import com.kanyandula.nyasa.util.SuccessHandling.SUCCESS_BLOG_DELETED
import com.kanyandula.nyasa.util.safeApiCall
import com.kanyandula.nyasa.util.toMultipartImage
import com.kanyandula.nyasa.util.toPlainTextBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class BlogRepositoryImpl
@Inject
constructor(
    private val nyasaBlogApiMainService: NyasaBlogApiMainService,
    private val database: AppDatabase,
    private val connectivityObserver: ConnectivityObserver
) : BlogRepository {

    private val blogPostDao = database.getBlogPostDao()

    @OptIn(ExperimentalPagingApi::class)
    override fun getBlogPagingData(
        query: String,
        filterAndOrder: String
    ): Flow<PagingData<BlogPost>> = Pager(
        config = PagingConfig(
            pageSize = PAGINATION_PAGE_SIZE,
            enablePlaceholders = false
        ),
        remoteMediator = BlogRemoteMediator(
            query = query,
            filterAndOrder = filterAndOrder,
            apiService = nyasaBlogApiMainService,
            database = database,
            connectivityObserver = connectivityObserver
        ),
        pagingSourceFactory = {
            blogPostDao.getOrderedBlogPagingSource(query, filterAndOrder)
        }
    ).flow

    override fun isAuthorOfBlogPost(
        slug: String
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        if (!connectivityObserver.isConnected.value) {
            emit(Resource.Error(UNABLE_TODO_OPERATION_WO_INTERNET))
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
                        emit(Resource.Success(false))
                    }
                    response.body.response == RESPONSE_HAS_PERMISSION_TO_EDIT -> {
                        emit(Resource.Success(true))
                    }
                    else -> emit(Resource.Error(ERROR_UNKNOWN))
                }
            }
            else -> emit(Resource.Error(apiErrorMessage(response)))
        }
    }.flowOn(Dispatchers.IO)

    override fun deleteBlogPost(
        blogPost: BlogPost
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading())

        if (!connectivityObserver.isConnected.value) {
            emit(Resource.Error(UNABLE_TODO_OPERATION_WO_INTERNET))
            return@flow
        }

        val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
            safeApiCall { nyasaBlogApiMainService.deleteBlogPost(blogPost.slug) }
        }

        when (response) {
            is ApiSuccessResponse -> {
                if (response.body.response == SUCCESS_BLOG_DELETED) {
                    blogPostDao.deleteBlogPost(blogPost)
                    emit(Resource.Success(SUCCESS_BLOG_DELETED))
                } else {
                    emit(Resource.Error(ERROR_UNKNOWN))
                }
            }
            else -> emit(Resource.Error(apiErrorMessage(response)))
        }
    }.flowOn(Dispatchers.IO)

    override fun updateBlogPost(
        slug: String,
        title: String,
        body: String,
        image: Uri?
    ): Flow<Resource<BlogPost>> = flow {
        emit(Resource.Loading())

        if (!connectivityObserver.isConnected.value) {
            emit(Resource.Error(UNABLE_TODO_OPERATION_WO_INTERNET))
            return@flow
        }

        val titleBody = title.toPlainTextBody()
        val bodyBody = body.toPlainTextBody()
        val imagePart = image?.toMultipartImage()

        val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
            safeApiCall { nyasaBlogApiMainService.updateBlog(slug, titleBody, bodyBody, imagePart) }
        }

        when (response) {
            is ApiSuccessResponse -> {
                val updatedBlogPost = response.body.toBlogPost()
                blogPostDao.updateBlogPost(
                    updatedBlogPost.pk,
                    updatedBlogPost.title,
                    updatedBlogPost.body,
                    updatedBlogPost.image
                )
                emit(Resource.Success(updatedBlogPost))
            }
            else -> emit(Resource.Error(apiErrorMessage(response)))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getBlogPostBySlug(slug: String): BlogPost? =
        blogPostDao.getBlogPostBySlug(slug)

    companion object {
        private const val TAG = "AppDebug"
    }
}
