package com.kanyandula.nyasa.models

data class ProfileUpdateRequest(
    val bio: String? = null,
    val location: String? = null,
    val website: String? = null,
    val twitter: String? = null,
    val facebook: String? = null,
    val instagram: String? = null,
    val linkedin: String? = null
)
