package com.rakcwc.presentation.ui.screens.detailproduct

import DetailProductState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakcwc.data.remote.repositories.ProductsRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class DetailProductViewModel @Inject constructor(
    private val productsRepository: ProductsRepositoryImpl,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(DetailProductState())
    val state: State<DetailProductState> = _state

    init {
        savedStateHandle.get<String>("id")?.let { productId ->
            getProductDetail(productId)
        }
    }

    private fun getProductDetail(productId: String) {
        productsRepository.getProductDetail(productId).onEach { result ->
            result.fold(
                onSuccess = { response ->
                    _state.value = _state.value.copy(
                        product = response.data,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        product = null,
                        isLoading = false,
                        error = error.message ?: "Unknown error occurred"
                    )
                }
            )
        }.launchIn(viewModelScope)

        _state.value = _state.value.copy(isLoading = true, error = null)
    }

    fun increaseQuantity() {
        _state.value = _state.value.copy(
            quantity = _state.value.quantity + 1
        )
    }

    fun decreaseQuantity() {
        if (_state.value.quantity > 1) {
            _state.value = _state.value.copy(
                quantity = _state.value.quantity - 1
            )
        }
    }

    fun toggleFavorite() {
        _state.value = _state.value.copy(
            isFavorite = !_state.value.isFavorite
        )
    }

    fun addToCart() {
        // Implement add to cart logic
        val product = _state.value.product
        val quantity = _state.value.quantity
        // TODO: Call cart repository to add product
    }

    fun buyNow() {
        // Implement buy now logic
        val product = _state.value.product
        val quantity = _state.value.quantity
        // TODO: Navigate to checkout
    }
}