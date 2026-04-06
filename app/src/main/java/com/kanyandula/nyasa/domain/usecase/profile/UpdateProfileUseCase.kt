package com.kanyandula.nyasa.domain.usecase.profile

import com.kanyandula.nyasa.domain.repository.ProfileRepository
import com.kanyandula.nyasa.models.ProfileUpdateRequest
import com.kanyandula.nyasa.models.UserProfile
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateProfileUseCase
@Inject
constructor(private val profileRepository: ProfileRepository) {
    operator fun invoke(request: ProfileUpdateRequest): Flow<Resource<UserProfile>> =
        profileRepository.updateProfile(request)
}
