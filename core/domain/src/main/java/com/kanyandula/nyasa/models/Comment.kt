package com.kanyandula.nyasa.models

data class Comment(
    val pk: Int,
    val body: String,
    val username: String,
    val dateCreated: Long
)
