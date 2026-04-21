package com.kanyandula.nyasa.api.main.responses

import com.kanyandula.nyasa.models.Category

fun CategoryResponse.toCategory(): Category = Category(
    pk = pk,
    name = name,
    slug = slug
)
