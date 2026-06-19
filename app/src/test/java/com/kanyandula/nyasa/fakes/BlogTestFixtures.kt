package com.kanyandula.nyasa.fakes

import com.kanyandula.nyasa.models.BlogPost

/** Shared [BlogPost] factory for ViewModel unit tests. */
fun createTestBlogPost(
    pk: Int = 1,
    title: String = "Test Blog",
    slug: String = "test-blog",
    body: String = "Test body",
    image: String = "https://example.com/image.jpg",
    dateUpdated: Long = 1000L,
    username: String = "testuser"
) = BlogPost(pk, title, slug, body, image, dateUpdated, username)
