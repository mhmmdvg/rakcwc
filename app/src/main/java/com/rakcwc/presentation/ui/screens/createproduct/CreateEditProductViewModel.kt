package com.rakcwc.presentation.ui.screens.createproduct

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakcwc.data.remote.repositories.CatalogsRepositoryImpl
import com.rakcwc.data.remote.repositories.ImageUploadRepositoryImpl
import com.rakcwc.data.remote.repositories.ProductsRepositoryImpl
import com.rakcwc.data.remote.resources.Resource
import com.rakcwc.domain.models.CatalogsResponse
import com.rakcwc.domain.models.HTTPResponse
import com.rakcwc.domain.models.ImageDeleteRequest
import com.rakcwc.domain.models.ProductRequest
import com.rakcwc.domain.models.Products
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateEditProductViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val productsRepository: ProductsRepositoryImpl,
    private val imageRepository: ImageUploadRepositoryImpl,
    private val catalogsRepository: CatalogsRepositoryImpl
) : ViewModel() {

    private val _state = MutableStateFlow(CreateEditProductState())
    val state: StateFlow<CreateEditProductState> = _state.asStateFlow()

    private val _saveState = MutableStateFlow<Resource<HTTPResponse<Products>>>(Resource.Success(null))
    val saveState: StateFlow<Resource<HTTPResponse<Products>>> = _saveState.asStateFlow()

    private val _catalogsState = MutableStateFlow<Resource<List<CatalogsResponse>>>(Resource.Loading())
    val catalogsState: StateFlow<Resource<List<CatalogsResponse>>> = _catalogsState.asStateFlow()

    private var currentProductId: String? = null
    private var originalImageUrl: String? = null

    init {
        _state.update { it.copy(uploadState = Resource.Success(null)) }
        _saveState.value = Resource.Success(null)
        loadCatalogs()
    }

    private fun loadCatalogs() {
        viewModelScope.launch {
            _catalogsState.value = Resource.Loading()

            catalogsRepository.getCatalogs()
                .collect { result ->
                    result.fold(
                        onSuccess = { response ->
                            _catalogsState.value = Resource.Success(response.data ?: emptyList())
                        },
                        onFailure = { error ->
                            _catalogsState.value = Resource.Error(
                                error.message ?: "Failed to load catalogs"
                            )
                        }
                    )
                }
        }
    }

    fun loadProduct(productId: String) {
        currentProductId = productId

        viewModelScope.launch {
            _state.update { it.copy(isLoadingProduct = true) }

            try {
                productsRepository.getProductDetail(productId)
                    .collect { result ->
                        result.fold(
                            onSuccess = { response ->
                                response.data?.let { product ->
                                    originalImageUrl = product.imageUrl

                                    // Find catalog name from loaded catalogs
                                    val catalogName = (_catalogsState.value as? Resource.Success)
                                        ?.data
                                        ?.find { it.id == product.catalogId }
                                        ?.name ?: ""

                                    _state.update {
                                        it.copy(
                                            name = product.name ?: "",
                                            variant = product.variant ?: "",
                                            code = product.code ?: "",
                                            description = product.description ?: "",
                                            pricePerUnit = product.pricePerUnit.toString(),
                                            price = product.price.toString(),
                                            imageUrl = product.imageUrl,
                                            catalogId = product.catalogId,
                                            catalogName = catalogName,
                                            isLoadingProduct = false
                                        )
                                    }
                                }
                            },
                            onFailure = { error ->
                                _state.update { it.copy(isLoadingProduct = false) }
                                _saveState.value = Resource.Error(
                                    error.message ?: "Failed to load product"
                                )
                            }
                        )
                    }
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingProduct = false) }
                _saveState.value = Resource.Error(
                    e.message ?: "Failed to load product"
                )
            }
        }
    }

    fun setCatalog(catalogId: String, catalogName: String) {
        _state.update {
            it.copy(
                catalogId = catalogId,
                catalogName = catalogName,
                catalogError = null,
                showCatalogSelector = false
            )
        }
        if (_saveState.value is Resource.Error) {
            _saveState.value = Resource.Success(null)
        }
    }

    fun toggleCatalogSelector() {
        _state.update { it.copy(showCatalogSelector = !it.showCatalogSelector) }
    }

    fun onNameChange(name: String) {
        _state.update {
            it.copy(
                name = name,
                nameError = null
            )
        }
        if (_saveState.value is Resource.Error) {
            _saveState.value = Resource.Success(null)
        }
    }

    fun onVariantChange(variant: String) {
        _state.update {
            it.copy(
                variant = variant,
                variantError = null
            )
        }
        if (_saveState.value is Resource.Error) {
            _saveState.value = Resource.Success(null)
        }
    }

    fun onCodeChange(code: String) {
        _state.update {
            it.copy(
                code = code,
                codeError = null
            )
        }
        if (_saveState.value is Resource.Error) {
            _saveState.value = Resource.Success(null)
        }
    }

    fun onDescriptionChange(description: String) {
        _state.update { it.copy(description = description) }
        if (_saveState.value is Resource.Error) {
            _saveState.value = Resource.Success(null)
        }
    }

    fun onPricePerUnitChange(pricePerUnit: String) {
        _state.update {
            it.copy(
                pricePerUnit = pricePerUnit,
                pricePerUnitError = null
            )
        }
        if (_saveState.value is Resource.Error) {
            _saveState.value = Resource.Success(null)
        }
    }

    fun onPriceChange(price: String) {
        _state.update {
            it.copy(
                price = price,
                priceError = null
            )
        }
        if (_saveState.value is Resource.Error) {
            _saveState.value = Resource.Success(null)
        }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            val existingImageUrl = _state.value.imageUrl

            if (existingImageUrl != null && existingImageUrl != originalImageUrl) {
                deleteImage(existingImageUrl)
            }

            uploadImage(uri)
        }
    }

    private suspend fun uploadImage(uri: Uri) {
        _state.update {
            it.copy(
                uploadState = Resource.Loading(),
                uploadProgress = 0f,
                imageError = null
            )
        }

        try {
            val file = createFileFromUri(uri)
            if (file == null) {
                _state.update {
                    it.copy(
                        uploadState = Resource.Error("Failed to process image"),
                        uploadProgress = 0f,
                        imageError = "Failed to process image"
                    )
                }
                return
            }

            simulateUploadProgress()

            val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

            val result = imageRepository.uploadImage(body)

            result.fold(
                onSuccess = { response ->
                    _state.update {
                        it.copy(
                            imageUrl = response.data?.imageUrl,
                            uploadState = Resource.Success(response.data?.imageUrl),
                            uploadProgress = 1f,
                            imageError = null
                        )
                    }
                    file.delete()
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            uploadState = Resource.Error(error.message ?: "Failed to upload image"),
                            uploadProgress = 0f,
                            imageError = error.message ?: "Failed to upload image"
                        )
                    }
                    file.delete()
                }
            )
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    uploadState = Resource.Error(e.message ?: "Failed to upload image"),
                    uploadProgress = 0f,
                    imageError = e.message ?: "Failed to upload image"
                )
            }
        }
    }

    private suspend fun deleteImage(imageUrl: String) {
        try {
            val result = imageRepository.deleteImage(ImageDeleteRequest(imageUrl))
            result.onFailure { error ->
                Log.e("CreateEditProductVM", "Failed to delete old image: ${error.message}")
            }
        } catch (e: Exception) {
            Log.e("CreateEditProductVM", "Error deleting old image: ${e.message}")
        }
    }

    private suspend fun simulateUploadProgress() {
        for (i in 1..10) {
            delay(100)
            _state.update { it.copy(uploadProgress = i / 10f * 0.9f) }
        }
    }

    private fun createFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveProduct() {
        if (!validateForm()) return

        viewModelScope.launch {
            if (currentProductId != null) {
                updateProduct()
            } else {
                createProduct()
            }
        }
    }

    private suspend fun createProduct() {
        val tempId = "temp_${UUID.randomUUID()}"

        val optimisticProduct = Products(
            id = tempId,
            name = _state.value.name,
            variant = _state.value.variant,
            description = _state.value.description.ifBlank { null },
            pricePerUnit = _state.value.pricePerUnit.toDouble(),
            price = _state.value.price.toDouble(),
            imageUrl = _state.value.imageUrl,
            catalogId = _state.value.catalogId!!,
            code = _state.value.code,
            createdAt = System.currentTimeMillis().toString(),
            updatedAt = System.currentTimeMillis().toString()
        )

        productsRepository.insertOptimisticProduct(optimisticProduct)

        _saveState.value = Resource.Loading()

        try {
            val request = ProductRequest(
                name = _state.value.name,
                variant = _state.value.variant,
                code = _state.value.code,
                description = _state.value.description.ifBlank { null },
                catalogId = _state.value.catalogId!!,
                pricePerUnit = _state.value.pricePerUnit.toDouble(),
                price = _state.value.price.toDouble(),
                imageUrl = _state.value.imageUrl ?: ""
            )

            val result = productsRepository.createProduct(request)

            result.fold(
                onSuccess = { response ->
                    productsRepository.removeOptimisticProduct(tempId)
                    _saveState.value = Resource.Success(response)
                },
                onFailure = { error ->
                    productsRepository.removeOptimisticProduct(tempId)
                    _saveState.value = Resource.Error(
                        error.message ?: "Failed to create product"
                    )
                }
            )
        } catch (e: Exception) {
            productsRepository.removeOptimisticProduct(tempId)
            _saveState.value = Resource.Error(
                e.message ?: "An unexpected error occurred"
            )
        }
    }

    private suspend fun updateProduct() {
        val productId = currentProductId ?: return

        productsRepository.updateOptimisticProduct(
            productId = productId,
            name = _state.value.name,
            variant = _state.value.variant,
            description = _state.value.description.ifBlank { null },
            pricePerUnit = _state.value.pricePerUnit.toDouble(),
            price = _state.value.price.toDouble(),
            imageUrl = _state.value.imageUrl,
            catalogId = _state.value.catalogId!!
        )

        _saveState.value = Resource.Loading()

        try {
            val request = ProductRequest(
                name = _state.value.name,
                code = _state.value.code,
                variant = _state.value.variant,
                description = _state.value.description.ifBlank { null },
                catalogId = _state.value.catalogId!!,
                pricePerUnit = _state.value.pricePerUnit.toDouble(),
                price = _state.value.price.toDouble(),
                imageUrl = _state.value.imageUrl ?: ""
            )

            val result = productsRepository.updateProduct(productId, request)

            result.fold(
                onSuccess = { response ->
                    Log.d("CreateEditProductVM", "Server update successful for product $productId")
                    _saveState.value = Resource.Success(response)
                },
                onFailure = { error ->
                    Log.e("CreateEditProductVM", "Server update failed, rolling back optimistic update")
                    reloadProductOnError(productId)
                    _saveState.value = Resource.Error(
                        error.message ?: "Failed to update product"
                    )
                }
            )
        } catch (e: Exception) {
            Log.e("CreateEditProductVM", "Exception during update, rolling back", e)
            reloadProductOnError(productId)
            _saveState.value = Resource.Error(
                e.message ?: "An unexpected error occurred"
            )
        }
    }

    private suspend fun reloadProductOnError(productId: String) {
        try {
            productsRepository.getProductDetail(productId)
                .collect { result ->
                    result.onSuccess { response ->
                        response.data?.let { product ->
                            Log.d("CreateEditProductVM", "Rollback successful, restored original data")
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("CreateEditProductVM", "Failed to rollback optimistic update: ${e.message}")
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (_state.value.name.isBlank()) {
            _state.update { it.copy(nameError = "Product name is required") }
            isValid = false
        }

        if (_state.value.variant.isBlank()) {
            _state.update { it.copy(variantError = "Variant is required") }
            isValid = false
        }

        if (_state.value.code.isBlank()) {
            _state.update { it.copy(codeError = "Product code is required") }
            isValid = false
        }

        if (_state.value.pricePerUnit.isBlank()) {
            _state.update { it.copy(pricePerUnitError = "Price per unit is required") }
            isValid = false
        } else {
            try {
                val price = _state.value.pricePerUnit.toDouble()
                if (price <= 0) {
                    _state.update { it.copy(pricePerUnitError = "Price must be greater than 0") }
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                _state.update { it.copy(pricePerUnitError = "Invalid price format") }
                isValid = false
            }
        }

        if (_state.value.price.isBlank()) {
            _state.update { it.copy(priceError = "Price is required") }
            isValid = false
        } else {
            try {
                val price = _state.value.price.toDouble()
                if (price <= 0) {
                    _state.update { it.copy(priceError = "Price must be greater than 0") }
                    isValid = false
                }
            } catch (e: NumberFormatException) {
                _state.update { it.copy(priceError = "Invalid price format") }
                isValid = false
            }
        }

        if (_state.value.imageUrl == null) {
            _state.update { it.copy(imageError = "Please upload a product image") }
            isValid = false
        }

        if (_state.value.catalogId == null) {
            _state.update { it.copy(catalogError = "Please select a catalog") }
            isValid = false
        }

        return isValid
    }
}