package com.kanyandula.nyasa.api.main.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CommentResponse(

    @SerialName("pk")
    var pk: Int = 0,

    @SerialName("body")
    var body: String = "",

    @SerialName("username")
    var username: String? = null,

    @SerialName("date_created")
    var date_created: String? = null
)
