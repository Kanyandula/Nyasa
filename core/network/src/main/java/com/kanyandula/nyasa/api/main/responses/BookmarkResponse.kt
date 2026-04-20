package com.kanyandula.nyasa.api.main.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class BookmarkResponse(

    @SerialName("bookmarked")
    var bookmarked: Boolean
)
