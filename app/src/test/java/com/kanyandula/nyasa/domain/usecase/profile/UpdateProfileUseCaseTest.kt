package com.kanyandula.nyasa.domain.usecase.profile

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.fakes.FakeProfileRepository
import com.kanyandula.nyasa.models.ProfileUpdateRequest
import com.kanyandula.nyasa.models.UserProfile
import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateProfileUseCaseTest {

    private lateinit var fakeRepository: FakeProfileRepository
    private lateinit var useCase: UpdateProfileUseCase

    @Before
    fun setup() {
        fakeRepository = FakeProfileRepository()
        useCase = UpdateProfileUseCase(fakeRepository)
    }

    @Test
    fun `invoke emits loading then success with updated profile`() = runTest {
        val updatedProfile = UserProfile(
            username = "testuser",
            bio = "New bio",
            location = "Blantyre",
            website = null,
            twitter = null,
            facebook = null,
            instagram = null,
            linkedin = null,
            profileImage = null
        )
        fakeRepository.updateProfileResult = Resource.Success(updatedProfile)

        val request = ProfileUpdateRequest(bio = "New bio", location = "Blantyre")
        useCase(request).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val success = awaitItem() as Resource.Success
            assertThat(success.data.bio).isEqualTo("New bio")
            assertThat(success.data.location).isEqualTo("Blantyre")
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits loading then error on failure`() = runTest {
        fakeRepository.updateProfileResult = Resource.Error(AppError.Unknown(RuntimeException("Unauthorized")))

        val request = ProfileUpdateRequest(bio = "bio")
        useCase(request).test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val error = awaitItem() as Resource.Error
            awaitComplete()
        }
    }
}
