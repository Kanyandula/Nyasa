package com.kanyandula.nyasa.ui.main.blog.state

import com.kanyandula.nyasa.ui.UiEvent

sealed interface BlogNavigationEvent : UiEvent {
    object BlogDeleted : BlogNavigationEvent
    object BlogUpdateSuccess : BlogNavigationEvent
}
