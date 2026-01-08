package com.example.spp_kursovoy.data.auth

import android.content.Context
import com.example.spp_kursovoy.ui.login.LoggedInUserView

class AuthStorage(context: Context) {
    private val prefs = context.getSharedPreferences(
        "auth_prefs",
        Context.MODE_PRIVATE
    )

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString("access_token", accessToken)
            .putString("refresh_token", refreshToken)
            .apply()
    }

    fun saveUser(user: LoggedInUserView) {
        prefs.edit()
            .putString("user_id", user.userId)
            .putString("name", user.name)
            .putString("email", user.email)
            .putString("role", user.role)
            .apply()
    }

    fun getAccessToken(): String? =
        prefs.getString("access_token", null)

    fun getRefreshToken(): String? =
        prefs.getString("refresh_token", null)

    fun getUser(): LoggedInUserView? {
        val id = prefs.getString("user_id", null) ?: return null
        val name = prefs.getString("name", null) ?: ""
        val email = prefs.getString("email", null) ?: ""
        val role = prefs.getString("role", null) ?: ""
        return LoggedInUserView(id, name, email, role)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}