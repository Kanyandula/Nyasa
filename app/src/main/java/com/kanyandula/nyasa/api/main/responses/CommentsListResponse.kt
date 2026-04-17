package com.kanyandula.nyasa.api.main.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CommentsListResponse(

    @SerialName("results")
    var results: List<CommentResponse>,

    @SerialName("detail")
    var detail: String? = null
)
