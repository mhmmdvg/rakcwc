package com.rakcwc.presentation.ui.screens.createcatalog

import com.rakcwc.data.remote.resources.Resource

data class CreateCatalogState(
    val name: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val nameError: String? = null,
    val imageError: String? = null,
    val uploadState: Resource<String> = Resource.Loading(data = null),
    val uploadProgress: Float = 0f
)
