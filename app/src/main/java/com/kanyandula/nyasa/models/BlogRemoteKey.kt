package com.kanyandula.nyasa.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blog_remote_keys")
data class BlogRemoteKey(
    @PrimaryKey
    val queryKey: String,
    val nextPage: Int?,
    val lastUpdated: Long
)
