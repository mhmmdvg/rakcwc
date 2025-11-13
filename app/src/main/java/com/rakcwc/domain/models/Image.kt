package com.rakcwc.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ImageUploadResponse(
    val imageUrl: String,
)

@Serializable
data class ImageDeleteRequest(val imageUrl: String)