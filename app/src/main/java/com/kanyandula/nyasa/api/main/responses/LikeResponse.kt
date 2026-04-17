package com.kanyandula.nyasa.api.main.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class LikeResponse(

    @SerialName("liked")
    var liked: Boolean,

    @SerialName("like_count")
    var like_count: Int
)
