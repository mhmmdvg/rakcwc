package com.rakcwc.data.remote.repositories

import android.util.Log
import com.rakcwc.data.remote.api.CatalogsApi
import com.rakcwc.data.remote.local.dao.CatalogDao
import com.rakcwc.data.remote.local.dao.ProductsDao
import com.rakcwc.data.remote.local.mappers.toDomain
import com.rakcwc.data.remote.local.mappers.toEntity
import com.rakcwc.domain.models.CatalogRequest
import com.rakcwc.domain.models.CatalogsResponse
import com.rakcwc.domain.models.HTTPResponse
import com.rakcwc.domain.repositories.CatalogsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CatalogsRepositoryImpl @Inject constructor(
    private val catalogsApi: CatalogsApi,
    private val catalogDao: CatalogDao,
    private val productsDao: ProductsDao
) : CatalogsRepository {

    private val _cacheValidityDuration = 30 * 60 * 1000L

    override fun getCatalogs(): Flow<Result<HTTPResponse<List<CatalogsResponse>>>> = flow {
        val entities = catalogDao.getCatalogs().first()
        val isCacheValid =
            entities.isNotEmpty() && (System.currentTimeMillis() - entities.first().cachedAt) < _cacheValidityDuration

        if (isCacheValid) {
            val cachedCatalogs = entities.map { it.toDomain() }
            val cachedResponse = HTTPResponse(
                status = true,
                message = "Cached data",
                data = cachedCatalogs
            )

            emit(Result.success(cachedResponse))
            return@flow
        } else {
            Log.d("CatalogsRepo", "No cached data, fetching from API only")
        }


        try {
            val response = catalogsApi.getCatalogs()

            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Response body is null")

                catalogDao.deleteAllCatalogs()
                body.data?.let { catalogs ->
                    catalogDao.insertCatalogs(catalogs.map { it.toEntity() })
                }

                Log.d("CatalogsRepo", "Get catalogs completed successfully: ${body.data?.size} items")
                emit(Result.success(body))
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = when (response.code()) {
                    400 -> "Bad Request"
                    401 -> "Invalid credentials"
                    404 -> "Endpoint not found"
                    405 -> "Method not allowed - Check API endpoint configuration"
                    500 -> "Server error. Please try again later"
                    else -> errorBody ?: "Unknown error: ${response.code()}"
                }

                Log.e("CatalogsRepo", "API Error: $errorMessage")
                emit(Result.failure(Exception(errorMessage)))
            }
        } catch (error: Exception) {
            when (error) {
                is HttpException -> {
                    Log.e("CatalogsRepo", "HTTP Exception: ${error.code()} - ${error.message()}", error)
                }

                is IOException -> {
                    Log.e("CatalogsRepo", "Network Error: ${error.message}", error)
                }

                else -> {
                    Log.e("CatalogsRepo", "Unexpected Error: ${error.message}", error)
                }
            }
            emit(Result.failure(error))
        }
    }

    override fun getCatalogDetail(id: String): Flow<Result<HTTPResponse<CatalogsResponse>>> = flow {
        val cachedCatalog = catalogDao.getCatalogById(id).first()
        val cachedProducts = productsDao.getProductsByCatalogId(id).first()

        val hasCompleteCache = cachedCatalog != null && cachedProducts.isNotEmpty()
        val isCacheValid =
            hasCompleteCache && (System.currentTimeMillis() - cachedCatalog.cachedAt) < _cacheValidityDuration

        // Always emit cache first if it exists (valid or stale)
        if (hasCompleteCache) {
            val catalogWithProducts = cachedCatalog.toDomain().copy(
                products = cachedProducts.map { it.toDomain() }
            )
            val cachedResponse = HTTPResponse(
                status = true,
                message = if (isCacheValid) "Cached data" else "Stale cached data",
                data = catalogWithProducts
            )
            Log.d("CatalogsRepo", "Emitting ${if (isCacheValid) "valid" else "stale"} cache for catalog $id")
            emit(Result.success(cachedResponse))

            // If cache is valid, don't fetch from API
            if (isCacheValid) {
                return@flow
            }
            // If cache is stale, continue to fetch fresh data below
        }

        // Fetch from API (only if no cache OR cache is stale)
        try {
            val response = catalogsApi.getCatalogDetail(id)

            if (!response.isSuccessful) {
                val errorMessage = "API Error: ${response.code()} - ${response.message()}"

                // If we already emitted stale cache above, don't emit error
                if (!hasCompleteCache) {
                    emit(Result.failure(Exception(errorMessage)))
                } else {
                    Log.d("CatalogsRepo", "API failed but stale cache already emitted for catalog $id")
                }
                return@flow
            }

            val body = response.body()
            if (body == null) {
                Log.e("CatalogsRepo", "Response body is null")
                if (!hasCompleteCache) {
                    emit(Result.failure(Exception("Response body is null")))
                }
                return@flow
            }

            val catalog = body.data
            if (catalog == null) {
                Log.e("CatalogsRepo", "Catalog data is null")
                if (!hasCompleteCache) {
                    emit(Result.failure(Exception("Catalog data is null")))
                }
                return@flow
            }

            // Save fresh data to cache
            val catalogEntity = catalog.toEntity().copy(
                cachedAt = System.currentTimeMillis()
            )
            catalogDao.insertCatalog(catalogEntity)

            productsDao.deleteProductsByCatalogId(id)
            catalog.products?.let { products ->
                if (products.isNotEmpty()) {
                    productsDao.insertProducts(products.map { it.toEntity() })
                }
            }

            val savedProducts = productsDao.getProductsByCatalogId(id).first()
            val catalogWithProducts = catalog.copy(
                products = savedProducts.map { it.toDomain() }
            )
            val responseWithProducts = body.copy(
                data = catalogWithProducts,
                message = "Fresh data"
            )

            Log.d("CatalogsRepo", "Emitting fresh data for catalog $id")
            emit(Result.success(responseWithProducts))

        } catch (error: Exception) {
            Log.e("CatalogsRepo", "API Error: ${error.message}", error)

            // Only emit error if we haven't emitted cache
            if (!hasCompleteCache) {
                emit(Result.failure(error))
            } else {
                Log.d("CatalogsRepo", "Exception occurred but stale cache already emitted for catalog $id")
            }
        }
    }

    override suspend fun createCatalog(request: CatalogRequest): Result<HTTPResponse<CatalogsResponse>> {
        return try {
            val response = catalogsApi.createCatalog(request)

            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Response body is null")
                val newCatalog = body.data

                if (newCatalog != null) {
                    catalogDao.insertCatalog(newCatalog.toEntity())
                }

                Log.d("CatalogsRepo", "Catalog created and cache updated")
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
}