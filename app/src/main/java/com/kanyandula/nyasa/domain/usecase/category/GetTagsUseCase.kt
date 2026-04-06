package com.kanyandula.nyasa.domain.usecase.category

import com.kanyandula.nyasa.domain.repository.CategoryRepository
import com.kanyandula.nyasa.models.Tag
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTagsUseCase
@Inject
constructor(private val categoryRepository: CategoryRepository) {
    operator fun invoke(): Flow<Resource<List<Tag>>> =
        categoryRepository.getTags()
}
