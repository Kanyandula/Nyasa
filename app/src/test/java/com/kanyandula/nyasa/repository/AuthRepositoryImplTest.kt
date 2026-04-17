package com.kanyandula.nyasa.repository

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.api.auth.NyasaBlogApiAuthService
import com.kanyandula.nyasa.api.auth.network_responses.LoginResponse
import com.kanyandula.nyasa.api.auth.network_responses.RegistrationResponse
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.persistance.AccountPropertiesDao
import com.kanyandula.nyasa.persistance.AuthTokenDao
import com.kanyandula.nyasa.repository.auth.AuthRepositoryImpl
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.InputValidation
import com.kanyandula.nyasa.util.MainDispatcherRule
import com.kanyandula.nyasa.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var authService: NyasaBlogApiAuthService
    private lateinit var authTokenDao: AuthTokenDao
    private lateinit var accountPropertiesDao: AccountPropertiesDao
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        authService = mockk()
        authTokenDao = mockk(relaxed = true)
        accountPropertiesDao = mockk(relaxed = true)
        connectivityObserver = mockk()
        sharedPreferences = mockk(relaxed = true)
        editor = mockk(relaxed = true)

        every { connectivityObserver.isConnected } returns MutableStateFlow(true)

        // Mock InputValidation because android.util.Patterns is unavailable in JVM tests.
        // Validation logic itself should be covered via instrumented tests.
        mockkObject(InputValidation)
        every { InputValidation.validateLoginFields(any(), any()) } returns null
        every { InputValidation.validateRegistrationFields(any(), any(), any(), any()) } returns null

        repository = AuthRepositoryImpl(
            authTokenDao = authTokenDao,
            accountPropertiesDao = accountPropertiesDao,
            nyasaBlogApiAuthService = authService,
            connectivityObserver = connectivityObserver,
            sharedPreferences = sharedPreferences,
            sharedPrefsEditor = editor
        )
    }

    @After
    fun tearDown() {
        unmockkObject(InputValidation)
    }

    @Test
    fun `login success saves token and returns success`() = runTest {
        val loginResponse = LoginResponse(
            "Success",
            "",
            "test-token",
            1,
            "test@test.com"
        )
        coEvery { authService.login(any(), any()) } returns Response.success(loginResponse)
        coEvery { accountPropertiesDao.insertOrIgnore(any()) } returns 1L
        coEvery { authTokenDao.insert(any()) } returns 1L

        val results = repository.attemptLogin("test@test.com", "password123").toList()

        assertThat(results.first()).isInstanceOf(Resource.Loading::class.java)
        val success = results.last()
        assertThat(success).isInstanceOf(Resource.Success::class.java)
        assertThat((success as Resource.Success).data.token).isEqualTo("test-token")
        assertThat(success.data.account_pk).isEqualTo(1)

        coVerify { authTokenDao.insert(AuthToken(1, "test-token")) }
    }

    @Test
    fun `login with auth error returns error`() = runTest {
        val loginResponse = LoginResponse(
            "Error",
            "Invalid credentials",
            "",
            0,
            ""
        )
        coEvery { authService.login(any(), any()) } returns Response.success(loginResponse)

        val results = repository.attemptLogin("test@test.com", "password123").toList()

        val last = results.last()
        assertThat(last).isInstanceOf(Resource.Error::class.java)
        val error = (last as Resource.Error).error
        assertThat(error).isInstanceOf(AppError.Validation::class.java)
        assertThat((error as AppError.Validation).fields["auth"]).isEqualTo("Invalid credentials")
    }

    @Test
    fun `login without internet returns error`() = runTest {
        every { connectivityObserver.isConnected } returns MutableStateFlow(false)

        val results = repository.attemptLogin("test@test.com", "password123").toList()

        val last = results.last()
        assertThat(last).isInstanceOf(Resource.Error::class.java)
        assertThat((last as Resource.Error).error).isEqualTo(AppError.Offline)
    }

    @Test
    fun `login with validation error returns error`() = runTest {
        every { InputValidation.validateLoginFields(any(), any()) } returns AppError.Validation(
            mapOf("password" to "Password is required.")
        )

        val results = repository.attemptLogin("test@test.com", "").toList()

        val last = results.last()
        assertThat(last).isInstanceOf(Resource.Error::class.java)
        val error = (last as Resource.Error).error
        assertThat(error).isInstanceOf(AppError.Validation::class.java)
        assertThat((error as AppError.Validation).fields["password"]).isEqualTo("Password is required.")
    }

    @Test
    fun `registration success saves account and token`() = runTest {
        val regResponse = RegistrationResponse(
            "Success",
            "",
            "test@test.com",
            "testuser",
            1,
            "reg-token"
        )
        coEvery { authService.register(any(), any(), any(), any()) } returns Response.success(regResponse)
        coEvery { accountPropertiesDao.insertAndReplace(any()) } returns 1L
        coEvery { authTokenDao.insert(any()) } returns 1L

        val results = repository.attemptRegistration(
            "test@test.com",
            "testuser",
            "password123",
            "password123"
        ).toList()

        val success = results.last()
        assertThat(success).isInstanceOf(Resource.Success::class.java)
        assertThat((success as Resource.Success).data.token).isEqualTo("reg-token")

        coVerify { accountPropertiesDao.insertAndReplace(AccountProperties(1, "test@test.com", "testuser")) }
    }

    @Test
    fun `registration with validation error returns error`() = runTest {
        every {
            InputValidation.validateRegistrationFields(any(), any(), any(), any())
        } returns AppError.Validation(mapOf("confirm_password" to "Passwords must match."))

        val results = repository.attemptRegistration(
            "test@test.com",
            "testuser",
            "password123",
            "different123"
        ).toList()

        val last = results.last()
        assertThat(last).isInstanceOf(Resource.Error::class.java)
        val error = (last as Resource.Error).error
        assertThat(error).isInstanceOf(AppError.Validation::class.java)
        assertThat((error as AppError.Validation).fields["confirm_password"]).isEqualTo("Passwords must match.")
    }

    @Test
    fun `checkPreviousAuthUser with no saved email returns null`() = runTest {
        every { sharedPreferences.getString(any(), any()) } returns null

        val results = repository.checkPreviousAuthUser().toList()

        val success = results.last()
        assertThat(success).isInstanceOf(Resource.Success::class.java)
        assertThat((success as Resource.Success).data).isNull()
    }

    @Test
    fun `checkPreviousAuthUser with valid token returns token`() = runTest {
        val account = AccountProperties(pk = 1, email = "test@test.com", username = "user")
        val token = AuthToken(1, "saved-token")

        every { sharedPreferences.getString(any(), any()) } returns "test@test.com"
        coEvery { accountPropertiesDao.searchByEmail("test@test.com") } returns account
        coEvery { authTokenDao.searchByPk(1) } returns token

        val results = repository.checkPreviousAuthUser().toList()

        val success = results.last()
        assertThat(success).isInstanceOf(Resource.Success::class.java)
        assertThat((success as Resource.Success).data).isEqualTo(token)
    }

    @Test
    fun `checkPreviousAuthUser with no account returns null`() = runTest {
        every { sharedPreferences.getString(any(), any()) } returns "test@test.com"
        coEvery { accountPropertiesDao.searchByEmail("test@test.com") } returns null

        val results = repository.checkPreviousAuthUser().toList()

        val success = results.last()
        assertThat(success).isInstanceOf(Resource.Success::class.java)
        assertThat((success as Resource.Success).data).isNull()
    }
}
