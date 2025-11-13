package com.rakcwc.presentation.ui.screens.management

import com.rakcwc.domain.models.HTTPResponse

data class CachedPage(
    val response: HTTPResponse<List<Any>>,
    val timestamp: Long = System.currentTimeMillis()
)

data class CacheKey(
    val type: String
)