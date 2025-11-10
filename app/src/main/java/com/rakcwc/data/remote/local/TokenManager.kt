package com.rakcwc.data.remote.local

import android.content.SharedPreferences
import android.util.Base64
import androidx.core.content.edit
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class TokenManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val KEY_TOKEN = "token"
    }

    fun saveToken(token: String) {
        sharedPreferences.edit { putString(KEY_TOKEN, token) }
    }

    fun getToken(): String? {
        val token = sharedPreferences.getString(KEY_TOKEN, "")

        if (token.isNullOrEmpty()) return null

        if (isTokenExpired(token)) {
            clearToken()
            return null
        }

        return token
    }

    private fun isTokenExpired(token: String): Boolean {
        try {
            val parts = token.split(".")

            if (parts.size != 3) return true

            val payload = parts[1]
            val decodedPayload = String(Base64.decode(payload, Base64.DEFAULT), StandardCharsets.UTF_8)
            val jsonObject = JSONObject(decodedPayload)
            val exp = jsonObject.optLong("exp", 0)

            if (exp == 0L) return true

            val currentTime = System.currentTimeMillis() / 1000

            return currentTime > exp
        } catch (error: Exception) {
            error.printStackTrace()
            return true
        }
    }

    fun clearToken() {
        sharedPreferences.edit { remove(KEY_TOKEN) }
    }
}