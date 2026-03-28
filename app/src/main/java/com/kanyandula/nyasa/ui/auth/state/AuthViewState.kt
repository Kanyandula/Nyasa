package com.kanyandula.nyasa.ui.auth.state

import com.kanyandula.nyasa.models.AuthToken

data class AuthViewState(
    val registrationFields: RegistrationFields? = RegistrationFields(),
    val loginFields: LoginFields? = LoginFields(),
    val authToken: AuthToken? = null
)

data class RegistrationFields(
    val registration_email: String? = null,
    val registration_username: String? = null
)

data class LoginFields(
    val login_email: String? = null
)
