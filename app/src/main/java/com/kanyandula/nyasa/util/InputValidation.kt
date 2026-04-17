package com.kanyandula.nyasa.util

import android.util.Patterns

object InputValidation {

    fun validateEmail(email: String): AppError? {
        if (email.isBlank()) {
            return AppError.Validation(mapOf("email" to "Email is required."))
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return AppError.Validation(mapOf("email" to "Invalid email format."))
        }
        return null
    }

    fun validatePassword(password: String): AppError? {
        if (password.isBlank()) {
            return AppError.Validation(mapOf("password" to "Password is required."))
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            return AppError.Validation(
                mapOf("password" to "Password must be at least $MIN_PASSWORD_LENGTH characters.")
            )
        }
        return null
    }

    fun validateLoginFields(email: String, password: String): AppError? {
        return validateEmail(email) ?: validatePassword(password)
    }

    fun validateRegistrationFields(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): AppError? {
        validateEmail(email)?.let { return it }
        if (username.isBlank()) {
            return AppError.Validation(mapOf("username" to "Username is required."))
        }
        validatePassword(password)?.let { return it }
        if (password != confirmPassword) {
            return AppError.Validation(
                mapOf("confirm_password" to "Passwords must match.")
            )
        }
        return null
    }

    private const val MIN_PASSWORD_LENGTH = 8
}
