package com.rakcwc.presentation.ui.screens.createproduct

import com.rakcwc.data.remote.resources.Resource

data class CreateEditProductState(
    val name: String = "",
    val variant: String = "",
    val code: String = "",
    val description: String = "",
    val pricePerUnit: String = "",
    val price: String = "",
    val imageUrl: String? = null,
    val catalogId: String? = null,
    val catalogName: String = "",

    // Validation errors
    val nameError: String? = null,
    val variantError: String? = null,
    val codeError: String? = null,
    val pricePerUnitError: String? = null,
    val priceError: String? = null,
    val imageError: String? = null,
    val catalogError: String? = null,

    // Upload state
    val uploadState: Resource<String?> = Resource.Success(null),
    val uploadProgress: Float = 0f,

    // Loading state
    val isLoadingProduct: Boolean = false,

    // Catalog selector
    val showCatalogSelector: Boolean = false
)