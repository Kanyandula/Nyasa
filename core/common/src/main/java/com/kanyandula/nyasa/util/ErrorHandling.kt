package com.kanyandula.nyasa.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

val lenientJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}

const val PAGINATION_DONE_ERROR = "Invalid page."

fun AppError.toUserMessage(): String = when (this) {
    is AppError.Offline -> "No internet connection. Please check your network."
    is AppError.Timeout -> "Request timed out. Please try again."
    is AppError.Unauthorized -> "Session expired. Please log in again."
    is AppError.Forbidden -> "You don't have permission to do this."
    is AppError.NotFound -> "The requested content was not found."
    is AppError.Validation -> fields.values.firstOrNull()
        ?: "Please check your input."
    is AppError.Server -> "Server error ($code). Please try again later."
    is AppError.Unknown -> "Something went wrong. Please try again."
}

fun isPaginationDone(errorBody: String?): Boolean {
    if (errorBody.isNullOrBlank()) return false
    return try {
        val json = lenientJson.parseToJsonElement(errorBody)
        val detail = json.jsonObject["detail"]?.jsonPrimitive?.content
        detail == PAGINATION_DONE_ERROR
    } catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") e: Exception) {
        errorBody == PAGINATION_DONE_ERROR
    }
}
