package com.kanyandula.nyasa.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kanyandula.nyasa.util.AppError
import com.kanyandula.nyasa.util.Resource
import com.kanyandula.nyasa.util.toUserMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    /**
     * Collects a `Resource` [flow] in [viewModelScope], routing each emission through
     * [handleResource] and reducing a success payload into the view state via [onSuccess].
     * Centralizes the launch/collect/reduce boilerplate shared by simple "load X into state" loads.
     */
    protected fun <T> collectIntoState(
        flow: Flow<Resource<T>>,
        onSuccess: ViewState.(T) -> ViewState
    ) {
        viewModelScope.launch {
            flow.collect { resource ->
                handleResource(resource, onSuccess = { data -> updateState { onSuccess(data) } })
            }
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
}
