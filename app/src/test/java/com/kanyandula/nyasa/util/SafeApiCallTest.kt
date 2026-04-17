package com.kanyandula.nyasa.util

import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class SafeApiCallTest {

    @Test
    fun `successful response returns Resource Success`() = runTest {
        val result = safeApiCall { Response.success("data") }
        assertTrue(result is Resource.Success)
        assertEquals("data", (result as Resource.Success).data)
    }

    @Test
    fun `null body returns Unknown error`() = runTest {
        val result = safeApiCall { Response.success<String?>(null) }
        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).error is AppError.Unknown)
    }

    @Test
    fun `401 returns Unauthorized`() = runTest {
        val result = safeApiCall {
            Response.error<String>(401, "{}".toResponseBody("application/json".toMediaType()))
        }
        assertTrue(result is Resource.Error)
        assertEquals(AppError.Unauthorized, (result as Resource.Error).error)
    }

    @Test
    fun `403 returns Forbidden`() = runTest {
        val result = safeApiCall {
            Response.error<String>(403, "{}".toResponseBody("application/json".toMediaType()))
        }
        assertTrue(result is Resource.Error)
        assertEquals(AppError.Forbidden, (result as Resource.Error).error)
    }

    @Test
    fun `404 returns NotFound`() = runTest {
        val result = safeApiCall {
            Response.error<String>(404, "{}".toResponseBody("application/json".toMediaType()))
        }
        assertTrue(result is Resource.Error)
        assertEquals(AppError.NotFound, (result as Resource.Error).error)
    }

    @Test
    fun `400 returns Validation with parsed fields`() = runTest {
        val body = """{"email": "This field is required"}"""
        val result = safeApiCall {
            Response.error<String>(400, body.toResponseBody("application/json".toMediaType()))
        }
        assertTrue(result is Resource.Error)
        val error = (result as Resource.Error).error
        assertTrue(error is AppError.Validation)
        assertEquals("This field is required", (error as AppError.Validation).fields["email"])
    }

    @Test
    fun `500 returns Server error`() = runTest {
        val result = safeApiCall {
            Response.error<String>(500, "{}".toResponseBody("application/json".toMediaType()))
        }
        assertTrue(result is Resource.Error)
        val error = (result as Resource.Error).error
        assertTrue(error is AppError.Server)
        assertEquals(500, (error as AppError.Server).code)
    }

    @Test
    fun `SocketTimeoutException returns Timeout`() = runTest {
        val result = safeApiCall<String> { throw SocketTimeoutException("timed out") }
        assertTrue(result is Resource.Error)
        assertEquals(AppError.Timeout, (result as Resource.Error).error)
    }

    @Test
    fun `IOException returns Offline`() = runTest {
        val result = safeApiCall<String> { throw IOException("network failure") }
        assertTrue(result is Resource.Error)
        assertEquals(AppError.Offline, (result as Resource.Error).error)
    }

    @Test
    fun `unexpected exception returns Unknown`() = runTest {
        val result = safeApiCall<String> { throw RuntimeException("unexpected") }
        assertTrue(result is Resource.Error)
        assertTrue((result as Resource.Error).error is AppError.Unknown)
    }
}
