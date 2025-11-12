package com.rakcwc.data.remote.local.mappers

import com.rakcwc.data.remote.local.entities.CatalogEntity
import com.rakcwc.data.remote.local.entities.CatalogWithProducts
import com.rakcwc.domain.models.CatalogsResponse
import com.rakcwc.domain.models.Filters

fun CatalogsResponse.toEntity() = CatalogEntity(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl,
    filters = filters?.map { it.name },
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CatalogEntity.toDomain() = CatalogsResponse(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl,
    filters = filters?.map { Filters(it) },
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CatalogWithProducts.toDomain() = CatalogsResponse(
    id = catalog.id,
    name = catalog.name,
    description = catalog.description,
    imageUrl = catalog.imageUrl,
    filters = catalog.filters?.map { Filters(it) },
    products = products.map { it.toDomain() },
    createdAt = catalog.createdAt,
    updatedAt = catalog.updatedAt
)