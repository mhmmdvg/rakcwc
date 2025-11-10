package com.rakcwc.presentation.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakcwc.data.remote.repositories.ProductsRepositoryImpl
import com.rakcwc.data.remote.resources.Resource
import com.rakcwc.domain.models.HTTPResponse
import com.rakcwc.domain.models.ProductsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val productsRepository: ProductsRepositoryImpl
) : ViewModel() {
    private val _products = MutableStateFlow<Resource<HTTPResponse<ProductsResponse>>>(Resource.Loading())
    val products: StateFlow<Resource<HTTPResponse<ProductsResponse>>> = _products.asStateFlow()
    var fetchJob: Job? = null

    init {
        getProducts()
    }

    fun getProducts() {
        if (_products.value is Resource.Success) return

        fetchJob?.cancel()

        fetchJob = viewModelScope.launch {
            if (_products.value !is Resource.Success) {
                _products.value = Resource.Loading()

            }

            try {
                productsRepository.getProducts().collect { result ->
                    result.fold(
                        onSuccess = {
                            _products.value = Resource.Success(it)
                        },
                        onFailure = {
                            _products.value = Resource.Error(it.message ?: "An unexpected error occurred")
                        }
                    )
                }
            } catch (error: Exception) {
                _products.value = Resource.Error(error.message ?: "An unknown error occurred")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        fetchJob?.cancel()
    }
}