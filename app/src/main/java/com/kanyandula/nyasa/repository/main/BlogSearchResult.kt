package com.kanyandula.nyasa.repository.main

import com.kanyandula.nyasa.models.BlogPost

data class BlogSearchResult(
    val blogList: List<BlogPost>,
    val isQueryExhausted: Boolean
)
