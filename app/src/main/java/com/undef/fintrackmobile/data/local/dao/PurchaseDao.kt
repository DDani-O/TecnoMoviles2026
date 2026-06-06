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
     * Al retornar Flow, Room notifica automáticamente a la UI cuando los datos cambian.
     * Reactividad desde la fuente de verdad.
     */
    @Query("SELECT * FROM purchases ORDER BY dateMillis DESC")
    fun observePurchases(): Flow<List<PurchaseEntity>>

    /**
     * @Transaction garantiza que la consulta de la compra y sus productos relacionados
     * ocurra de forma atómica.
     */
    @Transaction
    @Query("SELECT * FROM purchases ORDER BY dateMillis DESC")
    fun observePurchasesWithProducts(): Flow<List<PurchaseWithProducts>>

    @Transaction
    @Query("SELECT * FROM purchases WHERE id = :purchaseId")
    suspend fun obtenerCompraConProductosPorId(purchaseId: Long): PurchaseWithProducts?

    @Query("SELECT * FROM purchases WHERE dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun observarComprasPorPeriodo(startMillis: Long, endMillis: Long): Flow<List<PurchaseEntity>>

    // 'suspend' indica que la operación es asincrónica y debe ejecutarse en un hilo de I/O
    @Insert
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Update
    suspend fun updatePurchase(purchase: PurchaseEntity)

    @Delete
    suspend fun deletePurchase(purchase: PurchaseEntity)
    
    @Query("SELECT COUNT(*) FROM purchases")
    suspend fun countPurchases(): Int
    
    @Query("DELETE FROM purchases")
    suspend fun deleteAllPurchases()
}
