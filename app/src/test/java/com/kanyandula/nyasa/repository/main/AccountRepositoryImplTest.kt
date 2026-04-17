package com.kanyandula.nyasa.repository.main

import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.api.GenericResponse
import com.kanyandula.nyasa.api.main.NyasaBlogApiMainService
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.persistance.AccountPropertiesDao
import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.session.SessionManager
import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.MainDispatcherRule
import com.kanyandula.nyasa.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AccountRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var apiService: NyasaBlogApiMainService
    private lateinit var accountPropertiesDao: AccountPropertiesDao
    private lateinit var sessionManager: SessionManager
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var repository: AccountRepositoryImpl

    private val testToken = AuthToken(account_pk = 1, token = "test-token")
    private val testAccount = AccountProperties(pk = 1, email = "test@test.com", username = "testuser")

    @Before
    fun setup() {
        apiService = mockk()
        accountPropertiesDao = mockk(relaxed = true)
        sessionManager = mockk()
        connectivityObserver = mockk()

        every { sessionManager.cachedToken } returns MutableStateFlow(testToken)
        every { connectivityObserver.isConnected } returns MutableStateFlow(true)

        repository = AccountRepositoryImpl(
            apiService,
            accountPropertiesDao,
            sessionManager,
            connectivityObserver
        )
    }

    @Test
    fun `getAccountProperties when not authenticated returns error`() = runTest {
        every { sessionManager.cachedToken } returns MutableStateFlow(null)
        repository = AccountRepositoryImpl(
            apiService,
            accountPropertiesDao,
            sessionManager,
            connectivityObserver
        )

        val results = repository.getAccountProperties().toList()

        val last = results.last()
        assertThat(last).isInstanceOf(Resource.Error::class.java)
        assertThat((last as Resource.Error).error).isEqualTo(AppError.Unauthorized)
    }

    @Test
    fun `getAccountProperties with cache and network returns success`() = runTest {
        coEvery { accountPropertiesDao.searchByPk(1) } returns testAccount
        coEvery { apiService.getAccountProperties() } returns Response.success(testAccount)

        val results = repository.getAccountProperties().toList()

        val success = results.last()
        assertThat(success).isInstanceOf(Resource.Success::class.java)
        assertThat((success as Resource.Success).data.email).isEqualTo("test@test.com")
    }

    @Test
    fun `getAccountProperties with network updates DAO`() = runTest {
        coEvery { accountPropertiesDao.searchByPk(1) } returns null
        coEvery { apiService.getAccountProperties() } returns Response.success(testAccount)

        repository.getAccountProperties().toList()

        coVerify {
            accountPropertiesDao.updateAccountProperties(1, "test@test.com", "testuser")
        }
    }

    @Test
    fun `getAccountProperties offline with cache returns cached`() = runTest {
        every { connectivityObserver.isConnected } returns MutableStateFlow(false)
        coEvery { accountPropertiesDao.searchByPk(1) } returns testAccount

        val results = repository.getAccountProperties().toList()

        val success = results.last()
        assertThat(success).isInstanceOf(Resource.Success::class.java)
        assertThat((success as Resource.Success).data).isEqualTo(testAccount)
    }

    @Test
    fun `getAccountProperties offline without cache returns error`() = runTest {
        every { connectivityObserver.isConnected } returns MutableStateFlow(false)
        coEvery { accountPropertiesDao.searchByPk(1) } returns null

        val results = repository.getAccountProperties().toList()

        assertThat(results.last()).isInstanceOf(Resource.Error::class.java)
    }

    @Test
    fun `saveAccountProperties success updates DAO`() = runTest {
        coEvery {
            apiService.saveAccountProperties(any(), any())
        } returns Response.success(GenericResponse("Updated"))

        val results = repository.saveAccountProperties(testAccount).toList()

        val success = results.last() as Resource.Success
        assertThat(success.data).isEqualTo("Updated")
        coVerify { accountPropertiesDao.updateAccountProperties(1, "test@test.com", "testuser") }
    }

    @Test
    fun `saveAccountProperties offline returns error`() = runTest {
        every { connectivityObserver.isConnected } returns MutableStateFlow(false)

        val results = repository.saveAccountProperties(testAccount).toList()

        assertThat(results.last()).isInstanceOf(Resource.Error::class.java)
    }

    @Test
    fun `updatePassword success returns response`() = runTest {
        coEvery {
            apiService.updatePassword(any(), any(), any())
        } returns Response.success(GenericResponse("Password updated"))

        val results = repository.updatePassword("old", "new", "new").toList()

        val success = results.last() as Resource.Success
        assertThat(success.data).isEqualTo("Password updated")
    }
}
