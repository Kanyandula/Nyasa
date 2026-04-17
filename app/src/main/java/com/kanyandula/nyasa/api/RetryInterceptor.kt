package com.kanyandula.nyasa.api

import okhttp3.Interceptor
import okhttp3.Response
import java.net.SocketTimeoutException

class RetryInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.method != "GET") return chain.proceed(request)

        val delays = longArrayOf(250L, 1_000L, 4_000L)
        var attempt = 0

        while (true) {
            val result = runCatching { chain.proceed(request) }

            result.onSuccess { response ->
                if (response.code in 500..599 && attempt < delays.size) {
                    response.close()
                    Thread.sleep(delays[attempt])
                    attempt++
                    return@onSuccess
                }
                return response
            }

            result.onFailure { throwable ->
                if (
                    throwable is SocketTimeoutException &&
                    attempt < delays.size
                ) {
                    Thread.sleep(delays[attempt])
                    attempt++
                } else {
                    throw throwable
                }
            }
        }
    }
}
