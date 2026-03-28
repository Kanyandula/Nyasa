package com.kanyandula.nyasa.api.main.responses

import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.util.DateUtils

fun BlogSearchResponse.toBlogPost(): BlogPost = BlogPost(
    pk = pk,
    title = title,
    slug = slug,
    body = body,
    image = image,
    date_updated = DateUtils.convertServerStringDateToLong(date_updated),
    username = username
)

fun BlogCreateUpdateResponse.toBlogPost(): BlogPost = BlogPost(
    pk = pk,
    title = title,
    slug = slug,
    body = body,
    image = image,
    date_updated = DateUtils.convertServerStringDateToLong(date_updated),
    username = username
)
