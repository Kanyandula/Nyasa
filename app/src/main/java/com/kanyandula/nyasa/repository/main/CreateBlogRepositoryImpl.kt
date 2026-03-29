@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.repository.main

import android.net.Uri
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.toBlogPost
import com.kanyandula.nyasa.domain.repository.CreateBlogRepository
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.repository.apiErrorMessage
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.ApiSuccessResponse
import com.kanyandula.nyasa.util.Constants.NETWORK_TIMEOUT
import com.kanyandula.nyasa.util.Constants.RESPONSE_MUST_HAVE_NYASABLOG_UER
import com.kanyandula.nyasa.util.ErrorHandling.UNABLE_TODO_OPERATION_WO_INTERNET
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.safeApiCall
import com.kanyandula.nyasa.util.toMultipartImage
import com.kanyandula.nyasa.util.toPlainTextBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class CreateBlogRepositoryImpl
@Inject
constructor(
    private val blogApiMainService: NyasaBlogApiMainService,
    private val blogPostDao: BlogPostDao,
    private val connectivityObserver: ConnectivityObserver
) : CreateBlogRepository {

    override fun createNewBlogPost(
        title: String,
        body: String,
        image: Uri?
    ): Flow<Resource<String>> = flow {
        emit(Resource.Loading())

        if (!connectivityObserver.isConnected.value) {
            emit(Resource.Error(UNABLE_TODO_OPERATION_WO_INTERNET))
            return@flow
        }

        val titleBody = title.toPlainTextBody()
        val bodyBody = body.toPlainTextBody()
        val imagePart = image?.toMultipartImage()

        val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
            safeApiCall { blogApiMainService.createBlog(titleBody, bodyBody, imagePart) }
        }

        when (response) {
            is ApiSuccessResponse -> {
                if (response.body.response != RESPONSE_MUST_HAVE_NYASABLOG_UER) {
                    blogPostDao.insert(response.body.toBlogPost())
                }
                emit(Resource.Success(response.body.response))
            }
            else -> emit(Resource.Error(apiErrorMessage(response)))
        }
    }.flowOn(Dispatchers.IO)
}
