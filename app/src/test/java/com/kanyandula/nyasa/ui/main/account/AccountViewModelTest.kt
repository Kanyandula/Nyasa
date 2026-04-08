package com.kanyandula.nyasa.ui.main.account

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.domain.usecase.account.ChangePasswordUseCase
import com.kanyandula.nyasa.domain.usecase.account.GetAccountPropertiesUseCase
import com.kanyandula.nyasa.domain.usecase.account.SaveAccountPropertiesUseCase
import com.kanyandula.nyasa.domain.usecase.profile.UpdateProfileUseCase
import com.kanyandula.nyasa.fakes.FakeAccountRepository
import com.kanyandula.nyasa.fakes.FakeProfileRepository
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.ui.main.account.state.AccountUiEvent
import com.kanyandula.nyasa.util.MainDispatcherRule
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.SuccessHandling.RESPONSE_PASSWORD_UPDATE_SUCCESS
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeAccountRepository
    private lateinit var fakeProfileRepository: FakeProfileRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: AccountViewModel

    @Before
    fun setup() {
        fakeRepository = FakeAccountRepository()
        fakeProfileRepository = FakeProfileRepository()
        sessionManager = mockk(relaxed = true)
        every { sessionManager.cachedToken } returns MutableStateFlow(null)
        viewModel = AccountViewModel(
            sessionManager = sessionManager,
            getAccountPropertiesUseCase = GetAccountPropertiesUseCase(fakeRepository),
            saveAccountPropertiesUseCase = SaveAccountPropertiesUseCase(fakeRepository),
            changePasswordUseCase = ChangePasswordUseCase(fakeRepository),
            updateProfileUseCase = UpdateProfileUseCase(fakeProfileRepository)
        )
    }

    @Test
    fun `getAccountProperties success updates state`() = runTest {
        val account = AccountProperties(pk = 1, email = "test@test.com", username = "testuser")
        fakeRepository.accountPropertiesResult = Resource.Success(account)

        viewModel.getAccountProperties()
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.accountProperties).isEqualTo(account)
        assertThat(viewModel.isLoading.value).isFalse()
    }

    @Test
    fun `getAccountProperties error emits error event`() = runTest {
        fakeRepository.accountPropertiesResult = Resource.Error("Failed to fetch")

        viewModel.events.test {
            viewModel.getAccountProperties()
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.ShowErrorDialog::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveAccountProperties emits toast on success`() = runTest {
        val account = AccountProperties(pk = 1, email = "old@test.com", username = "old")
        viewModel.setAccountPropertiesData(account)

        fakeRepository.saveResult = Resource.Success("Update successful")

        viewModel.events.test {
            viewModel.saveAccountProperties("new@test.com", "newuser")
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.ShowToast::class.java)
            assertThat((event as UiEvent.ShowToast).message).isEqualTo("Update successful")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveAccountProperties does nothing if accountProperties is null`() = runTest {
        // accountProperties is null by default
        viewModel.saveAccountProperties("new@test.com", "newuser")
        advanceUntilIdle()

        // No crash, no events
        assertThat(viewModel.isLoading.value).isFalse()
    }

    @Test
    fun `changePassword success emits PasswordChanged and toast`() = runTest {
        fakeRepository.updatePasswordResult = Resource.Success(RESPONSE_PASSWORD_UPDATE_SUCCESS)

        viewModel.events.test {
            viewModel.changePassword("old", "new", "new")
            advanceUntilIdle()

            val events = mutableListOf<com.kanyandula.nyasa.ui.UiEvent>()
            events.add(awaitItem())
            events.add(awaitItem())

            assertThat(events).contains(AccountUiEvent.PasswordChanged)
            assertThat(events.filterIsInstance<UiEvent.ShowToast>()).isNotEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changePassword error emits error event`() = runTest {
        fakeRepository.updatePasswordResult = Resource.Error("Wrong password")

        viewModel.events.test {
            viewModel.changePassword("wrong", "new", "new")
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.ShowErrorDialog::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `logout delegates to sessionManager`() {
        viewModel.logout()
        verify { sessionManager.logout() }
    }

    @Test
    fun `setAccountPropertiesData updates state`() {
        val account = AccountProperties(pk = 1, email = "test@test.com", username = "user")
        viewModel.setAccountPropertiesData(account)
        assertThat(viewModel.viewState.value.accountProperties).isEqualTo(account)
    }
}
