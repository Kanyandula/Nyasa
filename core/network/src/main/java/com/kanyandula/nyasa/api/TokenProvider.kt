package com.kanyandula.nyasa.api

interface TokenProvider {
    val token: String?
    fun invalidate()
}
