package com.kanyandula.nyasa.api.main.responses

import com.kanyandula.nyasa.models.Category
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CategoryResponse(

    @SerialName("pk")
    var pk: Int,

    @SerialName("name")
    var name: String,

    @SerialName("slug")
    var slug: String
)

fun CategoryResponse.toCategory(): Category = Category(
    pk = pk,
    name = name,
    slug = slug
)
