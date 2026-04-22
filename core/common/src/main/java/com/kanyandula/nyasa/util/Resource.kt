package com.kanyandula.nyasa.util

sealed class Resource<out T> {
    class Loading<out T>(val data: T? = null) : Resource<T>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val error: AppError) : Resource<Nothing>()
}
