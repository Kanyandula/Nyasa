package com.kanyandula.nyasa.repository.main

import android.net.Uri
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.toBlogPost
import com.kanyandula.nyasa.domain.repository.BlogRepository
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.models.LikeResult
import com.kanyandula.nyasa.persistance.AppDatabase
import com.kanyandula.nyasa.persistance.getOrderedBlogPagingSource
import com.kanyandula.nyasa.repository.networkApiFlow
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.Constants.PAGINATION_PAGE_SIZE
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_UNKNOWN
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.SuccessHandling.RESPONSE_HAS_PERMISSION_TO_EDIT
import com.kanyandula.nyasa.util.SuccessHandling.RESPONSE_NO_PERMISSION_TO_EDIT
import com.kanyandula.nyasa.util.SuccessHandling.SUCCESS_BLOG_DELETED
import com.kanyandula.nyasa.util.toMultipartImage
import com.kanyandula.nyasa.util.toPlainTextBody
import kotlinx.coroutines.flow.Flow
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
    ): Flow<Resource<Boolean>> = networkApiFlow(
        connectivityObserver = connectivityObserver,
        apiCall = { nyasaBlogApiMainService.isAuthorOfBlogPost(slug) },
        onSuccess = { body ->
            when (body.response) {
                RESPONSE_NO_PERMISSION_TO_EDIT -> Resource.Success(false)
                RESPONSE_HAS_PERMISSION_TO_EDIT -> Resource.Success(true)
                else -> Resource.Error(ERROR_UNKNOWN)
            }
        }
    )

    override fun deleteBlogPost(
        blogPost: BlogPost
    ): Flow<Resource<String>> = networkApiFlow(
        connectivityObserver = connectivityObserver,
        apiCall = { nyasaBlogApiMainService.deleteBlogPost(blogPost.slug) },
        onSuccess = { body ->
            if (body.response == SUCCESS_BLOG_DELETED) {
                blogPostDao.deleteBlogPost(blogPost)
                Resource.Success(SUCCESS_BLOG_DELETED)
            } else {
                Resource.Error(ERROR_UNKNOWN)
            }
        }
    )

    override fun updateBlogPost(
        slug: String,
        title: String,
        body: String,
        image: Uri?,
        category: String?,
        tags: List<String>?
    ): Flow<Resource<BlogPost>> = networkApiFlow(
        connectivityObserver = connectivityObserver,
        apiCall = {
            val titleBody = title.toPlainTextBody()
            val bodyBody = body.toPlainTextBody()
            val imagePart = image?.toMultipartImage()
            val categoryBody = category?.toPlainTextBody()
            val tagsBody = tags?.takeIf { it.isNotEmpty() }?.joinToString(",")?.toPlainTextBody()
            nyasaBlogApiMainService.updateBlog(
                slug,
                titleBody,
                bodyBody,
                imagePart,
                categoryBody,
                tagsBody
            )
        },
        onSuccess = { body ->
            val updatedBlogPost = body.toBlogPost()
            blogPostDao.insert(updatedBlogPost)
            Resource.Success(updatedBlogPost)
        }
    )

    override suspend fun getBlogPostBySlug(slug: String): BlogPost? =
        blogPostDao.getBlogPostBySlug(slug)

    override fun likeBlogPost(
        slug: String
    ): Flow<Resource<LikeResult>> = networkApiFlow(
        connectivityObserver = connectivityObserver,
        apiCall = { nyasaBlogApiMainService.likeBlogPost(slug) },
        onSuccess = { body ->
            Resource.Success(LikeResult(liked = body.liked, likeCount = body.like_count))
        }
    )

    override fun bookmarkBlogPost(
        slug: String
    ): Flow<Resource<Boolean>> = networkApiFlow(
        connectivityObserver = connectivityObserver,
        apiCall = { nyasaBlogApiMainService.bookmarkBlogPost(slug) },
        onSuccess = { body -> Resource.Success(body.bookmarked) }
    )

    override fun getBookmarks(): Flow<Resource<List<BlogPost>>> = networkApiFlow(
        connectivityObserver = connectivityObserver,
        apiCall = { nyasaBlogApiMainService.getBookmarks() },
        onSuccess = { body -> Resource.Success(body.results.map { it.toBlogPost() }) }
    )
}
