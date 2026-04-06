package com.kanyandula.nyasa.repository

import com.kanyandula.nyasa.session.ConnectivityObserver
import com.kanyandula.nyasa.util.ApiEmptyResponse
import com.kanyandula.nyasa.util.ApiErrorResponse
import com.kanyandula.nyasa.util.ApiSuccessResponse
import com.kanyandula.nyasa.util.Constants.NETWORK_TIMEOUT
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_UNKNOWN
import com.kanyandula.nyasa.util.ErrorHandling.UNABLE_TODO_OPERATION_WO_INTERNET
import com.kanyandula.nyasa.util.ErrorHandling.UNABLE_TO_RESOLVE_HOST
import com.kanyandula.nyasa.util.GenericApiResponse
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.Response

fun apiErrorMessage(response: GenericApiResponse<*>?): String {
    return when (response) {
        is ApiErrorResponse -> response.errorMessage
        is ApiEmptyResponse -> "HTTP 204. Returned NOTHING."
        null -> UNABLE_TO_RESOLVE_HOST
        else -> ERROR_UNKNOWN
    }
}

fun <ApiType, ResultType> networkApiFlow(
    connectivityObserver: ConnectivityObserver,
    apiCall: suspend () -> Response<ApiType>,
    onSuccess: suspend (ApiType) -> Resource<ResultType>
): Flow<Resource<ResultType>> = flow {
    emit(Resource.Loading())

    if (!connectivityObserver.isConnected.value) {
        emit(Resource.Error(UNABLE_TODO_OPERATION_WO_INTERNET))
        return@flow
    }

    val response = withTimeoutOrNull(NETWORK_TIMEOUT) {
        safeApiCall { apiCall() }
    }

    when (response) {
        is ApiSuccessResponse -> emit(onSuccess(response.body))
        else -> emit(Resource.Error(apiErrorMessage(response)))
    }
}.flowOn(Dispatchers.IO)
