package com.kanyandula.nyasa.api.main

import androidx.lifecycle.LiveData
import com.kanyandula.nyasa.models.AccountProperties
import com.kanyandula.nyasa.util.GenericApiResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface NyasaBlogApiMainService {

    @GET("account/properties")
    fun getAccountProperties(
        @Header("Authorization") authorization: String
    ): LiveData<GenericApiResponse<AccountProperties>>

}


