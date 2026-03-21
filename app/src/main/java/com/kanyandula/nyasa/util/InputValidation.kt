package com.kanyandula.nyasa.util

import android.util.Patterns

object InputValidation {

    fun validateEmail(email: String): String? {
        if (email.isBlank()) {
            return "Email is required."
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid email format."
        }
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.isBlank()) {
            return "Password is required."
        }
        if (password.length < 8) {
            return "Password must be at least 8 characters."
        }
        return null
    }

    fun validateLoginFields(email: String, password: String): String? {
        return validateEmail(email) ?: validatePassword(password)
    }

    fun validateRegistrationFields(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): String? {
        validateEmail(email)?.let { return it }
        if (username.isBlank()) {
            return "Username is required."
        }
        validatePassword(password)?.let { return it }
        if (password != confirmPassword) {
            return "Passwords must match."
        }
        return null
    }
}
