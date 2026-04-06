package com.kanyandula.nyasa.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CommentsListResponse(

    @SerializedName("results")
    @Expose
    var results: List<CommentResponse>,

    @SerializedName("detail")
    @Expose
    var detail: String? = null
)
