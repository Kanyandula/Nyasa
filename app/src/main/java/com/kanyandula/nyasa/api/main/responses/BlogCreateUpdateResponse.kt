package com.kanyandula.nyasa.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Suppress("LongParameterList")
class BlogCreateUpdateResponse(

    @SerializedName("response")
    @Expose
    var response: String,

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

) : BlogResponseFields
