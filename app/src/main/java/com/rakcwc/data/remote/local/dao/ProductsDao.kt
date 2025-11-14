package com.rakcwc.data.remote.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rakcwc.data.remote.local.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductsDao {
    @Query("SELECT * FROM products")
    fun getProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: String): Flow<ProductEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Query("SELECT * FROM products WHERE catalogId = :catalogId")
    fun getProductsByCatalogId(catalogId: String): Flow<List<ProductEntity>>

    @Query("DELETE FROM products WHERE catalogId = :catalogId")
    suspend fun deleteProductsByCatalogId(catalogId: String)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: String)
}