package com.rakcwc.presentation.ui.screens.authentication

import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakcwc.data.remote.repositories.AuthenticationRepositoryImpl
import com.rakcwc.data.remote.resources.Resource
import com.rakcwc.domain.models.AuthResponse
import com.rakcwc.domain.models.HTTPResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthenticationRepositoryImpl
) : ViewModel() {
    private val _state = mutableStateOf(AuthState())
    val state: State<AuthState> = _state

    private val _authState = MutableStateFlow<Resource<HTTPResponse<AuthResponse>>>(Resource.Success(null))
    val authState: StateFlow<Resource<HTTPResponse<AuthResponse>>> = _authState.asStateFlow()

    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(
            email = email,
            emailError = null,
            generalError = null
        )
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(
            password = password,
            passwordError = null,
            generalError = null
        )
    }

    fun onPasswordVisibilityChange(isVisible: Boolean) {
        _state.value = _state.value.copy(isPasswordVisible = isVisible)
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Please enter a valid email address"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Password is required"
            password.length < 6 -> "Password must be at least 6 characters"
            else -> null
        }
    }

    fun signIn(email: String, password: String) {
        // Clear previous errors
        _state.value = _state.value.copy(
            emailError = null,
            passwordError = null,
            generalError = null
        )

        // Validate inputs
        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)

        if (emailError != null || passwordError != null) {
            _state.value = _state.value.copy(
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            _authState.value = Resource.Loading()

            try {
                authRepository.signIn(email = email, password = password).fold(
                    onSuccess = {
                        _state.value = _state.value.copy(
                            isAuthenticated = true,
                            isLoading = false
                        )
                        _authState.value = Resource.Success(it)
                    },
                    onFailure = {
                        val errorMessage = when {
                            it.message?.contains("401") == true ||
                                    it.message?.contains("unauthorized") == true ->
                                "Invalid email or password"
                            it.message?.contains("network") == true ->
                                "Network error. Please check your connection"
                            else -> it.message ?: "Login failed. Please try again"
                        }
                        _state.value = _state.value.copy(
                            generalError = errorMessage,
                            isLoading = false
                        )
                        _authState.value = Resource.Error(errorMessage)
                    }
                )
            } catch (error: Exception) {
                val errorMessage = error.message ?: "An unexpected error occurred"
                _state.value = _state.value.copy(
                    generalError = errorMessage,
                    isLoading = false
                )
                _authState.value = Resource.Error(errorMessage)
            }
        }
    }

    fun clearErrors() {
        _state.value = _state.value.copy(
            emailError = null,
            passwordError = null,
            generalError = null
        )
    }
}