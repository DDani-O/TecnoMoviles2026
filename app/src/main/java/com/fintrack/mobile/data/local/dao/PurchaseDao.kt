package com.fintrack.mobile.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.fintrack.mobile.data.local.entity.PurchaseEntity
import com.fintrack.mobile.data.local.entity.PurchaseWithProducts
import com.fintrack.mobile.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY dateMillis DESC")
    fun observePurchases(): Flow<List<PurchaseEntity>>

    @Transaction
    @Query("SELECT * FROM purchases ORDER BY dateMillis DESC")
    fun observePurchasesWithProducts(): Flow<List<PurchaseWithProducts>>

    @Query("SELECT COUNT(*) FROM purchases")
    suspend fun countPurchases(): Int

    @Insert
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Insert
    suspend fun insertProducts(products: List<ProductEntity>)
}
