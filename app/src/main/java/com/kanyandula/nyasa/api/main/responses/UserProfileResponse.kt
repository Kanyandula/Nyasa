package com.kanyandula.nyasa.api.main.responses

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.kanyandula.nyasa.models.UserProfile

class UserProfileResponse(

    @SerializedName("username")
    @Expose
    var username: String,

    @SerializedName("bio")
    @Expose
    var bio: String? = null,

    @SerializedName("location")
    @Expose
    var location: String? = null,

    @SerializedName("website")
    @Expose
    var website: String? = null,

    @SerializedName("twitter")
    @Expose
    var twitter: String? = null,

    @SerializedName("facebook")
    @Expose
    var facebook: String? = null,

    @SerializedName("instagram")
    @Expose
    var instagram: String? = null,

    @SerializedName("linkedin")
    @Expose
    var linkedin: String? = null,

    @SerializedName("profile_image")
    @Expose
    var profile_image: String? = null
)

fun UserProfileResponse.toUserProfile(): UserProfile = UserProfile(
    username = username,
    bio = bio,
    location = location,
    website = website,
    twitter = twitter,
    facebook = facebook,
    instagram = instagram,
    linkedin = linkedin,
    profileImage = profile_image
)
