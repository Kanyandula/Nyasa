package com.kanyandula.nyasa.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Docs: https://nyasablog.com/api/
 */
@Parcelize
@Entity(tableName = "blog_post", indices = [Index(value = ["slug"], unique = true)])
data class BlogPost(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "pk")
    var pk: Int,

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "slug")
    var slug: String,

    @ColumnInfo(name = "body")
    var body: String,

    @ColumnInfo(name = "image")
    var image: String,

    @ColumnInfo(name = "date_updated")
    var date_updated: Long,

    @ColumnInfo(name = "username")
    var username: String,

    @ColumnInfo(name = "category")
    var category: String? = null,

    @ColumnInfo(name = "tags")
    var tags: String? = null,

    @ColumnInfo(name = "reading_time")
    var reading_time: Int? = null,

    @ColumnInfo(name = "view_count")
    var view_count: Int? = null,

    @ColumnInfo(name = "like_count")
    var like_count: Int? = null,

    @ColumnInfo(name = "comment_count")
    var comment_count: Int? = null,

    @ColumnInfo(name = "author_avatar")
    var author_avatar: String? = null,

    @ColumnInfo(name = "is_featured")
    var is_featured: Boolean = false

) : Parcelable {

    override fun toString(): String {
        return "BlogPost(pk=$pk, " +
            "title='$title', " +
            "slug='$slug', " +
            "image='$image', " +
            "date_updated=$date_updated, " +
            "username='$username', " +
            "category=$category, " +
            "reading_time=$reading_time, " +
            "view_count=$view_count, " +
            "like_count=$like_count, " +
            "comment_count=$comment_count, " +
            "author_avatar=$author_avatar, " +
            "is_featured=$is_featured)"
    }
}
