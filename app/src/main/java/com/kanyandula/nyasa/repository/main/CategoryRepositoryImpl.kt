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
import javax.inject.Inject

class CategoryRepositoryImpl
@Inject
constructor(
    private val apiService: NyasaBlogApiMainService,
    private val connectivityObserver: ConnectivityObserver
) : CategoryRepository {

    override fun getCategories(): Flow<Resource<List<Category>>> = networkApiFlow(
        connectivityObserver = connectivityObserver,
        apiCall = { apiService.getCategories() },
        onSuccess = { body -> Resource.Success(body.map { it.toCategory() }) }
    )

    override fun getTags(): Flow<Resource<List<Tag>>> = networkApiFlow(
        connectivityObserver = connectivityObserver,
        apiCall = { apiService.getTags() },
        onSuccess = { body -> Resource.Success(body.map { it.toTag() }) }
    )
}
