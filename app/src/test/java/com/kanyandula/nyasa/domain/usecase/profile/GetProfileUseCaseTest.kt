package com.kanyandula.nyasa.domain.usecase.profile

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.fakes.FakeProfileRepository
import com.kanyandula.nyasa.models.UserProfile
import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetProfileUseCaseTest {

    private lateinit var fakeRepository: FakeProfileRepository
    private lateinit var useCase: GetProfileUseCase

    @Before
    fun setup() {
        fakeRepository = FakeProfileRepository()
        useCase = GetProfileUseCase(fakeRepository)
    }

    @Test
    fun `invoke emits loading then success with profile`() = runTest {
        val profile = UserProfile(
            username = "author1",
            bio = "Writer from Lilongwe",
            location = "Lilongwe",
            website = null,
            twitter = null,
            facebook = null,
            instagram = null,
            linkedin = null,
            profileImage = null
        )
        fakeRepository.profileResult = Resource.Success(profile)

        useCase("author1").test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val success = awaitItem() as Resource.Success
            assertThat(success.data.username).isEqualTo("author1")
            assertThat(success.data.bio).isEqualTo("Writer from Lilongwe")
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits loading then error on failure`() = runTest {
        fakeRepository.profileResult = Resource.Error(AppError.Unknown(RuntimeException("Not found")))

        useCase("unknown").test {
            assertThat(awaitItem()).isInstanceOf(Resource.Loading::class.java)
            val error = awaitItem() as Resource.Error
            awaitComplete()
        }
    }
}
