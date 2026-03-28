package com.kanyandula.nyasa.repository.main

import android.net.Uri
import android.util.Log
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.BlogListSearchResponse
import com.kanyandula.nyasa.api.main.responses.toBlogPost
import com.kanyandula.nyasa.domain.model.BlogSearchResult
import com.kanyandula.nyasa.domain.repository.BlogRepository
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.persistance.returnOrderedBlogQuery
import com.kanyandula.nyasa.repository.apiErrorMessage
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.util.ApiSuccessResponse
import com.kanyandula.nyasa.util.Constants.NETWORK_TIMEOUT
import com.kanyandula.nyasa.util.Constants.PAGINATION_PAGE_SIZE
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_UNKNOWN
import com.kanyandula.nyasa.util.ErrorHandling.UNABLE_TODO_OPERATION_WO_INTERNET
import com.kanyandula.nyasa.util.GenericApiResponse
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.SuccessHandling.RESPONSE_HAS_PERMISSION_TO_EDIT
import com.kanyandula.nyasa.util.SuccessHandling.RESPONSE_NO_PERMISSION_TO_EDIT
import com.kanyandula.nyasa.util.SuccessHandling.SUCCESS_BLOG_DELETED
import com.kanyandula.nyasa.util.safeApiCall
import com.kanyandula.nyasa.util.toMultipartImage
import com.kanyandula.nyasa.util.toPlainTextBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class BlogRepositoryImpl
@Inject
constructor(
    private val nyasaBlogApiMainService: NyasaBlogApiMainService,
    private val blogPostDao: BlogPostDao,
    private val sessionManager: SessionManager
) : BlogRepository {

    override fun searchBlogPosts(
        query: String,
        filterAndOrder: String,
        page: Int
    ): Flow<Resource<BlogSearchResult>> = flow {
        emit(Resource.Loading())

        val cachedPosts = blogPostDao.returnOrderedBlogQuery(query, filterAndOrder, page)
        emit(Resource.Loading(BlogSearchResult(cachedPosts, false)))

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
            val isExhausted = page * PAGINATION_PAGE_SIZE > cachedPosts.size
            emit(Resource.Success(BlogSearchResult(cachedPosts, isExhausted)))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun FlowCollector<Resource<BlogSearchResult>>.emitSearchResponse(
        response: GenericApiResponse<BlogListSearchResponse>?,
        query: String,
        filterAndOrder: String,
        page: Int
    ) {
        when (response) {
            is ApiSuccessResponse -> {
                val blogPostList = response.body.results.map { it.toBlogPost() }
                blogPostDao.insertAll(blogPostList)
                val updatedPosts = blogPostDao.returnOrderedBlogQuery(query, filterAndOrder, page)
                val isExhausted = page * PAGINATION_PAGE_SIZE > updatedPosts.size
                emit(Resource.Success(BlogSearchResult(updatedPosts, isExhausted)))
            }
            else -> emit(Resource.Error(apiErrorMessage(response)))
        }
    }

    override fun isAuthorOfBlogPost(
        slug: String
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())

        if (!sessionManager.isConnectedToTheInternet()) {
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

        if (!sessionManager.isConnectedToTheInternet()) {
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

        if (!sessionManager.isConnectedToTheInternet()) {
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
