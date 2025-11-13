package com.rakcwc.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ImageUploadResponse(
    val imageUrl: String,
)
