package com.kanyandula.nyasa.api

import com.kanyandula.nyasa.models.AuthToken
import com.kanyandula.nyasa.session.SessionManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var sessionManager: SessionManager
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        sessionManager = mockk(relaxed = true)
        every { sessionManager.cachedToken } returns MutableStateFlow(AuthToken(1, "test-token"))
        client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `adds Authorization header when token exists`() {
        server.enqueue(MockResponse().setResponseCode(200))
        client.newCall(Request.Builder().url(server.url("/")).build()).execute()
        val recorded = server.takeRequest()
        assertEquals("Token test-token", recorded.getHeader("Authorization"))
    }

    @Test
    fun `no Authorization header when token is null`() {
        every { sessionManager.cachedToken } returns MutableStateFlow(null)
        val noTokenClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .build()
        server.enqueue(MockResponse().setResponseCode(200))
        noTokenClient.newCall(Request.Builder().url(server.url("/")).build()).execute()
        val recorded = server.takeRequest()
        assertEquals(null, recorded.getHeader("Authorization"))
    }

    @Test
    fun `401 response calls SessionManager invalidate`() {
        server.enqueue(MockResponse().setResponseCode(401))
        client.newCall(Request.Builder().url(server.url("/")).build()).execute()
        verify(exactly = 1) { sessionManager.invalidate() }
    }

    @Test
    fun `200 response does not call invalidate`() {
        server.enqueue(MockResponse().setResponseCode(200))
        client.newCall(Request.Builder().url(server.url("/")).build()).execute()
        verify(exactly = 0) { sessionManager.invalidate() }
    }
}
