package com.rakcwc.data.remote.api

import com.rakcwc.domain.models.AuthRequest
import com.rakcwc.domain.models.AuthResponse
import com.rakcwc.domain.models.HTTPResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthenticationApi {
    @POST("auth/signin")
    suspend fun signIn(@Body request: AuthRequest): Response<HTTPResponse<AuthResponse>>
}