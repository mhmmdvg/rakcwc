package com.rakcwc.presentation.ui.screens.authentication

data class AuthState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isAuthenticated: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false
)