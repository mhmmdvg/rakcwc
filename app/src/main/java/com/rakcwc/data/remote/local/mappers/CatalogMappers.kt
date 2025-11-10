package com.rakcwc.data.remote.local.mappers

import com.rakcwc.data.remote.local.entities.CatalogEntity
import com.rakcwc.domain.models.CatalogsResponse

fun CatalogsResponse.toEntity() = CatalogEntity(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CatalogEntity.toDomain() = CatalogsResponse(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)