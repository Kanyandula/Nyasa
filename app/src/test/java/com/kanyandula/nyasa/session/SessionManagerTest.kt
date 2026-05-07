package com.kanyandula.nyasa.session

import com.google.common.truth.Truth.assertThat
import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.persistance.AuthTokenDao
import com.kanyandula.nyasa.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authTokenDao: AuthTokenDao = mockk()

    @Test
    fun `invalidate nullifies persisted token for cached account`() {
        coEvery { authTokenDao.nullifyToken(any()) } returns 1
        val sessionManager = SessionManager(authTokenDao)
        sessionManager.login(AuthToken(account_pk = 42, token = "abc"))

        sessionManager.invalidate()

        coVerify(timeout = 1_000L, exactly = 1) { authTokenDao.nullifyToken(42) }
        assertThat(sessionManager.token).isNull()
    }

    @Test
    fun `invalidate without cached token does not touch dao`() {
        val sessionManager = SessionManager(authTokenDao)

        sessionManager.invalidate()

        coVerify(exactly = 0) { authTokenDao.nullifyToken(any()) }
    }
}
