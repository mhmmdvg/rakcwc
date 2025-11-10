package com.rakcwc.domain.repositories


import com.rakcwc.domain.models.CatalogRequest
import com.rakcwc.domain.models.CatalogsResponse
import com.rakcwc.domain.models.HTTPResponse
import kotlinx.coroutines.flow.Flow

interface CatalogsRepository {
    fun getCatalogs(): Flow<Result<HTTPResponse<List<CatalogsResponse>>>>
    fun getCatalogDetail(id: String): Flow<Result<HTTPResponse<CatalogsResponse>>>
    suspend fun createCatalog(request: CatalogRequest): Result<HTTPResponse<CatalogsResponse>>
}