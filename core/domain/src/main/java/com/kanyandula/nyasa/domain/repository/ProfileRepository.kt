package com.kanyandula.nyasa.domain.repository

import com.kanyandula.nyasa.models.ProfileUpdateRequest
import com.kanyandula.nyasa.models.UserProfile
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfile(username: String): Flow<Resource<UserProfile>>
    fun updateProfile(request: ProfileUpdateRequest): Flow<Resource<UserProfile>>
}
