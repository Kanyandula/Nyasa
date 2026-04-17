package com.kanyandula.nyasa.api

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RetryInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = OkHttpClient.Builder()
            .addInterceptor(RetryInterceptor())
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `GET retries on 500 and succeeds on retry`() {
        server.enqueue(MockResponse().setResponseCode(500))
        server.enqueue(MockResponse().setResponseCode(200).setBody("ok"))
        val response = client.newCall(Request.Builder().url(server.url("/")).build()).execute()
        assertEquals(200, response.code)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `GET returns 500 after max retries exhausted`() {
        repeat(4) { server.enqueue(MockResponse().setResponseCode(500)) }
        val response = client.newCall(Request.Builder().url(server.url("/")).build()).execute()
        assertEquals(500, response.code)
        assertEquals(4, server.requestCount)
    }

    @Test
    fun `POST is never retried on 500`() {
        server.enqueue(MockResponse().setResponseCode(500))
        val response = client.newCall(
            Request.Builder().url(server.url("/")).post(byteArrayOf().toRequestBody()).build()
        ).execute()
        assertEquals(500, response.code)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `PUT is never retried`() {
        server.enqueue(MockResponse().setResponseCode(500))
        val response = client.newCall(
            Request.Builder().url(server.url("/")).put(byteArrayOf().toRequestBody()).build()
        ).execute()
        assertEquals(500, response.code)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `DELETE is never retried`() {
        server.enqueue(MockResponse().setResponseCode(500))
        val response = client.newCall(
            Request.Builder().url(server.url("/")).delete(byteArrayOf().toRequestBody()).build()
        ).execute()
        assertEquals(500, response.code)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `GET 200 is not retried`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("ok"))
        val response = client.newCall(Request.Builder().url(server.url("/")).build()).execute()
        assertEquals(200, response.code)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `GET 404 is not retried`() {
        server.enqueue(MockResponse().setResponseCode(404))
        val response = client.newCall(Request.Builder().url(server.url("/")).build()).execute()
        assertEquals(404, response.code)
        assertEquals(1, server.requestCount)
    }
}
