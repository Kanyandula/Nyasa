package com.kanyandula.nyasa.ui

interface UiEvent {
    data class ShowToast(val message: String) : UiEvent
    data class ShowSuccessDialog(val message: String) : UiEvent
    data class ShowErrorDialog(val message: String) : UiEvent
}
