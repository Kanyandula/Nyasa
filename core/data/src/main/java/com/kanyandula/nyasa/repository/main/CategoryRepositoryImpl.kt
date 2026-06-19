package com.kanyandula.nyasa.repository.main

import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.toCategory
import com.kanyandula.nyasa.api.main.responses.toTag
import com.kanyandula.nyasa.domain.repository.CategoryRepository
import com.kanyandula.nyasa.models.Category
import com.kanyandula.nyasa.models.Tag
import com.kanyandula.nyasa.repository.networkApiFlow
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CategoryRepositoryImpl
@Inject
constructor(
    private val apiService: NyasaBlogApiMainService,
    private val connectivityObserver: ConnectivityObserver
) : CategoryRepository {

    // Session cache: categories change rarely, so once fetched they are served from memory.
    // This repository is a @Singleton, so the cache is shared across every ViewModel that loads
    // categories (feed, create, edit). Errors are never cached, so an offline first load retries.
    @Volatile
    private var cachedCategories: List<Category>? = null

    override fun getCategories(): Flow<Resource<List<Category>>> = flow {
        cachedCategories?.let {
            emit(Resource.Success(it))
            return@flow
        }
        networkApiFlow(
            connectivityObserver = connectivityObserver,
            apiCall = { apiService.getCategories() },
            onSuccess = { body -> Resource.Success(body.map { it.toCategory() }) }
        ).collect { resource ->
            if (resource is Resource.Success) cachedCategories = resource.data
            emit(resource)
        }
    }

    override fun getTags(): Flow<Resource<List<Tag>>> = networkApiFlow(
        connectivityObserver = connectivityObserver,
        apiCall = { apiService.getTags() },
        onSuccess = { body -> Resource.Success(body.map { it.toTag() }) }
    )
}
