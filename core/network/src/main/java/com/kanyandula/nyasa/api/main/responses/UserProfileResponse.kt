package com.kanyandula.nyasa.api.main.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class UserProfileResponse(

    @SerialName("username")
    var username: String,

    @SerialName("bio")
    var bio: String? = null,

    @SerialName("location")
    var location: String? = null,

    @SerialName("website")
    var website: String? = null,

    @SerialName("twitter")
    var twitter: String? = null,

    @SerialName("facebook")
    var facebook: String? = null,

    @SerialName("instagram")
    var instagram: String? = null,

    @SerialName("linkedin")
    var linkedin: String? = null,

    @SerialName("profile_image")
    var profile_image: String? = null
)
