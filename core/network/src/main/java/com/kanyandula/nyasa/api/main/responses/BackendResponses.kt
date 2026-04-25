package com.kanyandula.nyasa.api.main.responses

/**
 * Sentinel value the backend returns in `BlogCreateUpdateResponse.response`
 * when the caller isn't authenticated — i.e. the create/update wasn't actually
 * performed. Callers must NOT persist the returned post in that case.
 */
const val RESPONSE_MUST_HAVE_NYASABLOG_USER = "Create a NyasaBlog Account"