package com.rakcwc.data.remote.local.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "catalogs")
data class CatalogEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val createdAt: String,
    val updatedAt: String,
    @ColumnInfo(name = "cached_at") val cachedAt: Long = System.currentTimeMillis(),
)

data class CatalogDetailEntity(
    @Embedded val catalog: CatalogEntity,
    @Relation(parentColumn = "id", entityColumn = "catalogId") val variants: List<ProductEntity>
)
