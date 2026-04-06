package com.kanyandula.nyasa.models

data class UserProfile(
    val username: String,
    val bio: String?,
    val location: String?,
    val website: String?,
    val twitter: String?,
    val facebook: String?,
    val instagram: String?,
    val linkedin: String?,
    val profileImage: String?
)
