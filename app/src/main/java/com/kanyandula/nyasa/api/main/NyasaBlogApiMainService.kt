package com.kanyandula.nyasa.api.main

import com.kanyandula.nyasa.api.GenericResponse
import com.kanyandula.nyasa.api.main.responses.BlogCreateUpdateResponse
import com.kanyandula.nyasa.api.main.responses.BlogListSearchResponse
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

    @GET("blog/list")
    suspend fun searchListBlogPosts(
        @Query("search") query: String,
        @Query("ordering") ordering: String,
        @Query("page") page: Int
    ): Response<BlogListSearchResponse>

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
        @Part image: MultipartBody.Part?
    ): Response<BlogCreateUpdateResponse>

    @Multipart
    @POST("blog/create")
    suspend fun createBlog(
        @Part("title") title: RequestBody,
        @Part("body") body: RequestBody,
        @Part image: MultipartBody.Part?
    ): Response<BlogCreateUpdateResponse>
}
