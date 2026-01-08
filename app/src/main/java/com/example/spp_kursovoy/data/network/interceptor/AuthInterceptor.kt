package com.example.spp_kursovoy.data.network.interceptor

import com.example.spp_kursovoy.data.auth.AuthStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val authStorage: AuthStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = authStorage.getAccessToken()

        val request = if (token != null) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        return chain.proceed(request)
    }
}
