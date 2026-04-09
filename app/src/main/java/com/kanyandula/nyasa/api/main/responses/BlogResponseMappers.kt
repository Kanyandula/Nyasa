package com.kanyandula.nyasa.api.main.responses

import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.util.DateUtils

fun BlogResponseFields.toBlogPost(): BlogPost = BlogPost(
    pk = pk,
    title = title,
    slug = slug,
    body = body,
    image = image,
    date_updated = DateUtils.convertServerStringDateToLong(date_updated),
    username = username,
    category = category?.name,
    tags = tags?.joinToString(","),
    reading_time = reading_time,
    view_count = view_count,
    like_count = like_count,
    comment_count = comment_count
)
