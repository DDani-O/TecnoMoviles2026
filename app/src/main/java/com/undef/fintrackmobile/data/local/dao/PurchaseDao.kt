package com.undef.fintrackmobile.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.undef.fintrackmobile.data.local.entity.PurchaseEntity
import com.undef.fintrackmobile.data.local.entity.PurchaseWithProducts
import kotlinx.coroutines.flow.Flow

/*
 * 6️⃣ BASE DE DATOS LOCAL - Room DAO
 * El DAO define las operaciones SQL de forma Type-Safe.
 */
@Dao
interface PurchaseDao {
    /**
     * Retorna el flujo de compras filtradas por el email del usuario.
     */
    @Query("SELECT * FROM purchases WHERE userEmail = :email ORDER BY dateMillis DESC")
    fun observePurchasesByUser(email: String): Flow<List<PurchaseEntity>>

    /**
     * Retorna las compras con sus productos, filtradas por usuario.
     * @Transaction garantiza atomicidad en la consulta relacional.
     */
    @Transaction
    @Query("SELECT * FROM purchases WHERE userEmail = :email ORDER BY dateMillis DESC")
    fun observePurchasesWithProductsByUser(email: String): Flow<List<PurchaseWithProducts>>

    @Transaction
    @Query("SELECT * FROM purchases WHERE id = :purchaseId")
    suspend fun getPurchaseWithProductsById(purchaseId: Long): PurchaseWithProducts?

    @Query("SELECT * FROM purchases WHERE userEmail = :email AND dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun observePurchasesByPeriod(email: String, startMillis: Long, endMillis: Long): Flow<List<PurchaseEntity>>

    // 'suspend' indica que la operación es asincrónica y debe ejecutarse en un hilo de I/O
    @Insert
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Update
    suspend fun updatePurchase(purchase: PurchaseEntity)

    @Delete
    suspend fun deletePurchase(purchase: PurchaseEntity)
    
    @Query("SELECT COUNT(*) FROM purchases WHERE userEmail = :email")
    suspend fun countPurchasesByUser(email: String): Int
    
    @Query("DELETE FROM purchases")
    suspend fun deleteAllPurchases()
}
