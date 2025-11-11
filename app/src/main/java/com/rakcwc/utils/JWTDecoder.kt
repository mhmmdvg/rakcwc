package com.rakcwc.utils

import android.util.Base64
import com.rakcwc.domain.models.Role
import com.rakcwc.domain.models.UserInfo
import org.json.JSONObject

object JWTDecoder {
    fun decode(jwt: String): UserInfo? {
        return try {
            val parts = jwt.split(".")
            if (parts.size != 3) return null

            // Decode the payload (second part)
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP)
            val decodedString = String(decodedBytes)

            // Parse JSON
            val json = JSONObject(decodedString)
            val roleJson = json.getJSONObject("role")

            UserInfo(
                userId = json.getString("userId"),
                firstName = json.getString("firstName"),
                lastName = json.getString("lastName"),
                email = json.getString("email"),
                role = Role(
                    id = roleJson.getString("id"),
                    name = roleJson.getString("name")
                ),
                imageUrl = json.getString("imageUrl"),
                iat = json.getLong("iat"),
                exp = json.getLong("exp")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}