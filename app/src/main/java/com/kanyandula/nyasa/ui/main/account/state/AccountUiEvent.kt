package com.kanyandula.nyasa.ui.main.account.state

import com.kanyandula.nyasa.ui.UiEvent

sealed interface AccountUiEvent : UiEvent {
    object PasswordChanged : AccountUiEvent
}
