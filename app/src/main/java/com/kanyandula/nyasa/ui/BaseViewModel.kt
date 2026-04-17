package com.kanyandula.nyasa.ui

import androidx.lifecycle.ViewModel
import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.toUserMessage
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

    protected fun handleError(error: AppError) {
        setLoading(false)
        if (error is AppError.Offline) {
            sendEvent(UiEvent.ShowToast(error.toUserMessage()))
        } else {
            sendEvent(UiEvent.ShowErrorDialog(error.toUserMessage()))
        }
    }

    protected fun <T> handleResource(
        resource: Resource<T>,
        onLoading: (T?) -> Unit = {},
        onSuccess: (T) -> Unit,
        onError: (AppError) -> Unit = { handleError(it) }
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
            is Resource.Error -> onError(resource.error)
        }
    }

    companion object {
        const val TAG: String = "AppDebug"
    }
}
