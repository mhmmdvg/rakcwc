package com.rakcwc.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class CatalogsResponse(
    val id: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val createdAt: String,
    val updatedAt: String,
    val filters: List<Filters>? = null,
    val products: List<Products>? = null,
    val pagination: Pagination? = null
)

@Serializable
data class CatalogRequest(
    val name: String,
    val description: String?,
    val imageUrl: String
)

@Serializable
data class Filters(
    val name: String,
    val count: Int? = null
)