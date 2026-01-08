package com.example.spp_kursovoy.data.network

import com.example.spp_kursovoy.data.auth.AuthStorage
import com.example.spp_kursovoy.data.network.interceptor.AuthInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


object ApiClient {
    private const val BASE_URL = "http://192.168.0.105:5000/"

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun create(authStorage: AuthStorage): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authStorage))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}