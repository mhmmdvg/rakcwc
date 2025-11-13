package com.rakcwc.presentation.ui.screens.management

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakcwc.data.remote.repositories.CatalogsRepositoryImpl
import com.rakcwc.data.remote.repositories.ProductsRepositoryImpl
import com.rakcwc.data.remote.resources.Resource
import com.rakcwc.domain.models.HTTPResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManagementViewModel @Inject constructor(
    private val catalogsRepository: CatalogsRepositoryImpl,
    private val productsRepository: ProductsRepositoryImpl
) : ViewModel() {
    private val CACHE_VALIDITY_MS = 5 * 60 * 1000L

    private val _managementState = MutableStateFlow<Resource<HTTPResponse<List<Any>>>>(Resource.Loading())
    val managementState: StateFlow<Resource<HTTPResponse<List<Any>>>> = _managementState.asStateFlow()

    private var fetchJob: Job? = null
    private var currentType: String? = null
    private val memoryCache = mutableMapOf<CacheKey, CachedPage>()

    fun getManagementData(type: String, forceRefresh: Boolean = false) {
        val cacheKey = CacheKey(type)

        // Check cache if not forcing refresh
        if (!forceRefresh) {
            val cached = memoryCache[cacheKey]
            if (cached != null) {
                val isCacheValid = (System.currentTimeMillis() - cached.timestamp) < CACHE_VALIDITY_MS

                if (isCacheValid) {
                    currentType = type
                    _managementState.value = Resource.Success(cached.response)
                    return
                }
            }
        }

        if (!forceRefresh && currentType == type) return


        // Cancel any ongoing fetch
        fetchJob?.cancel()

        _managementState.value = Resource.Loading()

        fetchJob = viewModelScope.launch {
            try {
                when {
                    "catalog" in type -> {
                        catalogsRepository.getCatalogs().collect { result ->
                            result.fold(
                                onSuccess = { response ->
                                    val anyResponse = HTTPResponse(
                                        data = response.data?.map { it as Any },
                                        message = response.message,
                                        status = response.status
                                    )

                                    memoryCache[cacheKey] = CachedPage(anyResponse)
                                    _managementState.value = Resource.Success(anyResponse)
                                },
                                onFailure = { error ->
                                    _managementState.value = Resource.Error(
                                        error.message ?: "An unexpected error occurred"
                                    )
                                }
                            )
                        }
                    }

                    "product" in type -> {
                        productsRepository.getProducts().collect { result ->
                            result.fold(
                                onSuccess = { response ->
                                    val anyResponse = HTTPResponse(
                                        data = response.data?.products?.map { it as Any },
                                        message = response.message,
                                        status = response.status
                                    )

                                    memoryCache[cacheKey] = CachedPage(anyResponse)
                                    _managementState.value = Resource.Success(anyResponse)
                                },
                                onFailure = { error ->
                                    _managementState.value = Resource.Error(
                                        error.message ?: "An unexpected error occurred"
                                    )
                                }
                            )
                        }
                    }

                    else -> {
                        _managementState.value = Resource.Error("Unknown management type: $type")
                    }
                }
            } catch (error: Exception) {
                _managementState.value = Resource.Error(
                    error.message ?: "No internet connection"
                )
            }
        }
    }

    fun refreshData() {
        currentType?.let { type ->
            getManagementData(type, forceRefresh = true)
        }
    }
}