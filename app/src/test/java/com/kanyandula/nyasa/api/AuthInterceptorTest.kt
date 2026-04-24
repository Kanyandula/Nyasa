package com.kanyandula.nyasa.api

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.net.InetAddress

class AuthInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var tokenProvider: TokenProvider
    private lateinit var client: OkHttpClient

    private val localhostDns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> =
            listOf(InetAddress.getLoopbackAddress())
    }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        tokenProvider = mockk(relaxed = true)
        every { tokenProvider.token } returns "test-token"
        client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .dns(localhostDns)
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `adds Authorization header when token exists`() {
        server.enqueue(MockResponse().setResponseCode(200))
        client.newCall(Request.Builder().url(apiUrl()).build()).execute()
        val recorded = server.takeRequest()
        assertEquals("Token test-token", recorded.getHeader("Authorization"))
    }

    @Test
    fun `no Authorization header when token is null`() {
        every { tokenProvider.token } returns null
        val noTokenClient = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .dns(localhostDns)
            .build()
        server.enqueue(MockResponse().setResponseCode(200))
        noTokenClient.newCall(Request.Builder().url(apiUrl()).build()).execute()
        val recorded = server.takeRequest()
        assertEquals(null, recorded.getHeader("Authorization"))
    }

    @Test
    fun `no Authorization header for non-API host`() {
        server.enqueue(MockResponse().setResponseCode(200))
        client.newCall(Request.Builder().url(server.url("/image.jpg")).build()).execute()
        val recorded = server.takeRequest()
        assertEquals(null, recorded.getHeader("Authorization"))
    }

    @Test
    fun `401 response calls TokenProvider invalidate`() {
        server.enqueue(MockResponse().setResponseCode(401))
        client.newCall(Request.Builder().url(apiUrl()).build()).execute()
        verify(exactly = 1) { tokenProvider.invalidate() }
    }

    @Test
    fun `401 from non-API host does not call invalidate`() {
        server.enqueue(MockResponse().setResponseCode(401))
        client.newCall(Request.Builder().url(server.url("/")).build()).execute()
        verify(exactly = 0) { tokenProvider.invalidate() }
    }

    @Test
    fun `200 response does not call invalidate`() {
        server.enqueue(MockResponse().setResponseCode(200))
        client.newCall(Request.Builder().url(apiUrl()).build()).execute()
        verify(exactly = 0) { tokenProvider.invalidate() }
    }

    private fun apiUrl(path: String = "/"): okhttp3.HttpUrl =
        server.url(path).newBuilder().host("nyasablog.com").build()
}
