package com.kanyandula.nyasa.domain.usecase.profile

import com.kanyandula.nyasa.domain.repository.ProfileRepository
import com.kanyandula.nyasa.models.UserProfile
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfileUseCase
@Inject
constructor(private val profileRepository: ProfileRepository) {
    operator fun invoke(username: String): Flow<Resource<UserProfile>> =
        profileRepository.getProfile(username)
}
