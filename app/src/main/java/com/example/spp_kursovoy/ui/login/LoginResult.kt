package com.example.spp_kursovoy.ui.login

/**
 * Authentication result : success (user details) or error message.
 */
data class LoginSuccess(
    val user: LoggedInUserView,
    val accessToken: String,
    val refreshToken: String
)

data class LoginResult (
    val success: LoginSuccess? = null,
     val error:Int? = null
)