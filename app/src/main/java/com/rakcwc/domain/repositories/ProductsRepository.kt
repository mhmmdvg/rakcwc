package com.rakcwc.domain.repositories

import com.rakcwc.domain.models.HTTPResponse
import com.rakcwc.domain.models.ProductsResponse
import kotlinx.coroutines.flow.Flow

interface ProductsRepository {
    fun getProducts(): Flow<Result<HTTPResponse<ProductsResponse>>>
//    fun getProductDetail(id: String): Flow<Result<HTTPResponse<Products>>>
//    suspend fun createProduct(request: Products): Result<HTTPResponse<Products>>
}