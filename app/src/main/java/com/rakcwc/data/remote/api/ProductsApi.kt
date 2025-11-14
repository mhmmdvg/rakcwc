package com.rakcwc.data.remote.api

import com.rakcwc.domain.models.HTTPResponse
import com.rakcwc.domain.models.ProductRequest
import com.rakcwc.domain.models.Products
import com.rakcwc.domain.models.ProductsResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductsApi {
    @GET("products")
    suspend fun getProducts(): Response<HTTPResponse<ProductsResponse>>

    @GET("products")
    suspend fun searchProducts(@Query("search") search: String): Response<HTTPResponse<ProductsResponse>>

    @GET("products/{id}")
    suspend fun getProductDetail(@Path("id") id: String): Response<HTTPResponse<Products>>
    @POST("products")
    suspend fun createProduct(@Body request: ProductRequest): Response<HTTPResponse<Products>>
    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<HTTPResponse<Products>>
    @PATCH("products/{id}")
    suspend fun updateProduct(@Path("id") id: String, @Body request: ProductRequest): Response<HTTPResponse<Products>>

}