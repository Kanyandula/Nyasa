package com.kanyandula.nyasa.repository

import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response

inline fun <T, R> networkApiFlow(
    connectivityObserver: ConnectivityObserver,
    crossinline apiCall: suspend () -> Response<T>,
    crossinline onSuccess: suspend (T) -> Resource<R>
): Flow<Resource<R>> = flow {
    emit(Resource.Loading())
    if (!connectivityObserver.isConnected.value) {
        emit(Resource.Error(AppError.Offline))
        return@flow
    }
    when (val result = safeApiCall { apiCall() }) {
        is Resource.Success -> emit(onSuccess(result.data))
        is Resource.Error -> emit(result)
        is Resource.Loading -> Unit
    }
}.flowOn(Dispatchers.IO)
