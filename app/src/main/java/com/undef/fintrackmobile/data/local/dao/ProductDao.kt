package com.undef.fintrackmobile.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.undef.fintrackmobile.data.local.entity.ProductEntity

@Dao
interface ProductDao {
    @Insert
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProducts(products: List<ProductEntity>)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE purchaseId = :purchaseId")
    suspend fun deleteProductsByPurchaseId(purchaseId: Long)

    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}
