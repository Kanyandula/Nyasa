package com.kanyandula.nyasa.repository

import com.kanyandula.nyasa.ui.DataState
import com.kanyandula.nyasa.util.ApiEmptyResponse
import com.kanyandula.nyasa.util.ApiErrorResponse
import com.kanyandula.nyasa.util.ErrorHandling.UNABLE_TO_RESOLVE_HOST
import com.kanyandula.nyasa.util.GenericApiResponse
import kotlinx.coroutines.flow.FlowCollector

suspend fun <T> FlowCollector<DataState<T>>.emitApiError(
    response: GenericApiResponse<*>?
) {
    when (response) {
        is ApiErrorResponse -> emit(DataState.apiError<T>(response.errorMessage))
        is ApiEmptyResponse -> emit(DataState.apiError<T>("HTTP 204. Returned NOTHING."))
        null -> emit(DataState.apiError<T>(UNABLE_TO_RESOLVE_HOST, false, true))
        else -> { /* success handled by caller */ }
    }
}
