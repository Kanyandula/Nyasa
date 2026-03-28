package com.kanyandula.nyasa.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import kotlinx.coroutines.flow.Flow

abstract class BaseViewModel<StateEvent, ViewState> : ViewModel() {

    val TAG: String = "AppDebug"

    protected val _stateEvent: MutableLiveData<StateEvent> = MutableLiveData()
    protected val _viewState: MutableLiveData<ViewState> = MutableLiveData()

    val viewState: LiveData<ViewState>
        get() = _viewState

    val dataState: LiveData<DataState<ViewState>> = _stateEvent
        .switchMap { stateEvent ->
            stateEvent?.let {
                handleStateEvent(stateEvent).asLiveData()
            }
        }

    fun setStateEvent(event: StateEvent) {
        _stateEvent.value = event
    }

    fun getCurrentViewStateOrNew(): ViewState {
        val value = viewState.value ?: initNewViewState()
        return value
    }

    fun setViewState(viewState: ViewState) {
        _viewState.value = viewState
    }

    abstract fun handleStateEvent(stateEvent: StateEvent): Flow<DataState<ViewState>>

    abstract fun initNewViewState(): ViewState
}
