package com.kanyandula.nyasa.fakes

import com.kanyandula.nyasa.domain.repository.CategoryRepository
import com.kanyandula.nyasa.models.Category
import com.kanyandula.nyasa.models.Tag
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow

class FakeCategoryRepository : CategoryRepository {

    var categoriesResult: Resource<List<Category>> = Resource.Success(emptyList())

    override fun getCategories(): Flow<Resource<List<Category>>> =
        fakeResourceFlow { categoriesResult }

    var tagsResult: Resource<List<Tag>> = Resource.Success(emptyList())

    override fun getTags(): Flow<Resource<List<Tag>>> =
        fakeResourceFlow { tagsResult }
}
