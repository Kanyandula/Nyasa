package com.kanyandula.nyasa.repository.main

import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.toComment
import com.kanyandula.nyasa.api.main.responses.toEntity
import com.kanyandula.nyasa.domain.repository.CommentRepository
import com.kanyandula.nyasa.models.Comment
import com.kanyandula.nyasa.persistance.BlogPostDao
import com.kanyandula.nyasa.persistance.CommentDao
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CommentRepositoryImpl
@Inject
constructor(
    private val apiService: NyasaBlogApiMainService,
    private val commentDao: CommentDao,
    private val blogPostDao: BlogPostDao,
    private val connectivityObserver: ConnectivityObserver
) : CommentRepository {

    override fun getComments(slug: String): Flow<Resource<List<Comment>>> = flow {
        emit(Resource.Loading())

        val cached = commentDao.getBySlug(slug).first()
        if (cached.isNotEmpty()) {
            emit(Resource.Success(cached.map { it.toComment() }))
        }

        if (!connectivityObserver.isConnected.value) {
            if (cached.isEmpty()) emit(Resource.Error(AppError.Offline))
            return@flow
        }

        when (val result = safeApiCall { apiService.getComments(slug) }) {
            is Resource.Success -> {
                val entities = result.data.map { it.toEntity(slug) }
                commentDao.clearBySlug(slug)
                commentDao.insertAll(entities)
                emit(Resource.Success(entities.map { it.toComment() }))
            }
            is Resource.Error -> {
                if (cached.isEmpty()) emit(result)
            }
            is Resource.Loading -> Unit
        }
    }.flowOn(Dispatchers.IO)

    override fun getCommentsFlow(slug: String): Flow<List<Comment>> =
        commentDao.getBySlug(slug).map { entities ->
            entities.map { it.toComment() }
        }

    override fun createComment(slug: String, body: String): Flow<Resource<Comment>> = flow {
        emit(Resource.Loading())
        if (!connectivityObserver.isConnected.value) {
            emit(Resource.Error(AppError.Offline))
            return@flow
        }
        when (val result = safeApiCall { apiService.createComment(slug, body) }) {
            is Resource.Success -> {
                val entity = result.data.toEntity(slug)
                commentDao.insert(entity)
                blogPostDao.updateCommentCount(slug, 1)
                emit(Resource.Success(entity.toComment()))
            }
            is Resource.Error -> emit(result)
            is Resource.Loading -> Unit
        }
    }.flowOn(Dispatchers.IO)

    override fun deleteComment(pk: Int, slug: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        if (!connectivityObserver.isConnected.value) {
            emit(Resource.Error(AppError.Offline))
            return@flow
        }
        when (val result = safeApiCall { apiService.deleteComment(pk) }) {
            is Resource.Success -> {
                commentDao.deleteByPk(pk)
                blogPostDao.updateCommentCount(slug, -1)
                emit(Resource.Success(result.data.response))
            }
            is Resource.Error -> emit(result)
            is Resource.Loading -> Unit
        }
    }.flowOn(Dispatchers.IO)
}
