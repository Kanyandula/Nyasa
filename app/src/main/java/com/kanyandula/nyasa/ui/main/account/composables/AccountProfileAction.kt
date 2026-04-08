package com.kanyandula.nyasa.ui.main.account.composables

sealed interface AccountProfileAction {
    data object EditProfile : AccountProfileAction
    data object ChangePassword : AccountProfileAction
    data object Bookmarks : AccountProfileAction
    data object Logout : AccountProfileAction
}
