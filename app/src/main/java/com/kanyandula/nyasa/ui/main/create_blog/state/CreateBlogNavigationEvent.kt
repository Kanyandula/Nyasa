@file:Suppress("PackageNaming")

package com.kanyandula.nyasa.ui.main.create_blog.state

import com.kanyandula.nyasa.ui.UiEvent

sealed interface CreateBlogNavigationEvent : UiEvent {
    object BlogCreated : CreateBlogNavigationEvent
}
