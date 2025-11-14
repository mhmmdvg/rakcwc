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

    override fun getCatalogDetail(
        id: String,
        filter: String?,
        page: Int
    ): Flow<Result<HTTPResponse<CatalogsResponse>>> = flow {
        val hasFilter = filter != null && filter != "All"
        val shouldUseCache = page == 1 && !hasFilter

        if (shouldUseCache) {
            val catalogWithProducts = catalogDao.getCatalogWithProducts(id).first()

            val hasCompleteCache = catalogWithProducts != null && catalogWithProducts.products.isNotEmpty()
            val isCacheValid =
                hasCompleteCache && (System.currentTimeMillis() - catalogWithProducts.catalog.cachedAt) < _cacheValidityDuration

            // Always emit cache first if it exists (valid or stale)
            if (hasCompleteCache) {
                val cachedResponse = HTTPResponse(
                    status = true,
                    message = if (isCacheValid) "Cached data" else "Stale cached data",
                    data = catalogWithProducts.toDomain()
                )
                Log.d("CatalogsRepo", "Emitting ${if (isCacheValid) "valid" else "stale"} cache for catalog $id")
                emit(Result.success(cachedResponse))

                // If cache is valid, don't fetch from API
                if (isCacheValid) {
                    return@flow
                }
                // If cache is stale, continue to fetch fresh data below
            }
        }

        // Fetch from API (only if no cache OR cache is stale)
        try {
            val response = catalogsApi.getCatalogDetail(
                id = id,
                filter = if (hasFilter) filter else null,
                page = page
            )

            if (!response.isSuccessful) {
                val errorMessage = "API Error: ${response.code()} - ${response.message()}"
                emit(Result.failure(Exception(errorMessage)))
                return@flow
            }

            val body = response.body()
            Log.d("CatalogsRepo", "${body?.data?.filters}")
            if (body?.data == null) {
                Log.e("CatalogsRepo", "Response body is null")
                emit(Result.failure(Exception("Response body is null")))
                return@flow
            }

            val catalog = body.data
            if (page == 1 && !hasFilter) {
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
                Log.d("CatalogsRepo", "Cache updated for catalog $id")
            }

            Log.d("CatalogsRepo", "Emitting API data (page: $page, filter: ${filter ?: "none"})")
            emit(Result.success(body))
        } catch (error: Exception) {
            Log.e("CatalogsRepo", "API Error: ${error.message}", error)
            emit(Result.failure(error))
        }
    }

    override suspend fun createCatalog(request: CatalogRequest): Result<HTTPResponse<CatalogsResponse>> {
        return try {
            val response = catalogsApi.createCatalog(request)

            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Response body is null")
                val newCatalog = body.data

                if (newCatalog != null) {
                    catalogDao.insertCatalog(newCatalog.toEntity().copy(
                        cachedAt = System.currentTimeMillis()
                    ))
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

    override suspend fun updateCatalog(id: String, request: CatalogRequest): Result<HTTPResponse<CatalogsResponse>> {
        return runCatching {
            val response = catalogsApi.updateCatalog(id, request)

            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Response body is null")

                body.data?.let { updatedCatalog ->
                    catalogDao.insertCatalog(updatedCatalog.toEntity().copy(
                        cachedAt = System.currentTimeMillis()
                    ))
                }

                body
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Bad Request"
                    401 -> "Unauthorized"
                    500 -> "Server error"
                    else -> "Unknown error: ${response.code()}"
                }
                throw Exception(errorMessage)
            }
        }.onSuccess {
            Log.d("CatalogsRepo", "Catalog updated successfully")
        }.onFailure { error ->
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
        }
    }

    override suspend fun deleteCatalog(id: String): Result<HTTPResponse<CatalogsResponse>> {
        val deletedCatalog = catalogDao.getCatalogById(id).first()
        catalogDao.deleteCatalogById(id)

        return runCatching {
            val response = catalogsApi.deleteCatalog(id)

            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("Response body is null")
                Log.d("CatalogsRepo", "Catalog deleted successfully")
                body
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Bad Request"
                    401 -> "Unauthorized"
                    500 -> "Server error"
                    else -> "Unknown error: ${response.code()}"
                }
                throw Exception(errorMessage)
            }
        }.onFailure { error ->
            // Step 3: Rollback on failure
            if (deletedCatalog != null) {
                Log.e("CatalogsRepo", "Delete failed, restoring catalog", error)
                catalogDao.insertCatalog(deletedCatalog)
                // Products will be restored when user refreshes or navigates back
            }

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
        }
    }

    suspend fun insertOptimisticCatalog(catalog: CatalogsResponse) {
        val existingCatalog = catalogDao.getCatalogById(catalog.id).first()

        val catalogToInsert = if (existingCatalog != null) {
            // Edit mode: preserve existing data
            catalog.toEntity().copy(
                cachedAt = System.currentTimeMillis()
            )
        } else {
            // Create mode: new catalog
            catalog.toEntity().copy(
                cachedAt = System.currentTimeMillis()
            )
        }

        catalogDao.insertCatalog(catalogToInsert)
    }

    suspend fun updateOptimisticCatalog(
        catalogId: String,
        name: String,
        description: String,
        imageUrl: String?
    ) {
        val existingCatalog = catalogDao.getCatalogById(catalogId).first()

        if (existingCatalog != null) {
            // Update only the changed fields, preserve everything else
            val updatedCatalog = existingCatalog.copy(
                name = name,
                description = description,
                imageUrl = imageUrl,
                cachedAt = System.currentTimeMillis()
            )
            catalogDao.insertCatalog(updatedCatalog)
        }
    }

    suspend fun removeOptimisticCatalog(catalogId: String) {
        catalogDao.deleteCatalogById(catalogId)
    }
}