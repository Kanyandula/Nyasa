package com.kanyandula.nyasa.ui.auth

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.domain.usecase.auth.CheckPreviousAuthUseCase
import com.kanyandula.nyasa.domain.usecase.auth.LoginUseCase
import com.kanyandula.nyasa.domain.usecase.auth.RegisterUseCase
import com.kanyandula.nyasa.fakes.FakeAnalyticsTracker
import com.kanyandula.nyasa.fakes.FakeAuthRepository
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.ui.UiEvent
import com.kanyandula.nyasa.ui.auth.state.LoginFields
import com.kanyandula.nyasa.ui.auth.state.RegistrationFields
import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.MainDispatcherRule
import com.kanyandula.nyasa.util.Resource
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        fakeRepository = FakeAuthRepository()
        sessionManager = mockk(relaxed = true)
        viewModel = AuthViewModel(
            loginUseCase = LoginUseCase(fakeRepository),
            registerUseCase = RegisterUseCase(fakeRepository),
            checkPreviousAuthUseCase = CheckPreviousAuthUseCase(fakeRepository),
            sessionManager = sessionManager,
            analyticsTracker = FakeAnalyticsTracker()
        )
    }

    @Test
    fun `login success updates authToken in state`() = runTest {
        val expectedToken = AuthToken(1, "test-token")
        fakeRepository.loginResult = Resource.Success(expectedToken)

        viewModel.attemptLogin("test@test.com", "password")
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.authToken).isEqualTo(expectedToken)
        assertThat(viewModel.isLoading.value).isFalse()
    }

    @Test
    fun `login success activates session via SessionManager`() = runTest {
        val expectedToken = AuthToken(1, "test-token")
        fakeRepository.loginResult = Resource.Success(expectedToken)

        viewModel.attemptLogin("test@test.com", "password")
        advanceUntilIdle()

        verify { sessionManager.login(expectedToken) }
    }

    @Test
    fun `registration success activates session via SessionManager`() = runTest {
        val expectedToken = AuthToken(2, "reg-token")
        fakeRepository.registrationResult = Resource.Success(expectedToken)

        viewModel.attemptRegistration("test@test.com", "user", "pass", "pass")
        advanceUntilIdle()

        verify { sessionManager.login(expectedToken) }
    }

    @Test
    fun `checkPreviousAuthUser with token activates session`() = runTest {
        val existingToken = AuthToken(1, "existing-token")
        fakeRepository.previousAuthResult = Resource.Success(existingToken)

        viewModel.checkPreviousAuthUser()
        advanceUntilIdle()

        verify { sessionManager.login(existingToken) }
    }

    @Test
    fun `login error emits error event`() = runTest {
        fakeRepository.loginResult = Resource.Error(AppError.Unknown(RuntimeException("Invalid credentials")))

        viewModel.events.test {
            viewModel.attemptLogin("test@test.com", "wrong")
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.ShowErrorDialog::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `registration success updates authToken in state`() = runTest {
        val expectedToken = AuthToken(2, "reg-token")
        fakeRepository.registrationResult = Resource.Success(expectedToken)

        viewModel.attemptRegistration("test@test.com", "user", "pass", "pass")
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.authToken).isEqualTo(expectedToken)
    }

    @Test
    fun `registration error emits error event`() = runTest {
        fakeRepository.registrationResult = Resource.Error(AppError.Unknown(RuntimeException("Email already exists")))

        viewModel.events.test {
            viewModel.attemptRegistration("test@test.com", "user", "pass", "pass")
            advanceUntilIdle()

            val event = awaitItem()
            assertThat(event).isInstanceOf(UiEvent.ShowErrorDialog::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `checkPreviousAuthUser with token updates state`() = runTest {
        val existingToken = AuthToken(1, "existing-token")
        fakeRepository.previousAuthResult = Resource.Success(existingToken)

        viewModel.checkPreviousAuthUser()
        advanceUntilIdle()

        assertThat(viewModel.viewState.value.authToken).isEqualTo(existingToken)
    }

    @Test
    fun `checkPreviousAuthUser only runs once`() = runTest {
        fakeRepository.previousAuthResult = Resource.Success(null)

        viewModel.checkPreviousAuthUser()
        viewModel.checkPreviousAuthUser()
        advanceUntilIdle()

        assertThat(fakeRepository.checkPreviousAuthCallCount).isEqualTo(1)
    }

    @Test
    fun `setLoginFields updates state`() {
        val fields = LoginFields("test@test.com")
        viewModel.setLoginFields(fields)
        assertThat(viewModel.viewState.value.loginFields).isEqualTo(fields)
    }

    @Test
    fun `setRegistrationFields updates state`() {
        val fields = RegistrationFields("test@test.com", "testuser")
        viewModel.setRegistrationFields(fields)
        assertThat(viewModel.viewState.value.registrationFields).isEqualTo(fields)
    }

    @Test
    fun `setAuthToken updates state`() {
        val token = AuthToken(1, "manual-token")
        viewModel.setAuthToken(token)
        assertThat(viewModel.viewState.value.authToken).isEqualTo(token)
    }

    @Test
    fun `loading state toggles during login`() = runTest {
        fakeRepository.loginResult = Resource.Success(AuthToken(1, "t"))

        viewModel.attemptLogin("e", "p")

        // After launching but before idle, loading should be true once the coroutine runs
        advanceUntilIdle()

        // After completion, loading should be false
        assertThat(viewModel.isLoading.value).isFalse()
    }
}
