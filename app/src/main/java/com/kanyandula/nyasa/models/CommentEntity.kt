package com.kanyandula.nyasa.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = BlogPost::class,
            parentColumns = ["slug"],
            childColumns = ["post_slug"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("post_slug")]
)
data class CommentEntity(
    @PrimaryKey
    @ColumnInfo(name = "pk")
    val pk: Int,
    @ColumnInfo(name = "post_slug")
    val postSlug: String,
    @ColumnInfo(name = "body")
    val body: String,
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "date_created")
    val dateCreated: Long
)
