package com.kanyandula.nyasa.ui

import com.kanyandula.nyasa.util.ErrorHandling
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_CHECK_NETWORK_CONNECTION
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_UNKNOWN

data class DataState<T>(
    var error: Event<StateError>? = null,
    var loading: Loading = Loading(false),
    var data: Data<T>? = null
) {

    companion object {

        fun <T> error(
            response: Response
        ): DataState<T> {
            return DataState(
                error = Event(
                    StateError(
                        response
                    )
                ),
                loading = Loading(false),
                data = null
            )
        }

        fun <T> loading(
            isLoading: Boolean,
            cachedData: T? = null
        ): DataState<T> {
            return DataState(
                error = null,
                loading = Loading(isLoading),
                data = Data(
                    Event.dataEvent(
                        cachedData
                    ),
                    null
                )
            )
        }

        fun <T> data(
            data: T? = null,
            response: Response? = null
        ): DataState<T> {
            return DataState(
                error = null,
                loading = Loading(false),
                data = Data(
                    Event.dataEvent(data),
                    Event.responseEvent(response)
                )
            )
        }

        fun <T> apiError(
            message: String?,
            shouldUseDialog: Boolean = true,
            shouldUseToast: Boolean = false
        ): DataState<T> {
            var msg = message ?: ERROR_UNKNOWN
            var useDialog = shouldUseDialog
            var responseType: ResponseType = ResponseType.None()
            if (ErrorHandling.isNetworkError(msg)) {
                msg = ERROR_CHECK_NETWORK_CONNECTION
                useDialog = false
            }
            if (shouldUseToast) responseType = ResponseType.Toast()
            if (useDialog) responseType = ResponseType.Dialog()
            return error(Response(msg, responseType))
        }
    }
}
