package com.rakcwc.data.remote.api

import com.rakcwc.domain.models.HTTPResponse
import com.rakcwc.domain.models.ProductsResponse
import retrofit2.Response
import retrofit2.http.GET

interface ProductsApi {
    @GET("products")
    suspend fun getProducts(): Response<HTTPResponse<ProductsResponse>>
}