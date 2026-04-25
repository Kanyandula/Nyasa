package com.kanyandula.nyasa.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class GenericResponse(
    @SerialName("response")
    var response: String = ""
)
