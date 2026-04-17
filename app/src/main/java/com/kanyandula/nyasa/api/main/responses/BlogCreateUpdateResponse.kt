package com.kanyandula.nyasa.api.main.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("LongParameterList")
@Serializable
class BlogCreateUpdateResponse(

    @SerialName("response")
    var response: String,

    @SerialName("pk")
    override var pk: Int,

    @SerialName("title")
    override var title: String,

    @SerialName("slug")
    override var slug: String,

    @SerialName("body")
    override var body: String,

    @SerialName("image")
    override var image: String,

    @SerialName("date_updated")
    override var date_updated: String,

    @SerialName("username")
    override var username: String,

    @SerialName("category")
    override var category: CategoryResponse? = null,

    @SerialName("tags")
    override var tags: List<String>? = null,

    @SerialName("reading_time")
    override var reading_time: Int? = null,

    @SerialName("view_count")
    override var view_count: Int? = null,

    @SerialName("like_count")
    override var like_count: Int? = null,

    @SerialName("comment_count")
    override var comment_count: Int? = null,

    @SerialName("author_avatar")
    override var author_avatar: String? = null

) : BlogResponseFields
