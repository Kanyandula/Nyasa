package com.kanyandula.nyasa.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BookmarkResponse(

    @SerializedName("bookmarked")
    @Expose
    var bookmarked: Boolean
)
