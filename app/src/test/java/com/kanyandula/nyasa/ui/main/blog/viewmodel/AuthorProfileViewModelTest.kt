package com.kanyandula.nyasa.ui.main.blog.viewmodel

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.domain.usecase.profile.GetProfileUseCase
import com.kanyandula.nyasa.fakes.FakeProfileRepository
import com.kanyandula.nyasa.models.UserProfile
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.util.MainDispatcherRule
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthorProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeProfileRepository
    private lateinit var viewModel: AuthorProfileViewModel

    @Before
    fun setup() {
        fakeRepository = FakeProfileRepository()
        viewModel = AuthorProfileViewModel(
            getProfileUseCase = GetProfileUseCase(fakeRepository)
        )
    }

    @Test
    fun `loadProfile success updates state with profile`() = runTest {
        val profile = UserProfile(
            username = "author1",
            bio = "A writer",
            location = "Lilongwe",
            website = null,
            twitter = null,
            facebook = null,
            instagram = null,
            linkedin = null,
            profileImage = null
        )
        fakeRepository.profileResult = Resource.Success(profile)

        viewModel.loadProfile("author1")
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.profile).isEqualTo(profile)
    }

    @Test
    fun `loadProfile error emits error event`() = runTest {
        fakeRepository.profileResult = Resource.Error("Profile not found")

        viewModel.events.test {
            viewModel.loadProfile("unknown")
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.ShowErrorDialog::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadProfile sets loading then clears it`() = runTest {
        viewModel.loadProfile("author1")
        advanceUntilIdle()

        assertThat(viewModel.isLoading.value).isFalse()
    }

    @Test
    fun `loadProfile skips if same username already loaded`() = runTest {
        val profile = UserProfile(
            username = "author1",
            bio = "Original",
            location = null,
            website = null,
            twitter = null,
            facebook = null,
            instagram = null,
            linkedin = null,
            profileImage = null
        )
        fakeRepository.profileResult = Resource.Success(profile)

        viewModel.loadProfile("author1")
        advanceUntilIdle()

        fakeRepository.profileResult = Resource.Success(
            profile.copy(bio = "Changed")
        )
        viewModel.loadProfile("author1")
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.profile?.bio).isEqualTo("Original")
    }

    @Test
    fun `loadProfile reloads for different username`() = runTest {
        val profile1 = UserProfile(
            username = "author1",
            bio = null,
            location = null,
            website = null,
            twitter = null,
            facebook = null,
            instagram = null,
            linkedin = null,
            profileImage = null
        )
        fakeRepository.profileResult = Resource.Success(profile1)
        viewModel.loadProfile("author1")
        advanceUntilIdle()

        val profile2 = UserProfile(
            username = "author2",
            bio = "Different",
            location = null,
            website = null,
            twitter = null,
            facebook = null,
            instagram = null,
            linkedin = null,
            profileImage = null
        )
        fakeRepository.profileResult = Resource.Success(profile2)
        viewModel.loadProfile("author2")
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.profile).isEqualTo(profile2)
    }
}
