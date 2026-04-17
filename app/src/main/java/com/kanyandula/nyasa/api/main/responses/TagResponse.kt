package com.kanyandula.nyasa.api.main.responses

import com.kanyandula.nyasa.models.Tag
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class TagResponse(

    @SerialName("pk")
    var pk: Int,

    @SerialName("name")
    var name: String,

    @SerialName("slug")
    var slug: String
)

fun TagResponse.toTag(): Tag = Tag(
    pk = pk,
    name = name,
    slug = slug
)
