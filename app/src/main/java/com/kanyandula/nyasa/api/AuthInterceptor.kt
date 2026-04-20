package com.kanyandula.nyasa.api

import com.kanyandula.nyasa.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val session: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = session.cachedToken.value?.token
        val originalRequest = chain.request()
        val request = originalRequest.newBuilder()
            .apply {
                if (token != null && originalRequest.url.host == API_HOST) {
                    addHeader("Authorization", "Token $token")
                }
            }
            .build()
        val response = chain.proceed(request)
        if (response.code == 401 && originalRequest.url.host == API_HOST) {
            session.invalidate()
        }
        return response
    }

    private companion object {
        const val API_HOST = "nyasablog.com"
    }
}
