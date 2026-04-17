package com.kanyandula.nyasa.api.auth.network_responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class RegistrationResponse(

    @SerialName("response")
    var response: String,

    @SerialName("error_message")
    var errorMessage: String,

    @SerialName("email")
    var email: String,

    @SerialName("username")
    var username: String,

    @SerialName("pk")
    var pk: Int,

    @SerialName("token")
    var token: String
) {

    override fun toString(): String {
        return "RegistrationResponse(response='$response', errorMessage='$errorMessage', " +
            "email='$email', username='$username', token='$token')"
    }
}
