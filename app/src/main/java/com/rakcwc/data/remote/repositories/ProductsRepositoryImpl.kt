package com.rakcwc.data.remote.repositories

import com.rakcwc.data.remote.api.ProductsApi
import com.rakcwc.data.remote.local.dao.ProductsDao
import com.rakcwc.data.remote.local.mappers.toDomain
import com.rakcwc.data.remote.local.mappers.toEntity
import com.rakcwc.domain.models.HTTPResponse
import com.rakcwc.domain.models.Pagination
import com.rakcwc.domain.models.ProductsResponse
import com.rakcwc.domain.repositories.ProductsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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
            // Fetch from API
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
}