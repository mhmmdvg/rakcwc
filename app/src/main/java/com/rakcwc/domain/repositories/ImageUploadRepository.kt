package com.rakcwc.domain.repositories

import com.rakcwc.domain.models.HTTPResponse
import com.rakcwc.domain.models.ImageDeleteRequest
import com.rakcwc.domain.models.ImageUploadResponse
import okhttp3.MultipartBody

interface ImageUploadRepository {
    suspend fun uploadImage(image: MultipartBody.Part): Result<HTTPResponse<ImageUploadResponse>>
    suspend fun deleteImage(request: ImageDeleteRequest): Result<HTTPResponse<String>>
}