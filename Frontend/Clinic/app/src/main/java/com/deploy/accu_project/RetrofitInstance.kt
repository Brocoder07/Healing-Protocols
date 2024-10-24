package com.deploy.accu_project

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://accu-project-nxoe.onrender.com")//Api URL (hosted website)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val api: AcupunctureApi by lazy {
        retrofit.create(AcupunctureApi::class.java)
    }
}