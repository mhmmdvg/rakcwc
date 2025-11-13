package com.rakcwc.data.remote.repositories

import android.util.Log
import com.rakcwc.data.remote.api.ImageApi
import com.rakcwc.domain.models.HTTPResponse
import com.rakcwc.domain.models.ImageDeleteRequest
import com.rakcwc.domain.models.ImageUploadResponse
import com.rakcwc.domain.repositories.ImageUploadRepository
import okhttp3.MultipartBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ImageUploadRepositoryImpl @Inject constructor(
    private val imageApi: ImageApi
) : ImageUploadRepository {
    override suspend fun uploadImage(image: MultipartBody.Part): Result<HTTPResponse<ImageUploadResponse>> {
        return try {
            val response = imageApi.uploadImage(image)

            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Response body is null")
                Log.d("ImageRepo", "Image uploaded successfully: ${body.data?.imageUrl}")
                Result.success(body)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = when (response.code()) {
                    400 -> "Bad Request - Invalid image format"
                    401 -> "Unauthorized"
                    413 -> "Image file too large"
                    415 -> "Unsupported media type"
                    500 -> "Server error. Please try again later"
                    else -> errorBody ?: "Unknown error: ${response.code()}"
                }

                Log.e("ImageRepo", "Upload failed: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (error: Exception) {
            when (error) {
                is HttpException -> {
                    Log.e("ImageRepo", "HTTP Exception: ${error.code()} - ${error.message()}", error)
                    Result.failure(Exception("Network error: ${error.message()}"))
                }

                is IOException -> {
                    Log.e("ImageRepo", "Network Error: ${error.message}", error)
                    Result.failure(Exception("Connection failed. Please check your internet connection"))
                }

                else -> {
                    Log.e("ImageRepo", "Unexpected Error: ${error.message}", error)
                    Result.failure(Exception(error.message ?: "Failed to upload image"))
                }
            }
        }
    }

    override suspend fun deleteImage(request: ImageDeleteRequest): Result<HTTPResponse<String>> {
        return runCatching {
            val response = imageApi.deleteImage(request)

            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Response body is null")
                body
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = when (response.code()) {
                    400 -> "Bad Request - Invalid image format"
                    401 -> "Unauthorized"
                    413 -> "Image file too large"
                    415 -> "Unsupported media type"
                    500 -> "Server error. Please try again later"
                    else -> errorBody ?: "Unknown error: ${response.code()}"
                }

                Log.e("ImageRepo", "Upload failed: $errorMessage")
                throw Exception(errorMessage)
            }
        }.onSuccess {
            Log.d("Image Delete", "Delete image successfully")
        }.onFailure { error ->
            when (error) {
                is HttpException -> {
                    Log.e("ImageRepo", "HTTP Exception: ${error.code()} - ${error.message()}", error)
                }

                is IOException -> {
                    Log.e("ImageRepo", "Network Error: ${error.message}", error)
                }

                else -> {
                    Log.e("ImageRepo", "Unexpected Error: ${error.message}", error)
                }
            }
        }
    }
}