package com.kanyandula.nyasa.api.auth

import com.kanyandula.nyasa.api.auth.network_responses.LoginResponse
import com.kanyandula.nyasa.api.auth.network_responses.RegistrationResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface NyasaBlogApiAuthService {

    @POST("account/login")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @POST("account/register")
    @FormUrlEncoded
    suspend fun register(
        @Field("email") email: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("password2") password2: String
    ): Response<RegistrationResponse>
}
