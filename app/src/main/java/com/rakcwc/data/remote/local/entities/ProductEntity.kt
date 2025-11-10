package com.rakcwc.data.remote.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity (
    @PrimaryKey val id: String,
    val name: String,
    val variant: String,
    val code: String,
    val description: String?,
    val price: Double,
    val pricePerUnit: Double,
    val imageUrl: String?,
    val catalogId: String,
    val createdAt: String,
    val updatedAt: String,
    @ColumnInfo(name = "cached_at") val cachedAt: Long? = null,
)