package com.kanyandula.nyasa.repository

import com.kanyandula.nyasa.util.ApiEmptyResponse
import com.kanyandula.nyasa.util.ApiErrorResponse
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_UNKNOWN
import com.kanyandula.nyasa.util.ErrorHandling.UNABLE_TO_RESOLVE_HOST
import com.kanyandula.nyasa.util.GenericApiResponse

fun apiErrorMessage(response: GenericApiResponse<*>?): String {
    return when (response) {
        is ApiErrorResponse -> response.errorMessage
        is ApiEmptyResponse -> "HTTP 204. Returned NOTHING."
        null -> UNABLE_TO_RESOLVE_HOST
        else -> ERROR_UNKNOWN
    }
}
