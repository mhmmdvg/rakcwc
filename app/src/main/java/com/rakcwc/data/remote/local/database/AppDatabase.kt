package com.rakcwc.data.remote.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rakcwc.data.remote.local.dao.CatalogDao
import com.rakcwc.data.remote.local.dao.ProductsDao
import com.rakcwc.data.remote.local.entities.CatalogEntity
import com.rakcwc.data.remote.local.entities.ProductEntity

@Database(
    entities = [CatalogEntity::class, ProductEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
    abstract fun productDao(): ProductsDao
}