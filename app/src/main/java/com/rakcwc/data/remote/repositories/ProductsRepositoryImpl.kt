package com.rakcwc.data.remote.repositories

import android.util.Log
import com.rakcwc.data.remote.api.ProductsApi
import com.rakcwc.data.remote.local.dao.ProductsDao
import com.rakcwc.data.remote.local.mappers.toDomain
import com.rakcwc.data.remote.local.mappers.toEntity
import com.rakcwc.domain.models.HTTPResponse
import com.rakcwc.domain.models.Pagination
import com.rakcwc.domain.models.ProductRequest
import com.rakcwc.domain.models.Products
import com.rakcwc.domain.models.ProductsResponse
import com.rakcwc.domain.repositories.ProductsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ProductsRepositoryImpl @Inject constructor(
    private val productsApi: ProductsApi,
    private val productsDao: ProductsDao
) : ProductsRepository {

    private val _cacheValidityDuration = 15 * 60 * 1000L

    override fun getProducts(): Flow<Result<HTTPResponse<ProductsResponse>>> = flow {
        val products = productsDao.getProducts().first()
        val isCacheValid = products.isNotEmpty() &&
                products.first().cachedAt?.let {
                    (System.currentTimeMillis() - it) < _cacheValidityDuration
                } ?: false

        if (isCacheValid) {
            val cachedProducts = products.map { it.toDomain() }

            val productsResponse = ProductsResponse(
                products = cachedProducts,
                pagination = Pagination(
                    currentPage = 1,
                    totalPages = 1,
                    totalItems = cachedProducts.size,
                    itemsPerPage = cachedProducts.size,
                    hasNextPage = false,
                    hasPreviousPage = false
                )
            )

            val cachedResponse = HTTPResponse(
                status = true,
                message = "Cached data",
                data = productsResponse
            )

            emit(Result.success(cachedResponse))
        } else {
            try {
                val response = productsApi.getProducts()
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    apiResponse?.data?.let { productsResponse ->
                        val currentTime = System.currentTimeMillis()
                        val entitiesToCache = productsResponse.products.map { product ->
                            product.toEntity().copy(cachedAt = currentTime)
                        }
                        productsDao.insertProducts(entitiesToCache)
                        emit(Result.success(apiResponse))
                    }
                } else {
                    emit(Result.failure(Exception("API Error: ${response.code()}")))
                }
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        }
    }

    override suspend fun searchProducts(search: String): Flow<Result<HTTPResponse<ProductsResponse>>> = flow {
        try {
            val response = productsApi.searchProducts(search)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse != null) {
                    emit(Result.success(apiResponse))
                } else {
                    emit(Result.failure(Exception("Empty response body")))
                }
            } else {
                emit(Result.failure(Exception("API Error: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    override fun getProductDetail(id: String): Flow<Result<HTTPResponse<Products>>> = flow {
        val cachedProduct = productsDao.getProductById(id).first()

        val isCacheValid = cachedProduct?.cachedAt?.let {
            (System.currentTimeMillis() - it) < _cacheValidityDuration
        } ?: false

        if (cachedProduct != null && isCacheValid) {
            val cachedResponse = HTTPResponse(
                status = true,
                message = "Cached data",
                data = cachedProduct.toDomain()
            )
            emit(Result.success(cachedResponse))
            return@flow
        }

        try {
            val response = productsApi.getProductDetail(id)

            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Response body is null")

                body.data?.let { product ->
                    productsDao.insertProduct(
                        product.toEntity().copy(cachedAt = System.currentTimeMillis())
                    )
                }

                Log.d("ProductsRepo", "Product detail fetched successfully")
                emit(Result.success(body))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Bad Request"
                    401 -> "Unauthorized"
                    404 -> "Product not found"
                    500 -> "Server error"
                    else -> "Unknown error: ${response.code()}"
                }
                Log.e("ProductsRepo", "API Error: $errorMessage")
                emit(Result.failure(Exception(errorMessage)))
            }
        } catch (error: Exception) {
            when (error) {
                is HttpException -> {
                    Log.e("ProductsRepo", "HTTP Exception: ${error.code()} - ${error.message()}", error)
                }
                is IOException -> {
                    Log.e("ProductsRepo", "Network Error: ${error.message}", error)
                }
                else -> {
                    Log.e("ProductsRepo", "Unexpected Error: ${error.message}", error)
                }
            }
            emit(Result.failure(error))
        }
    }

    override suspend fun createProduct(request: ProductRequest): Result<HTTPResponse<Products>> {
        return try {
            val response = productsApi.createProduct(request)

            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Response body is null")
                val newProduct = body.data

                if (newProduct != null) {
                    productsDao.insertProduct(
                        newProduct.toEntity().copy(cachedAt = System.currentTimeMillis())
                    )
                }

                Log.d("ProductsRepo", "Product created and cache updated")
                Result.success(body)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Bad Request"
                    401 -> "Unauthorized"
                    500 -> "Server error"
                    else -> "Unknown error: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override suspend fun updateProduct(id: String, request: ProductRequest): Result<HTTPResponse<Products>> {
        return runCatching {
            val response = productsApi.updateProduct(id, request)

            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Response body is null")
                body
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Bad Request"
                    401 -> "Unauthorized"
                    404 -> "Product not found"
                    500 -> "Server error"
                    else -> "Unknown error: ${response.code()}"
                }
                throw Exception(errorMessage)
            }
        }.onSuccess {
            Log.d("ProductsRepo", "Product updated successfully")
        }.onFailure { error ->
            when (error) {
                is HttpException -> {
                    Log.e("ProductsRepo", "HTTP Exception: ${error.code()} - ${error.message()}", error)
                }
                is IOException -> {
                    Log.e("ProductsRepo", "Network Error: ${error.message}", error)
                }
                else -> {
                    Log.e("ProductsRepo", "Unexpected Error: ${error.message}", error)
                }
            }
        }
    }

    override suspend fun deleteProduct(id: String): Result<HTTPResponse<Products>> {
        val deletedProduct = productsDao.getProductById(id).first()
        productsDao.deleteProductById(id)

        return runCatching {
            val response = productsApi.deleteProduct(id)

            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Response body is null")
                Log.d("ProductsRepo", "Product deleted successfully")
                body
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Bad Request"
                    401 -> "Unauthorized"
                    404 -> "Product not found"
                    500 -> "Server error"
                    else -> "Unknown error: ${response.code()}"
                }
                throw Exception(errorMessage)
            }
        }.onFailure { error ->
            if (deletedProduct != null) {
                Log.e("ProductsRepo", "Delete failed, restoring product", error)
                productsDao.insertProduct(deletedProduct)
            }

            when (error) {
                is HttpException -> {
                    Log.e("ProductsRepo", "HTTP Exception: ${error.code()} - ${error.message()}", error)
                }
                is IOException -> {
                    Log.e("ProductsRepo", "Network Error: ${error.message}", error)
                }
                else -> {
                    Log.e("ProductsRepo", "Unexpected Error: ${error.message}", error)
                }
            }
        }
    }

    // Optimistic update methods
    suspend fun insertOptimisticProduct(product: Products) {
        val existingProduct = productsDao.getProductById(product.id).first()

        val productToInsert = if (existingProduct != null) {
            product.toEntity().copy(cachedAt = System.currentTimeMillis())
        } else {
            product.toEntity().copy(cachedAt = System.currentTimeMillis())
        }

        productsDao.insertProduct(productToInsert)
    }

    suspend fun updateOptimisticProduct(
        productId: String,
        name: String,
        variant: String,
        description: String?,
        pricePerUnit: Double,
        price: Double,
        imageUrl: String?,
        catalogId: String
    ) {
        val existingProduct = productsDao.getProductById(productId).first()

        if (existingProduct != null) {
            val updatedProduct = existingProduct.copy(
                name = name,
                variant = variant,
                description = description,
                pricePerUnit = pricePerUnit,
                price = price,
                imageUrl = imageUrl,
                catalogId = catalogId,
                cachedAt = System.currentTimeMillis()
            )
            productsDao.insertProduct(updatedProduct)
        }
    }

    suspend fun removeOptimisticProduct(productId: String) {
        productsDao.deleteProductById(productId)
    }
}