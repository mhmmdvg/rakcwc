package com.rakcwc.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: Role,
    val imageUrl: String,
    val iat: Long,
    val exp: Long
)

@Serializable
data class Role(val id: String, val name: String)