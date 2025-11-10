package com.rakcwc.presentation.ui.screens.home

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
class HomeViewModel @Inject constructor(
    private val catalogsRepository: CatalogsRepositoryImpl
) : ViewModel() {
    private val _catalogs = MutableStateFlow<Resource<HTTPResponse<List<CatalogsResponse>>>>(Resource.Loading())
    val catalogs: StateFlow<Resource<HTTPResponse<List<CatalogsResponse>>>> = _catalogs.asStateFlow()
    var fetchJob: Job? = null

    init {
        getCatalogs()
    }

    fun getCatalogs() {
        if (_catalogs.value is Resource.Success) {
            return
        }

        fetchJob?.cancel()

        fetchJob = viewModelScope.launch {
            if (_catalogs.value !is Resource.Success) {
                _catalogs.value = Resource.Loading()
            }

            try {
                catalogsRepository.getCatalogs().collect { result ->
                    result.fold(
                        onSuccess = {
                            Log.d("HomeViewModel", "getCatalogs: ${it.data}")
                            _catalogs.value = Resource.Success(it)
                        },
                        onFailure = {
                            _catalogs.value = Resource.Error(it.message ?: "An unknown error occurred")
                        }
                    )
                }
            } catch (error: Exception) {
                _catalogs.value = Resource.Error(error.message ?: "An unknown error occurred")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        fetchJob?.cancel()
    }
}

