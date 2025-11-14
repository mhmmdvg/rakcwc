package com.rakcwc.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ProductsResponse(
    val products: List<Products>,
    val pagination: Pagination
)

@Serializable
data class Products(
    val id: String,
    val name: String,
    val variant: String,
    val code: String,
    val description: String?,
    val catalogId: String,
    val pricePerUnit: Double,
    val price: Double,
    val imageUrl: String?,
    val createdAt: String,
    val updatedAt: String
)


@Serializable
data class Pagination(
    val currentPage: Int,
    val totalPages: Int,
    val totalItems: Int,
    val itemsPerPage: Int,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean
)

@Serializable
data class ProductRequest(
    val name: String,
    val variant: String,
    val code: String,
    val description: String?,
    val catalogId: String,
    val pricePerUnit: Double,
    val price: Double,
    val imageUrl: String,
)