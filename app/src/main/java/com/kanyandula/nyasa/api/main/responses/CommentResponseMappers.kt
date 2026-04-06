package com.kanyandula.nyasa.api.main.responses

import com.kanyandula.nyasa.models.Comment
import com.kanyandula.nyasa.util.DateUtils

fun CommentResponse.toComment(): Comment = Comment(
    pk = pk,
    body = body,
    username = username,
    dateCreated = DateUtils.convertServerStringDateToLong(date_created)
)
