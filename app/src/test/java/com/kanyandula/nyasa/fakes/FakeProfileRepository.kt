package com.kanyandula.nyasa.fakes

import com.kanyandula.nyasa.domain.repository.ProfileRepository
import com.kanyandula.nyasa.models.ProfileUpdateRequest
import com.kanyandula.nyasa.models.UserProfile
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.Flow

class FakeProfileRepository : ProfileRepository {

    var profileResult: Resource<UserProfile> = Resource.Success(
        UserProfile(
            username = "testuser",
            bio = null,
            location = null,
            website = null,
            twitter = null,
            facebook = null,
            instagram = null,
            linkedin = null,
            profileImage = null
        )
    )

    override fun getProfile(username: String): Flow<Resource<UserProfile>> =
        fakeResourceFlow { profileResult }

    var updateProfileResult: Resource<UserProfile> = Resource.Success(
        UserProfile(
            username = "testuser",
            bio = "Updated bio",
            location = null,
            website = null,
            twitter = null,
            facebook = null,
            instagram = null,
            linkedin = null,
            profileImage = null
        )
    )

    override fun updateProfile(
        request: ProfileUpdateRequest
    ): Flow<Resource<UserProfile>> = fakeResourceFlow { updateProfileResult }
}
