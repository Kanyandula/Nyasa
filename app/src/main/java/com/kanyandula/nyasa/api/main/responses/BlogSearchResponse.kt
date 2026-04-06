package com.kanyandula.nyasa.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Suppress("LongParameterList")
class BlogSearchResponse(

    @SerializedName("pk")
    @Expose
    override var pk: Int,

    @SerializedName("title")
    @Expose
    override var title: String,

    @SerializedName("slug")
    @Expose
    override var slug: String,

    @SerializedName("body")
    @Expose
    override var body: String,

    @SerializedName("image")
    @Expose
    override var image: String,

    @SerializedName("date_updated")
    @Expose
    override var date_updated: String,

    @SerializedName("username")
    @Expose
    override var username: String,

    @SerializedName("category")
    @Expose
    override var category: String? = null,

    @SerializedName("tags")
    @Expose
    override var tags: List<String>? = null,

    @SerializedName("reading_time")
    @Expose
    override var reading_time: Int? = null,

    @SerializedName("view_count")
    @Expose
    override var view_count: Int? = null,

    @SerializedName("like_count")
    @Expose
    override var like_count: Int? = null

) : BlogResponseFields {
    override fun toString(): String {
        return "BlogSearchResponse(pk=$pk, title='$title', slug='$slug', " +
            "image='$image', date_updated='$date_updated', username='$username', " +
            "category=$category, reading_time=$reading_time, " +
            "view_count=$view_count, like_count=$like_count)"
    }
}
