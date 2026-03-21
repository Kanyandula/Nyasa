package com.kanyandula.nyasa.api

import com.kanyandula.nyasa.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenInterceptor
@Inject
constructor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        sessionManager.cachedToken.value?.token?.let { token ->
            requestBuilder.addHeader("Authorization", "Token $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}
