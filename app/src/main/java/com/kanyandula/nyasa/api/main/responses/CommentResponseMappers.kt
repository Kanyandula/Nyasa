package com.kanyandula.nyasa.api.main.responses

import com.kanyandula.nyasa.models.Comment
import com.kanyandula.nyasa.util.DateUtils

fun CommentResponse.toComment(): Comment = Comment(
    pk = pk,
    body = body,
    username = username ?: "",
    dateCreated = date_created?.let { DateUtils.convertServerStringDateToLong(it) }
        ?: System.currentTimeMillis()
)
