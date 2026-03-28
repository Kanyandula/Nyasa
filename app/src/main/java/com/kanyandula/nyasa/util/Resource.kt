package com.kanyandula.nyasa.util

sealed class Resource<out T> {
    // Not a data class to avoid equals/copy issues with covariant type parameter
    class Loading<out T>(val data: T? = null) : Resource<T>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}
