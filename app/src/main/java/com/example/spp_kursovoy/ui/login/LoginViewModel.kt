package com.example.spp_kursovoy.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.example.spp_kursovoy.data.LoginRepository
import com.example.spp_kursovoy.data.Result

import com.example.spp_kursovoy.R
import androidx.lifecycle.viewModelScope
import com.example.spp_kursovoy.data.network.ApiClient
import com.example.spp_kursovoy.data.network.dto.LoginRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginViewModel() : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.login(
                    LoginRequest(
                        email = username,
                        password = password
                    )
                )
                val userView = LoggedInUserView(
                    userId = response.user.id,
                    name = response.user.name,
                    email = response.user.email,
                    role = response.user.role
                )

                _loginResult.value = LoginResult(
                    success = LoginSuccess(
                        user = userView,
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken
                    )
                )

            } catch (e: HttpException) {
                _loginResult.value = LoginResult(
                    error = R.string.login_failed
                )
            } catch (e: Exception) {
                _loginResult.value = LoginResult(
                    error = R.string.network_error
                )
            }
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}