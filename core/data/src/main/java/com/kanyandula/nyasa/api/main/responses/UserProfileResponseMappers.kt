package com.kanyandula.nyasa.api.main.responses

import com.kanyandula.nyasa.models.UserProfile

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
