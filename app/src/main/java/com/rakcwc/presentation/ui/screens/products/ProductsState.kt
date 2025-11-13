package com.rakcwc.presentation.ui.screens.products

import com.rakcwc.domain.models.CatalogsResponse
import com.rakcwc.domain.models.HTTPResponse
import com.rakcwc.domain.models.Products

data class ProductsUiState(
    val products: List<Products> = emptyList(),
    val filters: List<FilterItem> = emptyList(),
    val catalogName: String = "",
    val selectedFilter: String = "All",
    val currentPage: Int = 1,
    val hasNextPage: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null
)

data class FilterItem(
    val name: String,
    val count: Int
)

/**
 * Cache key for catalog + filter combination
 */
data class CacheKey(
    val catalogId: String,
    val filter: String? = null
)

/**
 * Cached page 1 data with timestamp
 */
data class CachedPage1(
    val response: HTTPResponse<CatalogsResponse>,
    val timestamp: Long = System.currentTimeMillis()
)