package com.kanyandula.nyasa.domain.repository

import com.kanyandula.nyasa.models.Category
import com.kanyandula.nyasa.models.Tag
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(): Flow<Resource<List<Category>>>
    fun getTags(): Flow<Resource<List<Tag>>>
}
