package com.kanyandula.nyasa.ui.auth.state

import com.kanyandula.nyasa.models.AuthToken

/**
 * The goal of view state is to all of the fields that you will have in any particular view
 */
data class AuthViewState(
    var registrationFields: RegistrationFields? = RegistrationFields(),
    var loginFields: LoginFields? = LoginFields(),
    var authToken: AuthToken? = null
)


data class RegistrationFields(
    var registration_email: String? = null,
    var registration_username: String? = null
)

data class LoginFields(
    var login_email: String? = null
)


