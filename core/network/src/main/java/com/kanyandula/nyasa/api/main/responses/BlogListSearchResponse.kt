package com.kanyandula.nyasa.api.main.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DRF paginated list response from the `blog/list` endpoint.
 * `next` is the next-page URL or null on the last page.
 */
@Serializable
class BlogListSearchResponse(

    @SerialName("results")
    var results: List<BlogSearchResponse> = emptyList(),

    @SerialName("next")
    var next: String? = null,

    @SerialName("detail")
    var detail: String = ""
) {

    override fun toString(): String {
        return "BlogListSearchResponse(results=$results, next=$next, detail='$detail')"
    }
}
