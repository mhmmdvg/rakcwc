package com.rakcwc.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class HTTPResponse<T>(
    val status: Boolean,
    val message: String,
    val data: T? = null
)