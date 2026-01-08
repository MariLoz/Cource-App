package com.example.spp_kursovoy.ui.login

/**
 * User details post authentication that is exposed to the UI
 */
data class LoggedInUserView(
    val userId: String,
    val name: String,
    val email: String,
    val role: String
)