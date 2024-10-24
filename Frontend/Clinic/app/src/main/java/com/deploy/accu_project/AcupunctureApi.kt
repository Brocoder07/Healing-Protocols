package com.deploy.accu_project

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

interface AcupunctureApi {
    @GET("/search")//search endpoint
    fun searchAcupuncture(@Query("query") query: String): Call<ResponseBody>
}
