package com.example.spp_kursovoy.data.network.dto

data class LoginResponse(val accessToken: String,
                         val refreshToken: String,
                         val user: UserDto)
