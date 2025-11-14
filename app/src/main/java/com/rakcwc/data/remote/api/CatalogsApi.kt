package com.rakcwc.data.remote.api

import com.rakcwc.domain.models.CatalogRequest
import com.rakcwc.domain.models.CatalogsResponse
import com.rakcwc.domain.models.HTTPResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CatalogsApi {
    @GET("catalogs")
    suspend fun getCatalogs(): Response<HTTPResponse<List<CatalogsResponse>>>

    @GET("catalogs/{id}")
    suspend fun getCatalogDetail(
        @Path("id") id: String,
        @Query("filter") filter: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
    ): Response<HTTPResponse<CatalogsResponse>>

    @POST("catalogs")
    suspend fun createCatalog(@Body request: CatalogRequest): Response<HTTPResponse<CatalogsResponse>>

    @PATCH("catalogs/{id}")
    suspend fun updateCatalog(@Path("id") id: String, @Body request: CatalogRequest): Response<HTTPResponse<CatalogsResponse>>

    @DELETE("catalogs/{id}")
    suspend fun deleteCatalog(@Path("id") id: String): Response<HTTPResponse<CatalogsResponse>>
}