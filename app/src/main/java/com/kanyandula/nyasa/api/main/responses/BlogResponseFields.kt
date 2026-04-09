package com.kanyandula.nyasa.api.main.responses

interface BlogResponseFields {
    val pk: Int
    val title: String
    val slug: String
    val body: String
    val image: String
    val date_updated: String
    val username: String
    val category: CategoryResponse?
    val tags: List<String>?
    val reading_time: Int?
    val view_count: Int?
    val like_count: Int?
    val comment_count: Int?
}
