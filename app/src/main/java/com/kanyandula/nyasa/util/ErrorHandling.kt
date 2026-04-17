package com.kanyandula.nyasa.util

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

fun isPaginationDone(detail: String?): Boolean {
    return detail == PAGINATION_DONE_ERROR
}
