package com.kanyandula.nyasa.api.main

import com.kanyandula.nyasa.api.GenericResponse
import com.kanyandula.nyasa.api.main.responses.BlogCreateUpdateResponse
import com.kanyandula.nyasa.api.main.responses.BlogListSearchResponse
import com.kanyandula.nyasa.api.main.responses.BlogSearchResponse
import com.kanyandula.nyasa.api.main.responses.BookmarkResponse
import com.kanyandula.nyasa.api.main.responses.CategoryResponse
import com.kanyandula.nyasa.api.main.responses.CommentResponse
import com.kanyandula.nyasa.api.main.responses.CommentsListResponse
import com.kanyandula.nyasa.api.main.responses.LikeResponse
import com.kanyandula.nyasa.api.main.responses.TagResponse
import com.kanyandula.nyasa.api.main.responses.UserProfileResponse
import com.kanyandula.nyasa.models.AccountProperties
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

@Suppress("TooManyFunctions")
interface NyasaBlogApiMainService {

    @GET("account/properties")
    suspend fun getAccountProperties(): Response<AccountProperties>

    @PUT("account/properties/update")
    @FormUrlEncoded
    suspend fun saveAccountProperties(
        @Field("email") email: String,
        @Field("username") username: String
    ): Response<GenericResponse>

    @PUT("account/change_password/")
    @FormUrlEncoded
    suspend fun updatePassword(
        @Field("old_password") currentPassword: String,
        @Field("new_password") newPassword: String,
        @Field("confirm_new_password") confirmNewPassword: String
    ): Response<GenericResponse>

    @GET("account/profile/{username}/")
    suspend fun getProfile(
        @Path("username") username: String
    ): Response<UserProfileResponse>

    @PUT("account/profile/update/")
    @FormUrlEncoded
    suspend fun updateProfile(
        @Field("bio") bio: String?,
        @Field("location") location: String?,
        @Field("website") website: String?,
        @Field("twitter") twitter: String?,
        @Field("facebook") facebook: String?,
        @Field("instagram") instagram: String?,
        @Field("linkedin") linkedin: String?
    ): Response<UserProfileResponse>

    @GET("blog/categories/")
    suspend fun getCategories(): Response<List<CategoryResponse>>

    @GET("blog/tags/")
    suspend fun getTags(): Response<List<TagResponse>>

    @GET("blog/list")
    suspend fun searchListBlogPosts(
        @Query("search") query: String,
        @Query("ordering") ordering: String,
        @Query("page") page: Int,
        @Query("category") category: String? = null,
        @Query("status") status: String? = null
    ): Response<BlogListSearchResponse>

    @GET("blog/{slug}/")
    suspend fun getBlogPost(
        @Path("slug") slug: String
    ): Response<BlogSearchResponse>

    @GET("blog/{slug}/is_author")
    suspend fun isAuthorOfBlogPost(
        @Path("slug") slug: String
    ): Response<GenericResponse>

    @DELETE("blog/{slug}/delete")
    suspend fun deleteBlogPost(
        @Path("slug") slug: String
    ): Response<GenericResponse>

    @Multipart
    @PUT("blog/{slug}/update")
    suspend fun updateBlog(
        @Path("slug") slug: String,
        @Part("title") title: RequestBody,
        @Part("body") body: RequestBody,
        @Part image: MultipartBody.Part?,
        @Part("category") category: RequestBody? = null,
        @Part("tags") tags: RequestBody? = null
    ): Response<BlogCreateUpdateResponse>

    @POST("blog/{slug}/like/")
    suspend fun likeBlogPost(
        @Path("slug") slug: String
    ): Response<LikeResponse>

    @POST("blog/{slug}/bookmark/")
    suspend fun bookmarkBlogPost(
        @Path("slug") slug: String
    ): Response<BookmarkResponse>

    @GET("blog/bookmarks/")
    suspend fun getBookmarks(): Response<BlogListSearchResponse>

    @GET("blog/{slug}/comments/")
    suspend fun getComments(
        @Path("slug") slug: String
    ): Response<CommentsListResponse>

    @POST("blog/{slug}/comments/create/")
    @FormUrlEncoded
    suspend fun createComment(
        @Path("slug") slug: String,
        @Field("body") body: String
    ): Response<CommentResponse>

    @DELETE("blog/comments/{pk}/delete/")
    suspend fun deleteComment(
        @Path("pk") pk: Int
    ): Response<GenericResponse>

    @Multipart
    @POST("blog/create")
    suspend fun createBlog(
        @Part("title") title: RequestBody,
        @Part("body") body: RequestBody,
        @Part image: MultipartBody.Part?,
        @Part("category") category: RequestBody? = null,
        @Part("tags") tags: RequestBody? = null
    ): Response<BlogCreateUpdateResponse>
}
