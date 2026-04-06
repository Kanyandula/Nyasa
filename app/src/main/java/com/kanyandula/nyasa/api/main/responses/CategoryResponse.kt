package com.kanyandula.nyasa.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.kanyandula.nyasa.models.Category

class CategoryResponse(

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

fun CategoryResponse.toCategory(): Category = Category(
    pk = pk,
    name = name,
    slug = slug
)
