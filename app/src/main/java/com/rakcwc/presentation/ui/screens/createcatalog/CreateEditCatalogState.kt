package com.rakcwc.presentation.ui.screens.createcatalog

import com.rakcwc.data.remote.resources.Resource

data class CreateEditCatalogState(
    val name: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val uploadState: Resource<String?> = Resource.Success(null),
    val uploadProgress: Float = 0f,
    val nameError: String? = null,
    val imageError: String? = null,
    val isLoadingCatalog: Boolean = false
)