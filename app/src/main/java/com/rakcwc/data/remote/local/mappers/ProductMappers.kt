package com.rakcwc.data.remote.local.mappers

import com.rakcwc.data.remote.local.entities.ProductEntity
import com.rakcwc.domain.models.Products

fun Products.toEntity() = ProductEntity(
    id = id,
    name = name,
    variant = variant,
    code = code,
    description = description,
    price = price,
    pricePerUnit = pricePerUnit,
    imageUrl = imageUrl,
    catalogId = catalogId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ProductEntity.toDomain() = Products(
    id = id,
    name = name,
    variant = variant,
    code = code,
    description = description,
    price = price,
    pricePerUnit = pricePerUnit,
    imageUrl = imageUrl,
    catalogId = catalogId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)