package com.kanyandula.nyasa.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LikeResponse(

    @SerializedName("liked")
    @Expose
    var liked: Boolean,

    @SerializedName("like_count")
    @Expose
    var like_count: Int
)
