package com.undef.fintrackmobile.data.repository

import com.undef.fintrackmobile.data.local.dao.ProductDao
import com.undef.fintrackmobile.data.local.dao.PurchaseDao
import com.undef.fintrackmobile.data.local.entity.ProductEntity
import com.undef.fintrackmobile.data.local.entity.PurchaseEntity
import com.undef.fintrackmobile.data.local.entity.PurchaseWithProducts
import com.undef.fintrackmobile.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext


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
) {

    /**
     * Observa las compras filtradas por el email del usuario logueado.
     */
    fun observePurchases(email: String): Flow<List<PurchaseEntity>> = 
        purchaseDao.observePurchasesByUser(email)

    /**
     * Observa las compras con sus productos filtradas por usuario.
     */
    fun observePurchasesWithProducts(email: String): Flow<List<PurchaseWithProducts>> = 
        purchaseDao.observePurchasesWithProductsByUser(email)

    /**
     * Operación de escritura: Usamos withContext(Dispatchers.IO) para delegar el trabajo
     * de base de datos a un hilo de I/O, evitando trabar la pantalla del usuario.
     */
    suspend fun addPurchase(
        supermarketName: String,
        totalCents: Long,
        dateMillis: Long,
        reason: String,
        products: List<NewProduct>
    ) = withContext(Dispatchers.IO) {
        // Recuperamos el email del usuario actual desde DataStore para el aislamiento de datos
        val currentUserEmail = userPreferencesRepository.preferencesFlow.first().email

        // Inserta la compra y obtiene el ID generado, incluyendo el userEmail
        val purchaseId = purchaseDao.insertPurchase(
            PurchaseEntity(
                userEmail = currentUserEmail,
                supermarketName = supermarketName,
                dateMillis = dateMillis,
                totalCents = totalCents,
                reason = reason
            )
        )
        // Si hay productos, los asocia al ID de la compra recién creada
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
        purchaseDao.updatePurchase(purchase)
        productDao.deleteProductsByPurchaseId(purchase.id)
        productDao.insertProducts(products)
    }

    suspend fun deletePurchase(purchase: PurchaseEntity) = withContext(Dispatchers.IO) {
        purchaseDao.deletePurchase(purchase)
    }
}
