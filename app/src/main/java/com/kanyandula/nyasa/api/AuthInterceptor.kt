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
        val request = chain.request().newBuilder()
            .apply {
                if (token != null) {
                    addHeader("Authorization", "Token $token")
                }
            }
            .build()
        val response = chain.proceed(request)
        if (response.code == 401) {
            session.invalidate()
        }
        return response
    }
}
