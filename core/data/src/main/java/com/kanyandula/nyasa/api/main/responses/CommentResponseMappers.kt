package com.kanyandula.nyasa.api.main.responses

import com.kanyandula.nyasa.models.Comment
import com.kanyandula.nyasa.models.CommentEntity
import com.kanyandula.nyasa.util.DateUtils

fun CommentResponse.toComment(): Comment = Comment(
    pk = pk,
    body = body,
    username = username ?: "",
    dateCreated = date_created?.let { DateUtils.convertServerStringDateToLong(it) }
        ?: System.currentTimeMillis()
)

fun CommentResponse.toEntity(postSlug: String): CommentEntity = CommentEntity(
    pk = pk,
    postSlug = postSlug,
    body = body,
    username = username ?: "",
    dateCreated = date_created?.let { DateUtils.convertServerStringDateToLong(it) }
        ?: System.currentTimeMillis()
)

fun CommentEntity.toComment(): Comment = Comment(
    pk = pk,
    body = body,
    username = username,
    dateCreated = dateCreated
)
