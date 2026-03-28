package com.kanyandula.nyasa.ui

import androidx.lifecycle.ViewModel
import com.kanyandula.nyasa.util.ErrorHandling
import com.kanyandula.nyasa.util.ErrorHandling.ERROR_CHECK_NETWORK_CONNECTION
import com.kanyandula.nyasa.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel<ViewState>(initialState: ViewState) : ViewModel() {

    private val _viewState = MutableStateFlow(initialState)
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 10)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    protected fun updateState(reducer: ViewState.() -> ViewState) {
        _viewState.value = _viewState.value.reducer()
    }

    protected fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    protected fun sendEvent(event: UiEvent) {
        _events.tryEmit(event)
    }

    protected fun handleError(message: String) {
        setLoading(false)
        if (ErrorHandling.isNetworkError(message)) {
            sendEvent(UiEvent.ShowToast(ERROR_CHECK_NETWORK_CONNECTION))
        } else {
            sendEvent(UiEvent.ShowErrorDialog(message))
        }
    }

    protected fun <T> handleResource(
        resource: Resource<T>,
        onLoading: (T?) -> Unit = {},
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit = { handleError(it) }
    ) {
        when (resource) {
            is Resource.Loading -> {
                if (resource.data == null) setLoading(true)
                onLoading(resource.data)
            }
            is Resource.Success -> {
                setLoading(false)
                onSuccess(resource.data)
            }
            is Resource.Error -> onError(resource.message)
        }
    }

    companion object {
        const val TAG: String = "AppDebug"
    }
}
