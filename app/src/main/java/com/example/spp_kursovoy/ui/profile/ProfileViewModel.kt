package com.example.spp_kursovoy.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spp_kursovoy.data.auth.AuthStorage
import com.example.spp_kursovoy.data.network.ApiClient
import com.example.spp_kursovoy.data.network.dto.UserProfileDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authStorage: AuthStorage
) : ViewModel() {

    private val api = ApiClient.create(authStorage)

    private val _profile = MutableStateFlow<UserProfileDto?>(null)
    val profile: StateFlow<UserProfileDto?> = _profile

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadProfile() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                _profile.value = api.getMyProfile()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to load profile", e)
                _error.value = "Не удалось загрузить профиль"
            } finally {
                _loading.value = false
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val refreshToken = authStorage.getRefreshToken() ?: ""
                if (refreshToken.isNotBlank()) {
                    // Передаём Map, как ожидает Retrofit
                    api.logout(mapOf("refreshToken" to refreshToken))
                }
                // Очистка токенов
                authStorage.clear()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Logout failed", e)
            } finally {
                onComplete()
            }
        }
    }
}
