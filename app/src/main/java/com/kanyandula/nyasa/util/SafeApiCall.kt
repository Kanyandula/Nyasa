package com.kanyandula.nyasa.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

internal val lenientJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}

suspend inline fun <T> safeApiCall(
    crossinline call: suspend () -> Response<T>
): Resource<T> {
    return try {
        val response = call()
        when {
            response.isSuccessful -> {
                response.body()?.let { Resource.Success(it) }
                    ?: Resource.Error(
                        AppError.Unknown(
                            IllegalStateException("Response body is null")
                        )
                    )
            }
            response.code() == 401 -> Resource.Error(AppError.Unauthorized)
            response.code() == 403 -> Resource.Error(AppError.Forbidden)
            response.code() == 404 -> Resource.Error(AppError.NotFound)
            response.code() == 400 -> Resource.Error(
                AppError.Validation(parseDrfFieldErrors(response))
            )
            response.code() in 500..599 -> Resource.Error(
                AppError.Server(response.code())
            )
            else -> Resource.Error(
                AppError.Unknown(
                    IllegalStateException("HTTP ${response.code()}")
                )
            )
        }
    } catch (@Suppress("SwallowedException") e: SocketTimeoutException) {
        Resource.Error(AppError.Timeout)
    } catch (@Suppress("SwallowedException") e: IOException) {
        Resource.Error(AppError.Offline)
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        Resource.Error(AppError.Unknown(e))
    }
}

fun <T> parseDrfFieldErrors(response: Response<T>): Map<String, String> {
    return try {
        val errorBody = response.errorBody()?.string() ?: return emptyMap()
        val json = lenientJson.parseToJsonElement(errorBody).jsonObject
        json.entries.associate { (key, value) ->
            key to when (value) {
                is JsonArray -> value.firstOrNull()?.jsonPrimitive?.content ?: ""
                else -> value.jsonPrimitive.content
            }
        }
    } catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") e: Exception) {
        emptyMap()
    }
}
