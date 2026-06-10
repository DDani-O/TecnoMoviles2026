package com.undef.fintrackmobile.data.repository

import android.content.Context
import androidx.annotation.StringRes
import com.undef.fintrackmobile.R
import com.undef.fintrackmobile.data.local.dao.ProductDao
import com.undef.fintrackmobile.data.local.dao.PurchaseDao
import com.undef.fintrackmobile.data.local.entity.ProductEntity
import com.undef.fintrackmobile.data.local.entity.PurchaseEntity
import com.undef.fintrackmobile.data.local.entity.PurchaseWithProducts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
    private val context: Context
) {
    private val seedMutex = Mutex()

    /**
     * 8️⃣ CORRUTINAS - Structured Concurrency
     * El repositorio expone flujos de datos (Flow) o funciones suspendidas.
     * Los flujos permiten que la UI reaccione a cambios en la BD sin volver a consultar.
     */
    fun observePurchases(): Flow<List<PurchaseEntity>> = purchaseDao.observePurchases()

    fun observePurchasesWithProducts(): Flow<List<PurchaseWithProducts>> = purchaseDao.observePurchasesWithProducts()

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
        // Inserta la compra y obtiene el ID generado
        val purchaseId = purchaseDao.insertPurchase(
            PurchaseEntity(
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

    suspend fun seedIfEmpty(force: Boolean = false) = withContext(Dispatchers.IO) {
        seedMutex.withLock {
            if (!force && purchaseDao.countPurchases() > 0) return@withLock
            if (force) {
                purchaseDao.deleteAllPurchases()
                productDao.deleteAllProducts()
            }
            seedPurchases().forEach { seed ->
                val purchaseId = purchaseDao.insertPurchase(
                    PurchaseEntity(
                        supermarketName = context.getString(seed.nameRes),
                        dateMillis = seed.dateMillis,
                        totalCents = seed.totalCents
                    )
                )
                if (seed.products.isNotEmpty()) {
                    productDao.insertProducts(
                        seed.products.map { product ->
                            ProductEntity(
                                purchaseId = purchaseId,
                                name = context.getString(product.nameRes),
                                code = "",
                                description = "",
                                quantity = product.quantity,
                                priceCents = product.priceCents,
                                discountCents = 0
                            )
                        }
                    )
                }
            }
        }
    }

    private fun seedPurchases(): List<SeedPurchase> {
        val now = System.currentTimeMillis()
        val day = 86_400_000L
        val month = 30 * day

        return listOf(
            // Mes Actual
            SeedPurchase(
                nameRes = R.string.supermarket_carrefour,
                dateMillis = now - 1 * day,
                totalCents = 452300,
                products = listOf(
                    SeedProduct(R.string.product_leche, 2, 32000),
                    SeedProduct(R.string.product_pan, 1, 18000),
                    SeedProduct(R.string.product_queso, 1, 25000)
                )
            ),
            SeedPurchase(
                nameRes = R.string.supermarket_coto,
                dateMillis = now - 3 * day,
                totalCents = 289000,
                products = listOf(
                    SeedProduct(R.string.product_carne, 1, 85000),
                    SeedProduct(R.string.product_verduras, 3, 21000)
                )
            ),
            SeedPurchase(
                nameRes = R.string.supermarket_jumbo,
                dateMillis = now - 5 * day,
                totalCents = 312000,
                products = listOf(
                    SeedProduct(R.string.product_bebidas, 2, 38000),
                    SeedProduct(R.string.product_leche, 3, 30000)
                )
            ),
            // Mes Anterior (Hace 1 mes)
            SeedPurchase(
                nameRes = R.string.supermarket_carrefour,
                dateMillis = now - 1 * month - 2 * day,
                totalCents = 550000,
                products = listOf(
                    SeedProduct(R.string.product_leche, 5, 30000),
                    SeedProduct(R.string.product_carne, 2, 90000)
                )
            ),
            SeedPurchase(
                nameRes = R.string.supermarket_coto,
                dateMillis = now - 1 * month - 10 * day,
                totalCents = 420000,
                products = listOf(SeedProduct(R.string.product_bebidas, 10, 15000))
            ),
            // Hace 2 meses
            SeedPurchase(
                nameRes = R.string.supermarket_jumbo,
                dateMillis = now - 2 * month - 5 * day,
                totalCents = 380000,
                products = listOf(SeedProduct(R.string.product_queso, 4, 28000))
            ),
            SeedPurchase(
                nameRes = R.string.supermarket_carrefour,
                dateMillis = now - 2 * month - 15 * day,
                totalCents = 620000,
                products = listOf(SeedProduct(R.string.product_carne, 3, 95000))
            ),
            // Hace 3 meses
            SeedPurchase(
                nameRes = R.string.supermarket_coto,
                dateMillis = now - 3 * month - 8 * day,
                totalCents = 250000,
                products = listOf(SeedProduct(R.string.product_pan, 5, 12000))
            ),
            SeedPurchase(
                nameRes = R.string.supermarket_jumbo,
                dateMillis = now - 3 * month - 20 * day,
                totalCents = 480000,
                products = listOf(SeedProduct(R.string.product_leche, 8, 31000))
            ),
            // Hace 4 meses
            SeedPurchase(
                nameRes = R.string.supermarket_carrefour,
                dateMillis = now - 4 * month - 12 * day,
                totalCents = 510000,
                products = listOf(SeedProduct(R.string.product_verduras, 10, 20000))
            ),
            // Hace 5 meses
            SeedPurchase(
                nameRes = R.string.supermarket_coto,
                dateMillis = now - 5 * month - 5 * day,
                totalCents = 330000,
                products = listOf(SeedProduct(R.string.product_bebidas, 6, 22000))
            )
        )
    }
}

private data class SeedPurchase(
    @param:StringRes val nameRes: Int,
    val dateMillis: Long,
    val totalCents: Long,
    val products: List<SeedProduct>,
)

private data class SeedProduct(
    @param:StringRes val nameRes: Int,
    val quantity: Int,
    val priceCents: Long,
)
