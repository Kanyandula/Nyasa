package com.kanyandula.nyasa.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


/**
 * Docs: https://nyasablog.com/api/
 */
@Parcelize
@Entity(tableName = "blog_post")
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
    var username: String


) : Parcelable {

    override fun toString(): String {
        return "BlogPost(pk=$pk, " +
                "title='$title', " +
                "slug='$slug', " +
                "image='$image', " +
                "date_updated=$date_updated, " +
                "username='$username')"
    }


}
