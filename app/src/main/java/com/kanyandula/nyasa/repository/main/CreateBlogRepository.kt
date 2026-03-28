@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.repository.main

import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.repository.emitApiError
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.DataState
import com.kanyandula.nyasa.ui.Response
import com.kanyandula.nyasa.ui.ResponseType
import com.kanyandula.nyasa.ui.main.create_blog.state.CreateBlogViewState
import com.kanyandula.nyasa.util.ApiSuccessResponse
import com.kanyandula.nyasa.util.Constants.NETWORK_TIMEOUT
import com.kanyandula.nyasa.util.Constants.RESPONSE_MUST_HAVE_NYASABLOG_UER
import com.kanyandula.nyasa.util.DateUtils
import com.kanyandula.nyasa.util.ErrorHandling.UNABLE_TODO_OPERATION_WO_INTERNET
import com.kanyandula.nyasa.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class CreateBlogRepository
@Inject
constructor(
    private val blogApiMainService: NyasaBlogApiMainService,
    private val blogPostDao: BlogPostDao,
    private val sessionManager: SessionManager
) {

    fun createNewBlogPost(
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ): Flow<DataState<CreateBlogViewState>> = flow {
        emit(DataState.loading<CreateBlogViewState>(isLoading = true))

        if (!sessionManager.isConnectedToTheInternet()) {
            emit(DataState.apiError<CreateBlogViewState>(UNABLE_TODO_OPERATION_WO_INTERNET))
            return@flow
        }

        val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
            safeApiCall { blogApiMainService.createBlog(title, body, image) }
        }

        when (response) {
            is ApiSuccessResponse -> {
                if (response.body.response != RESPONSE_MUST_HAVE_NYASABLOG_UER) {
                    val createdBlogPost = BlogPost(
                        response.body.pk,
                        response.body.title,
                        response.body.slug,
                        response.body.body,
                        response.body.image,
                        DateUtils.convertServerStringDateToLong(response.body.date_updated),
                        response.body.username
                    )
                    blogPostDao.insert(createdBlogPost)
                }
                emit(
                    DataState.data<CreateBlogViewState>(
                        null,
                        Response(response.body.response, ResponseType.Dialog())
                    )
                )
            }
            else -> this.emitApiError<CreateBlogViewState>(response)
        }
    }.flowOn(Dispatchers.IO)
}
