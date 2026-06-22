package com.undef.fintrackmobile.data.repository

import android.util.Log
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
        // 1. Borrar localmente
        purchaseDao.deletePurchase(purchase)
        
        // 2. Borrar remotamente si tiene ID remoto
        purchase.remoteId?.let { remoteId ->
            sincronizacionRepository.deleteRemotePurchase(remoteId)
        }
    }

    /**
     * Sincroniza las compras locales no sincronizadas con Supabase.
     */
    suspend fun syncWithRemote(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val prefs = userPreferencesRepository.preferencesFlow.first()
            val userId = prefs.userId
            val email = prefs.email
            
            if (userId.isBlank()) {
                Log.e("PurchaseRepo", "Sync failed: userId is blank")
                return@withContext Result.failure(Exception("Usuario no autenticado (ID faltante)"))
            }
            if (email.isBlank()) {
                Log.e("PurchaseRepo", "Sync failed: email is blank")
                return@withContext Result.failure(Exception("Usuario no autenticado (Email faltante)"))
            }

            val unsynced = purchaseDao.getUnsyncedPurchasesByUser(email)
            Log.d("PurchaseRepo", "Found ${unsynced.size} unsynced purchases")
            
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
                
                var remoteId = purchase.remoteId
                val syncResult: Result<Int> = if (remoteId == null) {
                    // 1. Crear nueva compra
                    Log.d("PurchaseRepo", "Creating new remote purchase for local id ${purchase.id}")
                    sincronizacionRepository.syncPurchase(dto).map { it.id }
                } else {
                    // 2. Actualizar compra existente
                    Log.d("PurchaseRepo", "Updating existing remote purchase $remoteId for local id ${purchase.id}")
                    sincronizacionRepository.updateRemotePurchase(remoteId, dto).map { remoteId }
                }

                syncResult.onSuccess { id ->
                    remoteId = id
                    var productsSynced = true

                    // 3. Sincronizar productos
                    if (products.isNotEmpty()) {
                        // Si era una actualización, primero borramos los productos remotos viejos
                        remoteId.let { rId ->
                            Log.d("PurchaseRepo", "Deleting old remote products for purchase $rId")
                            sincronizacionRepository.deleteRemoteProducts(rId)
                        }

                        val remoteProducts = products.map { p ->
                            mapOf(
                                "purchase_id" to remoteId,
                                "name" to p.name,
                                "code" to p.code,
                                "description" to p.description,
                                "quantity" to p.quantity,
                                "price_cents" to p.priceCents,
                                "discount_cents" to p.discountCents
                            )
                        }
                        Log.d("PurchaseRepo", "Syncing ${remoteProducts.size} products for purchase $remoteId")
                        val productsResult = sincronizacionRepository.syncProducts(remoteProducts)
                        if (productsResult.isFailure) {
                            productsSynced = false
                            Log.e("PurchaseRepo", "Failed to sync products: ${productsResult.exceptionOrNull()?.message}")
                        }
                    }

                    // 4. Marcar como sincronizado SOLO si los productos también se sincronizaron (o no había)
                    if (productsSynced) {
                        Log.d("PurchaseRepo", "Sync completed for purchase $remoteId")
                        purchaseDao.updatePurchase(
                            purchase.copy(
                                remoteId = remoteId,
                                isSynced = true
                            )
                        )
                    } else {
                        // Si fallaron los productos, guardamos el remoteId para la próxima pero no marcamos isSynced
                        purchaseDao.updatePurchase(purchase.copy(remoteId = remoteId))
                    }
                }.onFailure { e ->
                    Log.e("PurchaseRepo", "Failed to sync purchase header: ${e.message}")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("PurchaseRepo", "Critical error in syncWithRemote", e)
            Result.failure(e)
        }
    }
}
