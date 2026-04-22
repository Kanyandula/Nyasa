package com.kanyandula.nyasa.repository.main

import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.api.main.responses.toUserProfile
import com.kanyandula.nyasa.domain.repository.ProfileRepository
import com.kanyandula.nyasa.models.ProfileUpdateRequest
import com.kanyandula.nyasa.models.UserProfile
import com.kanyandula.nyasa.repository.networkApiFlow
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProfileRepositoryImpl
@Inject
constructor(
    private val apiService: NyasaBlogApiMainService,
    private val connectivityObserver: ConnectivityObserver
) : ProfileRepository {

    override fun getProfile(
        username: String
    ): Flow<Resource<UserProfile>> = networkApiFlow(
        connectivityObserver = connectivityObserver,
        apiCall = { apiService.getProfile(username) },
        onSuccess = { body -> Resource.Success(body.toUserProfile()) }
    )

    override fun updateProfile(
        request: ProfileUpdateRequest
    ): Flow<Resource<UserProfile>> = networkApiFlow(
        connectivityObserver = connectivityObserver,
        apiCall = {
            apiService.updateProfile(
                bio = request.bio,
                location = request.location,
                website = request.website,
                twitter = request.twitter,
                facebook = request.facebook,
                instagram = request.instagram,
                linkedin = request.linkedin
            )
        },
        onSuccess = { body -> Resource.Success(body.toUserProfile()) }
    )
}
