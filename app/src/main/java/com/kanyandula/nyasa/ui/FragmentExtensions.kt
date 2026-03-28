package com.kanyandula.nyasa.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

fun Fragment.collectLoadingState(
    isLoading: StateFlow<Boolean>,
    listener: DataStateChangeListener
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            isLoading.collect { loading ->
                listener.displayProgressBar(loading)
            }
        }
    }
}

fun Fragment.collectUiEvents(
    events: SharedFlow<UiEvent>,
    onEvent: (UiEvent) -> Unit
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            events.collect(onEvent)
        }
    }
}

fun handleStandardUiEvent(event: UiEvent, listener: DataStateChangeListener): Boolean {
    return when (event) {
        is UiEvent.ShowToast -> {
            listener.displayToast(event.message)
            true
        }
        is UiEvent.ShowErrorDialog -> {
            listener.displayErrorDialog(event.message)
            true
        }
        is UiEvent.ShowSuccessDialog -> {
            listener.displaySuccessDialog(event.message)
            true
        }
        else -> false
    }
}

fun Fragment.setupActionBarWithNavController(
    fragmentId: Int,
    activity: AppCompatActivity
) {
    val appBarConfiguration = AppBarConfiguration(setOf(fragmentId))
    NavigationUI.setupActionBarWithNavController(
        activity,
        findNavController(),
        appBarConfiguration
    )
}
