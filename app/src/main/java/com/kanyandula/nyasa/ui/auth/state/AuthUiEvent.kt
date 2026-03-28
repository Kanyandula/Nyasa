package com.kanyandula.nyasa.ui.auth.state

import com.kanyandula.nyasa.ui.UiEvent

sealed interface AuthUiEvent : UiEvent {
    object CheckPreviousAuthDone : AuthUiEvent
}
