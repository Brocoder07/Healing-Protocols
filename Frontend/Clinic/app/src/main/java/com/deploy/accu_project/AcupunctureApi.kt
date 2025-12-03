package com.deploy.accu_project

import retrofit2.http.GET
import retrofit2.http.Query

interface AcupunctureApi{
    @GET("/search")
    suspend fun searchAcupuncture(@Query("query") query: String): List <AcupunctureResponse>
}
