package com.kanyandula.nyasa.ui.auth.composables

sealed interface LoginAction {
    data class Login(val email: String, val password: String) : LoginAction
    data object ForgotPassword : LoginAction
    data object NavigateToRegister : LoginAction
    data class EmailChanged(val email: String) : LoginAction
    data object BackClicked : LoginAction
}
