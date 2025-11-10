package com.rakcwc.data.remote.api

import com.rakcwc.domain.models.HTTPResponse
import com.rakcwc.domain.models.ProductsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductsApi {
    @GET("products")
    suspend fun getProducts(): Response<HTTPResponse<ProductsResponse>>

    @GET("products")
    suspend fun searchProducts(@Query("search") search: String): Response<HTTPResponse<ProductsResponse>>
}