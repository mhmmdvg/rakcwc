package com.rakcwc.utils

import com.rakcwc.data.remote.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().apply {
            tokenManager.getToken()?.takeIf { it.isNotEmpty() }?.let {
                addHeader("Authorization", "Bearer $it")
            }
        }.build()

        return chain.proceed(request)
    }
}