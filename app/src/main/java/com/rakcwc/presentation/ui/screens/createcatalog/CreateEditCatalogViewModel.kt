package com.rakcwc.presentation.ui.screens.createcatalog

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakcwc.data.remote.repositories.CatalogsRepositoryImpl
import com.rakcwc.data.remote.repositories.ImageUploadRepositoryImpl
import com.rakcwc.data.remote.resources.Resource
import com.rakcwc.domain.models.CatalogRequest
import com.rakcwc.domain.models.CatalogsResponse
import com.rakcwc.domain.models.HTTPResponse
import com.rakcwc.domain.models.ImageDeleteRequest
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
class CreateEditCatalogViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val catalogsRepository: CatalogsRepositoryImpl,
    private val imageRepository: ImageUploadRepositoryImpl
) : ViewModel() {

    private val _state = MutableStateFlow(CreateEditCatalogState())
    val state: StateFlow<CreateEditCatalogState> = _state.asStateFlow()

    private val _saveState = MutableStateFlow<Resource<HTTPResponse<CatalogsResponse>>>(Resource.Success(null))
    val saveState: StateFlow<Resource<HTTPResponse<CatalogsResponse>>> = _saveState.asStateFlow()

    private var currentCatalogId: String? = null
    private var originalImageUrl: String? = null

    init {
        _state.update { it.copy(uploadState = Resource.Success(null)) }
        _saveState.value = Resource.Success(null)
    }

    fun loadCatalog(catalogId: String) {
        currentCatalogId = catalogId

        viewModelScope.launch {
            _state.update { it.copy(isLoadingCatalog = true) }

            try {
                catalogsRepository.getCatalogDetail(catalogId, null, 1)
                    .collect { result ->
                        result.fold(
                            onSuccess = { response ->
                                response.data?.let { catalog ->
                                    originalImageUrl = catalog.imageUrl
                                    _state.update {
                                        it.copy(
                                            name = catalog.name ?: "",
                                            description = catalog.description ?: "",
                                            imageUrl = catalog.imageUrl,
                                            isLoadingCatalog = false
                                        )
                                    }
                                }
                            },
                            onFailure = { error ->
                                _state.update { it.copy(isLoadingCatalog = false) }
                                _saveState.value = Resource.Error(
                                    error.message ?: "Failed to load catalog"
                                )
                            }
                        )
                    }
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingCatalog = false) }
                _saveState.value = Resource.Error(
                    e.message ?: "Failed to load catalog"
                )
            }
        }
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

    fun onDescriptionChange(description: String) {
        _state.update {
            it.copy(description = description)
        }
        if (_saveState.value is Resource.Error) {
            _saveState.value = Resource.Success(null)
        }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
            val existingImageUrl = _state.value.imageUrl

            // Only delete if it's not the original image (for edit mode)
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
                Log.e("CreateEditVM", "Failed to delete old image: ${error.message}")
            }
        } catch (e: Exception) {
            Log.e("CreateEditVM", "Error deleting old image: ${e.message}")
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

    fun saveCatalog() {
        if (!validateForm()) return

        viewModelScope.launch {
            if (currentCatalogId != null) {
                // Edit mode
                updateCatalog()
            } else {
                // Create mode
                createCatalog()
            }
        }
    }

    private suspend fun createCatalog() {
        val tempId = "temp_${UUID.randomUUID()}"

        val optimisticCatalog = CatalogsResponse(
            id = tempId,
            name = _state.value.name,
            description = _state.value.description.ifBlank { "" },
            imageUrl = _state.value.imageUrl,
            products = emptyList(),
            filters = emptyList(),
            createdAt = System.currentTimeMillis().toString(),
            updatedAt = System.currentTimeMillis().toString(),
        )

        catalogsRepository.insertOptimisticCatalog(optimisticCatalog)

        _saveState.value = Resource.Loading()

        try {
            val request = CatalogRequest(
                name = _state.value.name,
                description = _state.value.description.ifBlank { "" },
                imageUrl = _state.value.imageUrl ?: ""
            )

            val result = catalogsRepository.createCatalog(request)

            result.fold(
                onSuccess = { response ->
                    catalogsRepository.removeOptimisticCatalog(tempId)
                    _saveState.value = Resource.Success(response)
                },
                onFailure = { error ->
                    catalogsRepository.removeOptimisticCatalog(tempId)
                    _saveState.value = Resource.Error(
                        error.message ?: "Failed to create catalog"
                    )
                }
            )
        } catch (e: Exception) {
            catalogsRepository.removeOptimisticCatalog(tempId)
            _saveState.value = Resource.Error(
                e.message ?: "An unexpected error occurred"
            )
        }
    }

    private suspend fun updateCatalog() {
        val catalogId = currentCatalogId ?: return

        // Optimistic update - update cache immediately (preserves products & filters)
        catalogsRepository.updateOptimisticCatalog(
            catalogId = catalogId,
            name = _state.value.name,
            description = _state.value.description.ifBlank { "" },
            imageUrl = _state.value.imageUrl
        )

        Log.d("CreateEditVM", "Optimistic update applied for catalog $catalogId")

        _saveState.value = Resource.Loading()

        try {
            val request = CatalogRequest(
                name = _state.value.name,
                description = _state.value.description.ifBlank { "" },
                imageUrl = _state.value.imageUrl ?: ""
            )

            val result = catalogsRepository.updateCatalog(catalogId, request)

            result.fold(
                onSuccess = { response ->
                    Log.d("CreateEditVM", "Server update successful for catalog $catalogId")
                    // Server update successful, real data will be cached by repository
                    _saveState.value = Resource.Success(response)
                },
                onFailure = { error ->
                    Log.e("CreateEditVM", "Server update failed, rolling back optimistic update")
                    // Rollback optimistic update by reloading from server
                    reloadCatalogOnError(catalogId)
                    _saveState.value = Resource.Error(
                        error.message ?: "Failed to update catalog"
                    )
                }
            )
        } catch (e: Exception) {
            Log.e("CreateEditVM", "Exception during update, rolling back", e)
            // Rollback optimistic update
            reloadCatalogOnError(catalogId)
            _saveState.value = Resource.Error(
                e.message ?: "An unexpected error occurred"
            )
        }
    }

    private suspend fun reloadCatalogOnError(catalogId: String) {
        try {
            // Force reload from API to rollback optimistic update
            catalogsRepository.getCatalogDetail(catalogId, null, 1)
                .collect { result ->
                    result.onSuccess { response ->
                        response.data?.let { catalog ->
                            Log.d("CreateEditVM", "Rollback successful, restored original data")
                            // This will restore the original catalog data
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("CreateEditVM", "Failed to rollback optimistic update: ${e.message}")
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (_state.value.name.isBlank()) {
            _state.update {
                it.copy(nameError = "Catalog name is required")
            }
            isValid = false
        }

        if (_state.value.imageUrl == null) {
            _state.update {
                it.copy(imageError = "Please upload a catalog image")
            }
            isValid = false
        }

        return isValid
    }
}