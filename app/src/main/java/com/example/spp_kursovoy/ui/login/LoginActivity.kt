package com.example.spp_kursovoy.ui.login

import android.app.Activity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import com.example.spp_kursovoy.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

import com.example.spp_kursovoy.R
import com.example.spp_kursovoy.data.auth.AuthStorage
import com.example.spp_kursovoy.data.network.ApiClient
import com.example.spp_kursovoy.MainActivity
import androidx.lifecycle.lifecycleScope
import android.content.Intent
import android.util.Log
import com.example.spp_kursovoy.data.network.dto.RefreshRequest


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var authStorage: AuthStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authStorage = AuthStorage(applicationContext)


        val refreshToken = authStorage.getRefreshToken()
        val savedUser = authStorage.getUser()
        if (refreshToken != null && savedUser != null) {
            refreshAccessToken(refreshToken)
        } else {
            initLoginScreen()
        }

    }

    private fun initLoginScreen() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            loginResult.error?.let {
                showLoginFailed(it)
            }

            loginResult.success?.let {
                updateUiWithUser(it.user)
                onLoginSuccess(
                    user = it.user,
                    accessToken = it.accessToken,
                    refreshToken = it.refreshToken
                )
            }
            setResult(Activity.RESULT_OK)

            //Complete and destroy login activity once successful
            finish()
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString()
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                loginViewModel.login(username.text.toString(), password.text.toString())
            }
        }
    }


    private fun onLoginSuccess(user: LoggedInUserView, accessToken: String, refreshToken: String) {
        authStorage.saveTokens(accessToken, refreshToken)
        authStorage.saveUser(user)

        goToMain()
    }

    private fun refreshAccessToken(refreshToken: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.api.refresh(
                    RefreshRequest(refreshToken)
                )

                authStorage.saveTokens(
                    response.accessToken,
                    response.refreshToken
                )

                val savedUser = authStorage.getUser()
                if (savedUser != null) {
                    goToMain()
                } else {
                    initLoginScreen()
                }

            } catch (e: retrofit2.HttpException) {
                if (e.code() == 401) { // refresh token недействителен
                    authStorage.clear()
                } else {
                    // Любая другая ошибка
                    Toast.makeText(applicationContext, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
                }
                initLoginScreen()
            } catch (e: Exception) {
                authStorage.clear()
                initLoginScreen()
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }



    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.name
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}