package com.rakcwc.presentation.ui.screens.createcatalog

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rakcwc.data.remote.repositories.CatalogsRepositoryImpl
import com.rakcwc.data.remote.repositories.ImageUploadRepositoryImpl
import com.rakcwc.data.remote.resources.Resource
import com.rakcwc.domain.models.CatalogRequest
import com.rakcwc.domain.models.CatalogsResponse
import com.rakcwc.domain.models.HTTPResponse
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
import javax.inject.Inject

@HiltViewModel
class CreateCatalogViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val catalogsRepository: CatalogsRepositoryImpl,
    private val imageRepository: ImageUploadRepositoryImpl
) : ViewModel() {

    private val _state = MutableStateFlow(CreateCatalogState())
    val state: StateFlow<CreateCatalogState> = _state.asStateFlow()

    private val _createState = MutableStateFlow<Resource<HTTPResponse<CatalogsResponse>>>(Resource.Success(null))
    val createState: StateFlow<Resource<HTTPResponse<CatalogsResponse>>> = _createState.asStateFlow()

    init {
        // Initialize with success state (not loading)
        _state.update { it.copy(uploadState = Resource.Success(null)) }
        _createState.value = Resource.Success(null)
    }

    fun onNameChange(name: String) {
        _state.update {
            it.copy(
                name = name,
                nameError = null
            )
        }
        // Reset create state error when user types
        if (_createState.value is Resource.Error) {
            _createState.value = Resource.Success(null)
        }
    }

    fun onDescriptionChange(description: String) {
        _state.update {
            it.copy(description = description)
        }
        // Reset create state error when user types
        if (_createState.value is Resource.Error) {
            _createState.value = Resource.Success(null)
        }
    }

    fun onImageSelected(uri: Uri) {
        viewModelScope.launch {
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

            // Simulate progress
            simulateUploadProgress()

            // Explicitly set the MIME type to image/jpeg
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

    fun createCatalog() {
        if (!validateForm()) return

        viewModelScope.launch {
            _createState.value = Resource.Loading()

            try {
                val request = CatalogRequest(
                    name = _state.value.name,
                    description = _state.value.description.ifBlank { "" },
                    imageUrl = _state.value.imageUrl ?: ""
                )

                val result = catalogsRepository.createCatalog(request)

                result.fold(
                    onSuccess = { response ->
                        _createState.value = Resource.Success(response)
                    },
                    onFailure = { error ->
                        _createState.value = Resource.Error(
                            error.message ?: "Failed to create catalog"
                        )
                    }
                )
            } catch (e: Exception) {
                _createState.value = Resource.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
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