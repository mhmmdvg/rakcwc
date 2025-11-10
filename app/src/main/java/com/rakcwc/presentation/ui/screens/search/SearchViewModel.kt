package com.rakcwc.presentation.ui.screens.search

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakcwc.data.remote.repositories.ProductsRepositoryImpl
import com.rakcwc.data.remote.resources.Resource
import com.rakcwc.domain.models.HTTPResponse
import com.rakcwc.domain.models.ProductsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val productsRepository: ProductsRepositoryImpl
) : ViewModel() {

    private val _searchQuery = mutableStateOf(SearchState())
    val searchQuery: State<SearchState> = _searchQuery

    val products: StateFlow<Resource<HTTPResponse<ProductsResponse>>> =
        snapshotFlow { _searchQuery.value.query }
            .debounce(1500)
            .distinctUntilChanged()
            .flatMapLatest { query ->
                flow {
                    emit(Resource.Loading())

                    try {
                        val result = if (query.isBlank()) {
                            productsRepository.getProducts()
                        } else {
                            productsRepository.searchProducts(query)
                        }

                        result.collect { apiResult ->
                            apiResult.fold(
                                onSuccess = { emit(Resource.Success(it)) },
                                onFailure = { emit(Resource.Error(it.message ?: "An error occurred")) }
                            )
                        }
                    } catch (e: Exception) {
                        emit(Resource.Error(e.message ?: "An error occurred"))
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = Resource.Loading()
            )

    fun onSearchQueryChanged(search: String) {
        _searchQuery.value = _searchQuery.value.copy(query = search)
    }
}