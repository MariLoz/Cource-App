package com.example.spp_kursovoy.ui.login

import androidx.annotation.StringRes
/**
 * Data validation state of the login form.
 */
data class LoginFormState (val usernameError: Int? = null,
                      val passwordError: Int? = null,
                      val isDataValid: Boolean = false)