package com.rakcwc.data.remote.api

import com.rakcwc.domain.models.CatalogRequest
import com.rakcwc.domain.models.CatalogsResponse
import com.rakcwc.domain.models.HTTPResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CatalogsApi {
    @GET("catalogs")
    suspend fun getCatalogs(): Response<HTTPResponse<List<CatalogsResponse>>>

    @GET("catalogs/{id}")
    suspend fun getCatalogDetail(@Path("id") id: String): Response<HTTPResponse<CatalogsResponse>>

    @POST("catalogs")
    suspend fun createCatalog(@Body request: CatalogRequest): Response<HTTPResponse<CatalogsResponse>>
}