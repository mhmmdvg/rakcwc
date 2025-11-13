package com.rakcwc.data.remote.api

import com.rakcwc.domain.models.HTTPResponse
import com.rakcwc.domain.models.ImageDeleteRequest
import com.rakcwc.domain.models.ImageUploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImageApi {
    @Multipart
    @POST("upload-image")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<HTTPResponse<ImageUploadResponse>>

    @POST("delete-image")
    suspend fun deleteImage(@Body request: ImageDeleteRequest): Response<HTTPResponse<String>>
}