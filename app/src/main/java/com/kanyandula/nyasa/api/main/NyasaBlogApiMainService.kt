package com.kanyandula.nyasa.api.main

import androidx.lifecycle.LiveData
import com.kanyandula.nyasa.api.GenericResponse
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.util.GenericApiResponse
import retrofit2.http.*

interface NyasaBlogApiMainService {

    @GET("account/properties")
    fun getAccountProperties(
        @Header("Authorization") authorization: String
    ): LiveData<GenericApiResponse<AccountProperties>>


    @PUT("account/properties/update")
    @FormUrlEncoded
    fun saveAccountProperties(
        @Header("Authorization") authorization: String,
        @Field("email") email: String,
        @Field("username") username: String
    ): LiveData<GenericApiResponse<GenericResponse>>

}


