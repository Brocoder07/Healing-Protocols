package com.deploy.accu_project

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    init {
        Log.d("RetrofitInstance", "Initializing Retrofit Singleton...")
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // Create the deserializer instance once
    private val deserializer = AcupunctureListDeserializer()

    private val gson = GsonBuilder()
        // Register for List interface
        .registerTypeAdapter(
            object : TypeToken<List<AcupunctureResponse>>() {}.type,
            deserializer
        )
        // SAFETY NET: Register for ArrayList implementation too
        .registerTypeAdapter(
            object : TypeToken<java.util.ArrayList<AcupunctureResponse>>() {}.type,
            deserializer
        )
        .create()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://fastapi-hosting.onrender.com")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val api: AcupunctureApi by lazy {
        retrofit.create(AcupunctureApi::class.java)
    }
}
