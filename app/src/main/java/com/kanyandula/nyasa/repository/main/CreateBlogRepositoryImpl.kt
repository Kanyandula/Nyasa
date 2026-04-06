@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.repository.main

import android.net.Uri
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.toBlogPost
import com.kanyandula.nyasa.domain.repository.CreateBlogRepository
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.repository.networkApiFlow
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.Constants.RESPONSE_MUST_HAVE_NYASABLOG_UER
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.toMultipartImage
import com.kanyandula.nyasa.util.toPlainTextBody
import kotlinx.coroutines.flow.Flow
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
    ): Flow<Resource<String>> = networkApiFlow(
        connectivityObserver = connectivityObserver,
        apiCall = {
            val titleBody = title.toPlainTextBody()
            val bodyBody = body.toPlainTextBody()
            val imagePart = image?.toMultipartImage()
            blogApiMainService.createBlog(titleBody, bodyBody, imagePart)
        },
        onSuccess = { responseBody ->
            if (responseBody.response != RESPONSE_MUST_HAVE_NYASABLOG_UER) {
                blogPostDao.insert(responseBody.toBlogPost())
            }
            Resource.Success(responseBody.response)
        }
    )
}
