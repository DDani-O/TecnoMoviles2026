package com.undef.fintrackmobile.data.repository

import com.undef.fintrackmobile.data.local.dao.ProductDao
import com.undef.fintrackmobile.data.local.dao.PurchaseDao
import com.undef.fintrackmobile.data.local.entity.ProductEntity
import com.undef.fintrackmobile.data.local.entity.PurchaseEntity
import com.undef.fintrackmobile.data.local.entity.PurchaseWithProducts
import com.undef.fintrackmobile.data.network.dto.RemotePurchaseDto
import com.undef.fintrackmobile.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

data class NewProduct(
    val name: String,
    val code: String = "",
    val description: String = "",
    val quantity: Int,
    val priceCents: Long,
    val discountCents: Long = 0
)

class PurchaseRepository(
    private val purchaseDao: PurchaseDao,
    private val productDao: ProductDao,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sincronizacionRepository: SincronizacionRepository
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun observePurchases(email: String): Flow<List<PurchaseEntity>> = 
        purchaseDao.observePurchasesByUser(email)

    fun observePurchasesWithProducts(email: String): Flow<List<PurchaseWithProducts>> = 
        purchaseDao.observePurchasesWithProductsByUser(email)

    suspend fun addPurchase(
        supermarketName: String,
        totalCents: Long,
        dateMillis: Long,
        reason: String,
        products: List<NewProduct>
    ) = withContext(Dispatchers.IO) {
        val currentUserEmail = userPreferencesRepository.preferencesFlow.first().email

        val purchaseId = purchaseDao.insertPurchase(
            PurchaseEntity(
                userEmail = currentUserEmail,
                supermarketName = supermarketName,
                dateMillis = dateMillis,
                totalCents = totalCents,
                reason = reason,
                isSynced = false
            )
        )
        if (products.isNotEmpty()) {
            val entities = products.map { product ->
                ProductEntity(
                    purchaseId = purchaseId,
                    name = product.name,
                    code = product.code,
                    description = product.description,
                    quantity = product.quantity,
                    priceCents = product.priceCents,
                    discountCents = product.discountCents
                )
            }
            productDao.insertProducts(entities)
        }
    }

    suspend fun updatePurchase(purchase: PurchaseEntity, products: List<ProductEntity>) = withContext(Dispatchers.IO) {
        purchaseDao.updatePurchase(purchase.copy(isSynced = false))
        productDao.deleteProductsByPurchaseId(purchase.id)
        productDao.insertProducts(products)
    }

    suspend fun deletePurchase(purchase: PurchaseEntity) = withContext(Dispatchers.IO) {
        purchaseDao.deletePurchase(purchase)
    }

    /**
     * Sincroniza las compras locales no sincronizadas con Supabase.
     */
    suspend fun syncWithRemote(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val prefs = userPreferencesRepository.preferencesFlow.first()
            val userId = prefs.userId
            val email = prefs.email
            
            if (userId.isBlank()) return@withContext Result.failure(Exception("Usuario no autenticado"))

            val unsynced = purchaseDao.getUnsyncedPurchasesByUser(email)
            
            for (item in unsynced) {
                val purchase = item.purchase
                val products = item.products
                
                val dto = RemotePurchaseDto(
                    storeName = purchase.supermarketName,
                    totalAmount = purchase.totalCents.toDouble() / 100.0,
                    purchaseDate = dateFormat.format(Date(purchase.dateMillis)),
                    reason = purchase.reason,
                    userId = userId
                )
                
                val result = sincronizacionRepository.syncPurchase(dto)
                result.onSuccess { response ->
                    // 1. Marcar compra como sincronizada
                    purchaseDao.updatePurchase(
                        purchase.copy(
                            remoteId = response.id,
                            isSynced = true
                        )
                    )
                    
                    // 2. Sincronizar productos asociados
                    if (products.isNotEmpty()) {
                        val remoteProducts = products.map { p ->
                            mapOf(
                                "purchase_id" to response.id,
                                "name" to p.name,
                                "code" to p.code,
                                "description" to p.description,
                                "quantity" to p.quantity,
                                "price_cents" to p.priceCents,
                                "discount_cents" to p.discountCents
                            )
                        }
                        sincronizacionRepository.syncProducts(remoteProducts)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
