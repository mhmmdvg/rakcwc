package com.rakcwc.presentation.ui.screens.products

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakcwc.data.remote.repositories.CatalogsRepositoryImpl
import com.rakcwc.data.remote.resources.Resource
import com.rakcwc.domain.models.CatalogsResponse
import com.rakcwc.domain.models.HTTPResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val catalogRepository: CatalogsRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    private var currentCatalogId: String? = null
    private var fetchJob: Job? = null

    // Memory cache: Only page 1 per catalog+filter combo
    // LRU eviction after 10 entries
    private val memoryCache = object : LinkedHashMap<CacheKey, CachedPage1>(
        10,
        0.75f,
        true
    ) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<CacheKey, CachedPage1>?
        ): Boolean {
            return size > 10
        }
    }

    private val CACHE_VALIDITY_MS = 5 * 60 * 1000L // 5 minutes

    /**
     * Load catalog detail (page 1, unfiltered)
     * This is called when user opens a catalog from the list
     */
    fun getCatalogDetail(id: String, forceRefresh: Boolean = false) {
        val cacheKey = CacheKey(id, null)

        // Check memory cache first (unless force refresh)
        if (!forceRefresh) {
            val cached = memoryCache[cacheKey]
            if (cached != null) {
                val isCacheValid = (System.currentTimeMillis() - cached.timestamp) < CACHE_VALIDITY_MS

                if (isCacheValid) {
                    Log.d("ProductsViewModel", "Using memory cache for catalog $id")
                    currentCatalogId = id
                    updateUiStateFromResponse(cached.response)

                    // Optionally fetch fresh data in background
                    fetchInBackground(id)
                    return
                }
            }
        }

        // Skip if already loaded and not force refresh
        if (!forceRefresh && currentCatalogId == id && _uiState.value.products.isNotEmpty()) {
            Log.d("ProductsViewModel", "Catalog $id already loaded, skipping")
            return
        }

        // Load fresh data
        currentCatalogId = id
        _uiState.value = ProductsUiState(isLoading = true)

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            loadPage(id, page = 1, filter = null)
        }
    }

    /**
     * Apply or remove filter (resets to page 1)
     */
    fun applyFilter(filterName: String?) {
        val catalogId = currentCatalogId ?: return

        // If same filter, do nothing
        if (_uiState.value.selectedFilter == filterName) {
            return
        }

        Log.d("ProductsViewModel", "Applying filter: ${filterName ?: "none"}")

        // Check memory cache for this filter
        val cacheKey = CacheKey(catalogId, filterName)
        val cached = memoryCache[cacheKey]

        if (cached != null) {
            val isCacheValid = (System.currentTimeMillis() - cached.timestamp) < CACHE_VALIDITY_MS

            if (isCacheValid) {
                Log.d("ProductsViewModel", "Using memory cache for filtered data")
                updateUiStateFromResponse(cached.response, filterName)

                // Refresh in background
                fetchInBackground(catalogId, filterName)
                return
            }
        }

        // Load filtered data
        _uiState.value = _uiState.value.copy(
            selectedFilter = filterName,
            isLoading = true,
            products = emptyList(),
            currentPage = 1
        )

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            loadPage(catalogId, page = 1, filter = filterName)
        }
    }

    /**
     * Load next page (filter persists across pages)
     */
    fun loadNextPage() {
        val currentState = _uiState.value

        if (currentState.isLoadingMore || !currentState.hasNextPage) {
            Log.d("ProductsViewModel", "Cannot load more: isLoadingMore=${currentState.isLoadingMore}, hasNextPage=${currentState.hasNextPage}")
            return
        }

        val catalogId = currentCatalogId ?: return
        val nextPage = currentState.currentPage + 1

        Log.d("ProductsViewModel", "Loading page $nextPage with filter: ${currentState.selectedFilter}")

        _uiState.value = currentState.copy(isLoadingMore = true)

        viewModelScope.launch {
            loadPage(
                id = catalogId,
                page = nextPage,
                filter = currentState.selectedFilter, // Filter persists!
                isLoadingMore = true
            )
        }
    }

    /**
     * Core function to load data from repository
     */
    private suspend fun loadPage(
        id: String,
        page: Int,
        filter: String? = null,
        isLoadingMore: Boolean = false
    ) {
        try {
            catalogRepository.getCatalogDetail(
                id = id,
                filter = filter,
                page = page
            ).collect { result ->
                result.fold(
                    onSuccess = { response ->
                        val catalog = response.data ?: return@fold
                        val currentState = _uiState.value

                        // Append products if loading more, replace if page 1
                        val updatedProducts = if (isLoadingMore) {
                            currentState.products + (catalog.products ?: emptyList())
                        } else {
                            catalog.products ?: emptyList()
                        }

                        val newState = ProductsUiState(
                            products = updatedProducts,
                            filters = catalog.filters?.map {
                                FilterItem(it.name, it.count ?: 0)
                            } ?: emptyList(),
                            catalogName = catalog.name,
                            selectedFilter = filter,
                            currentPage = catalog.pagination?.currentPage ?: page,
                            hasNextPage = catalog.pagination?.hasNextPage ?: false,
                            isLoading = false,
                            isLoadingMore = false,
                            error = null
                        )

                        _uiState.value = newState

                        // Cache only page 1 in memory
                        if (page == 1) {
                            val cacheKey = CacheKey(id, filter)
                            memoryCache[cacheKey] = CachedPage1(response)
                            Log.d("ProductsViewModel", "Cached page 1 for catalog $id, filter: ${filter ?: "none"}")
                        }
                    },
                    onFailure = { error ->
                        Log.e("ProductsViewModel", "Load failed: ${error.message}")

                        // Only show error if no data to display
                        if (_uiState.value.products.isEmpty()) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                error = error.message ?: "An unknown error occurred"
                            )
                        } else {
                            // Silently fail if we already have data
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoadingMore = false
                            )
                        }
                    }
                )
            }
        } catch (error: Exception) {
            Log.e("ProductsViewModel", "Exception: ${error.message}")

            if (_uiState.value.products.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = error.message ?: "An unknown error occurred"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false
                )
            }
        }
    }

    /**
     * Fetch fresh data in background (silent refresh)
     */
    private fun fetchInBackground(id: String, filter: String? = null) {
        viewModelScope.launch {
            try {
                catalogRepository.getCatalogDetail(
                    id = id,
                    filter = filter,
                    page = 1
                ).collect { result ->
                    result.fold(
                        onSuccess = { response ->
                            // Only update if user is still viewing same catalog+filter
                            if (currentCatalogId == id && _uiState.value.selectedFilter == filter) {
                                // Don't update if we got stale cache from repository
                                if (response.message != "Stale cached data") {
                                    Log.d("ProductsViewModel", "Background refresh completed")
                                    updateUiStateFromResponse(response, filter)

                                    // Update cache
                                    val cacheKey = CacheKey(id, filter)
                                    memoryCache[cacheKey] = CachedPage1(response)
                                }
                            }
                        },
                        onFailure = {
                            Log.d("ProductsViewModel", "Background refresh failed (silently ignored)")
                        }
                    )
                }
            } catch (error: Exception) {
                Log.d("ProductsViewModel", "Background refresh exception (silently ignored)")
            }
        }
    }

    /**
     * Helper to update UI state from HTTP response
     */
    private fun updateUiStateFromResponse(
        response: HTTPResponse<CatalogsResponse>,
        filter: String? = null
    ) {
        val catalog = response.data ?: return

        _uiState.value = ProductsUiState(
            products = catalog.products ?: emptyList(),
            filters = catalog.filters?.map {
                FilterItem(it.name, it.count ?: 0)
            } ?: emptyList(),
            catalogName = catalog.name,
            selectedFilter = filter,
            currentPage = catalog.pagination?.currentPage ?: 1,
            hasNextPage = catalog.pagination?.hasNextPage ?: false,
            isLoading = false,
            isLoadingMore = false,
            error = null
        )
    }

    /**
     * Retry loading after error
     */
    fun retry() {
        currentCatalogId?.let { id ->
            val currentFilter = _uiState.value.selectedFilter
            if (currentFilter != null) {
                applyFilter(currentFilter)
            } else {
                getCatalogDetail(id, forceRefresh = true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        fetchJob?.cancel()

        // Debug: Log memory usage
        val totalProducts = memoryCache.values.sumOf {
            it.response.data?.products?.size ?: 0
        }
        Log.d("ProductsViewModel",
            "ViewModel cleared: ${memoryCache.size} cached entries, " +
                    "~$totalProducts total products, ~${totalProducts * 2}KB memory"
        )

        memoryCache.clear()
    }
}