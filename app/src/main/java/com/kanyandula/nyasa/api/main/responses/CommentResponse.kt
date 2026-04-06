package com.kanyandula.nyasa.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CommentResponse(

    @SerializedName("pk")
    @Expose
    var pk: Int,

    @SerializedName("body")
    @Expose
    var body: String,

    @SerializedName("username")
    @Expose
    var username: String,

    @SerializedName("date_created")
    @Expose
    var date_created: String
)
