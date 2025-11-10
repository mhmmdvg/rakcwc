package com.rakcwc.domain.repositories

import com.rakcwc.domain.models.AuthResponse
import com.rakcwc.domain.models.HTTPResponse

interface AuthenticationRepository {
    suspend fun signIn(email: String, password: String): Result<HTTPResponse<AuthResponse>>
}