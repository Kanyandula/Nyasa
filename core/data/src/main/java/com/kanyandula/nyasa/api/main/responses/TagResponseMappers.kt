package com.kanyandula.nyasa.api.main.responses

import com.kanyandula.nyasa.models.Tag

fun TagResponse.toTag(): Tag = Tag(
    pk = pk,
    name = name,
    slug = slug
)
