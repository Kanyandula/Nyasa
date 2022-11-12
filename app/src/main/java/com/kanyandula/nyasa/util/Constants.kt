package com.kanyandula.nyasa.util

class Constants {

    companion object{

        const val BASE_URL = "https://nyasablog.com/api/"
        const val PASSWORD_RESET_URL: String = "https://nyasablog.com/password_reset/"
        const val RESPONSE_MUST_HAVE_NYASABLOG_UER = "Create a NyasaBlog Account"

        const val NETWORK_TIMEOUT = 3000L
        const val TESTING_NETWORK_DELAY = 0L // fake network delay for testing
        const val TESTING_CACHE_DELAY = 0L // fake cache delay for testing

        const val PAGINATION_PAGE_SIZE = 10

        const val GALLERY_REQUEST_CODE = 201
        const val PERMISSIONS_REQUEST_READ_STORAGE: Int = 301
        const val CROP_IMAGE_INTENT_CODE: Int = 401
    }
}