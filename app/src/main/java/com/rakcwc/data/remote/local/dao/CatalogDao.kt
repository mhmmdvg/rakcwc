package com.rakcwc.data.remote.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.rakcwc.data.remote.local.entities.CatalogEntity
import com.rakcwc.data.remote.local.entities.CatalogWithProducts
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {
    @Query("SELECT * FROM catalogs")
    fun getCatalogs(): Flow<List<CatalogEntity>>

    @Query("SELECT * FROM catalogs WHERE id = :id")
    fun getCatalogById(id: String): Flow<CatalogEntity?>

    @Transaction
    @Query("SELECT * FROM catalogs WHERE id = :id")
    fun getCatalogWithProducts(id: String): Flow<CatalogWithProducts?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalogs(catalogs: List<CatalogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalog(catalog: CatalogEntity)

    @Query("DELETE FROM catalogs")
    suspend fun deleteAllCatalogs()

    @Query("DELETE FROM catalogs WHERE id = :id")
    suspend fun deleteCatalogById(id: String)
}
