package com.kanyandula.nyasa.util

sealed interface AppError {
    data object Offline : AppError
    data object Timeout : AppError
    data object Unauthorized : AppError
    data object Forbidden : AppError
    data object NotFound : AppError
    data class Validation(
        val fields: Map<String, String>
    ) : AppError
    data class Server(val code: Int) : AppError
    data class Unknown(val cause: Throwable?) : AppError
}
