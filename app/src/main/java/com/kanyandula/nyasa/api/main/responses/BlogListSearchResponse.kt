package com.kanyandula.nyasa.api.main.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Class for modeling the response when querying https://open-api.xyz/

 */
@Serializable
class BlogListSearchResponse(

    @SerialName("results")
    var results: List<BlogSearchResponse> = emptyList(),

    @SerialName("detail")
    var detail: String = ""
) {

    override fun toString(): String {
        return "BlogListSearchResponse(results=$results, detail='$detail')"
    }
}
