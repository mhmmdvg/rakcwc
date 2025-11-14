package com.rakcwc.domain.repositories

import com.rakcwc.domain.models.HTTPResponse
import com.rakcwc.domain.models.ProductRequest
import com.rakcwc.domain.models.Products
import com.rakcwc.domain.models.ProductsResponse
import kotlinx.coroutines.flow.Flow

interface ProductsRepository {
    fun getProducts(): Flow<Result<HTTPResponse<ProductsResponse>>>
    suspend fun searchProducts(search: String): Flow<Result<HTTPResponse<ProductsResponse>>>
    fun getProductDetail(id: String): Flow<Result<HTTPResponse<Products>>>
    suspend fun createProduct(request: ProductRequest): Result<HTTPResponse<Products>>
    suspend fun updateProduct(id: String, request: ProductRequest): Result<HTTPResponse<Products>>
    suspend fun deleteProduct(id: String): Result<HTTPResponse<Products>>
}