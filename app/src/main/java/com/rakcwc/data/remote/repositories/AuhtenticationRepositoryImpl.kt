package com.rakcwc.data.remote.repositories

import android.util.Log
import com.rakcwc.data.remote.api.AuthenticationApi
import com.rakcwc.data.remote.local.TokenManager
import com.rakcwc.domain.models.AuthRequest
import com.rakcwc.domain.models.AuthResponse
import com.rakcwc.domain.models.HTTPResponse
import com.rakcwc.domain.repositories.AuthenticationRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class AuthenticationRepositoryImpl @Inject constructor(
    private val authApi: AuthenticationApi,
    private val tokenManager: TokenManager
) : AuthenticationRepository {

    override suspend fun signIn(email: String, password: String): Result<HTTPResponse<AuthResponse>> {
        return runCatching {
            val response = authApi.signIn(request = AuthRequest(email, password))

            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Response body is null")
                tokenManager.saveToken(body.data?.token ?: "")
                body
            } else {
                val errorBody = response.errorBody()?.string()

                // Better error messages based on HTTP status codes
                val errorMessage = when (response.code()) {
                    400 -> "Invalid email or password format"
                    401 -> "Invalid credentials"
                    404 -> "Endpoint not found"
                    405 -> "Method not allowed - Check API endpoint configuration"
                    500 -> "Server error. Please try again later"
                    else -> errorBody ?: "Unknown error: ${response.code()}"
                }
                throw Exception(errorMessage)
            }
        }.onSuccess {
            Log.d("AuthRepo", "Sign in completed successfully")
        }.onFailure { error ->
            // Categorize errors for better logging
            when (error) {
                is HttpException -> {
                    Log.e("AuthRepo", "HTTP Exception: ${error.code()} - ${error.message()}", error)
                }
                is IOException -> {
                    Log.e("AuthRepo", "Network Error: ${error.message}", error)
                }
                else -> {
                    Log.e("AuthRepo", "Unexpected Error: ${error.message}", error)
                }
            }
        }
    }
}