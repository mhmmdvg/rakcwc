package com.rakcwc.data.remote.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rakcwc.data.remote.local.dao.CatalogDao
import com.rakcwc.data.remote.local.dao.ProductsDao
import com.rakcwc.data.remote.local.entities.CatalogEntity
import com.rakcwc.data.remote.local.entities.ProductEntity
import com.rakcwc.utils.DataConverter

@Database(
    entities = [CatalogEntity::class, ProductEntity::class],
    version = 2,
    exportSchema = false
)

@TypeConverters(DataConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun catalogDao(): CatalogDao
    abstract fun productDao(): ProductsDao
}