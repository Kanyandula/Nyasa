package com.kanyandula.nyasa.domain.model

import com.kanyandula.nyasa.models.BlogPost

data class BlogSearchResult(
    val blogList: List<BlogPost>,
    val isQueryExhausted: Boolean
)
