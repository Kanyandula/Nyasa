package com.kanyandula.nyasa.api.auth.network_responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class LoginResponse(

    @SerialName("response")
    var response: String,

    @SerialName("error_message")
    var errorMessage: String = "",

    @SerialName("token")
    var token: String,

    @SerialName("pk")
    var pk: Int,

    @SerialName("email")
    var email: String
) {
    override fun toString(): String {
        return "LoginResponse(response='$response', errorMessage='$errorMessage', " +
            "token='$token', pk=$pk, email='$email')"
    }
}
