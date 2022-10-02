package com.kanyandula.nyasa.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Class for modeling the response when querying https://open-api.xyz/

 */
class BlogListSearchResponse(

    @SerializedName("results")
    @Expose
    var results: List<BlogSearchResponse>,

    @SerializedName("detail")
    @Expose
    var detail: String
) {

    override fun toString(): String {
        return "BlogListSearchResponse(results=$results, detail='$detail')"
    }
}