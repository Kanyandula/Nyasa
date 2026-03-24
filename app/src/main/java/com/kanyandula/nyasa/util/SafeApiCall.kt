package com.kanyandula.nyasa.util

import retrofit2.Response

@Suppress("TooGenericExceptionCaught")
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): GenericApiResponse<T> {
    return try {
        GenericApiResponse.create(apiCall())
    } catch (e: Exception) {
        GenericApiResponse.create(e)
    }
}
