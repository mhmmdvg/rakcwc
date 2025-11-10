package com.rakcwc.presentation.ui.screens.products

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
    private val _catalogDetail = MutableStateFlow<Resource<HTTPResponse<CatalogsResponse>>>(Resource.Loading())
    val catalogDetail: StateFlow<Resource<HTTPResponse<CatalogsResponse>>> = _catalogDetail.asStateFlow()
    private var currentCatalogId: String? = null
    private val catalogStates = mutableMapOf<String, Resource<HTTPResponse<CatalogsResponse>>>()
    private var fetchJob: Job? = null

    fun getCatalogDetail(id: String, forceRefresh: Boolean = false) {
        if (currentCatalogId == id && _catalogDetail.value is Resource.Success) return

        currentCatalogId = id

        val cachedState = catalogStates[id]
        if (cachedState != null && cachedState is Resource.Success && !forceRefresh) {
            _catalogDetail.value = cachedState
            fetchInBackground(id)
            return
        }

        _catalogDetail.value = Resource.Loading()

        fetchJob?.cancel()

        fetchJob = viewModelScope.launch {
            try {
                catalogRepository.getCatalogDetail(id).collect { result ->
                    result.fold(
                        onSuccess = {
                            val successState = Resource.Success(it)
                            _catalogDetail.value = Resource.Success(it)

                            if (it.message != "Stale cached data") {
                                catalogStates[id] = successState
                            }
                        },
                        onFailure = {
                            _catalogDetail.value = Resource.Error(it.message ?: "An unknown error occurred")
                        }
                    )
                }
            } catch (error: Exception) {
                _catalogDetail.value = Resource.Error(error.message ?: "An unknown error occurred")
            }
        }
    }

    private fun fetchInBackground(id: String) {
        viewModelScope.launch {
            try {
                catalogRepository.getCatalogDetail(id).collect { result ->
                    result.fold(
                        onSuccess = { response ->
                            // Only update if it's fresh data
                            if (response.message == "Fresh data") {
                                val successState = Resource.Success(response)
                                catalogStates[id] = successState

                                // Update UI only if still viewing this catalog
                                if (currentCatalogId == id) {
                                    _catalogDetail.value = successState
                                }
                            }
                        },
                        onFailure = { /* Silently fail background refresh */ }
                    )
                }
            } catch (error: Exception) {
                // Silently fail background refresh
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        fetchJob?.cancel()
    }

}