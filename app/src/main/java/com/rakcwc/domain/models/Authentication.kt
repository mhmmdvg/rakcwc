package com.rakcwc.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(
    val email: String,
    val password: String,
)

@Serializable
data class AuthResponse(
    val user: AuthUser,
    val token: String
)

@Serializable
data class AuthUser(
    val id: String,
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val imageUrl: String? = null,
    val roleId: String,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
)