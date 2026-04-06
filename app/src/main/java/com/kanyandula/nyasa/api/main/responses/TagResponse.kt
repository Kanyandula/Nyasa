package com.kanyandula.nyasa.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.kanyandula.nyasa.models.Tag

class TagResponse(

    @SerializedName("pk")
    @Expose
    var pk: Int,

    @SerializedName("name")
    @Expose
    var name: String,

    @SerializedName("slug")
    @Expose
    var slug: String
)

fun TagResponse.toTag(): Tag = Tag(
    pk = pk,
    name = name,
    slug = slug
)
