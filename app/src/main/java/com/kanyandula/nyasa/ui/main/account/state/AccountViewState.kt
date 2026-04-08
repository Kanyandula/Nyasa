package com.kanyandula.nyasa.ui.main.account.state

import com.kanyandula.nyasa.models.AccountProperties

data class AccountViewState(
    val accountProperties: AccountProperties? = null
)

data class ProfileFormData(
    val email: String,
    val username: String,
    val bio: String,
    val location: String,
    val website: String,
    val twitter: String,
    val facebook: String,
    val instagram: String,
    val linkedin: String
)
