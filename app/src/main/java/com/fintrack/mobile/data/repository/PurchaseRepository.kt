package com.fintrack.mobile.data.repository

import android.content.Context
import androidx.annotation.StringRes
import com.fintrack.mobile.R
import com.fintrack.mobile.data.local.dao.PurchaseDao
import com.fintrack.mobile.data.local.entity.ProductEntity
import com.fintrack.mobile.data.local.entity.PurchaseEntity
import com.fintrack.mobile.data.local.entity.PurchaseWithProducts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class NewProduct(
    val name: String,
    val quantity: Int,
    val priceCents: Long
)

class PurchaseRepository(
    private val purchaseDao: PurchaseDao,
    private val context: Context
) {
    private val seedMutex = Mutex()

    fun observePurchases(): Flow<List<PurchaseEntity>> = purchaseDao.observePurchases()

    fun observePurchasesWithProducts(): Flow<List<PurchaseWithProducts>> = purchaseDao.observePurchasesWithProducts()

    suspend fun updatePurchase(
        purchase: PurchaseEntity,
        products: List<ProductEntity>
    ) = withContext(Dispatchers.IO) {
        purchaseDao.updatePurchase(purchase)
        if (products.isNotEmpty()) {
            purchaseDao.updateProducts(products)
        }
    }

    suspend fun deletePurchase(purchase: PurchaseEntity) = withContext(Dispatchers.IO) {
        purchaseDao.deletePurchase(purchase)
    }

    suspend fun addPurchase(
        supermarketName: String,
        totalCents: Long,
        products: List<NewProduct>
    ) = withContext(Dispatchers.IO) {
        val purchaseId = purchaseDao.insertPurchase(
            PurchaseEntity(
                supermarketName = supermarketName,
                dateMillis = System.currentTimeMillis(),
                totalCents = totalCents
            )
        )
        if (products.isNotEmpty()) {
            val entities = products.map { product ->
                ProductEntity(
                    purchaseId = purchaseId,
                    name = product.name,
                    quantity = product.quantity,
                    priceCents = product.priceCents
                )
            }
            purchaseDao.insertProducts(entities)
        }
    }

    suspend fun seedIfEmpty(force: Boolean = false) = withContext(Dispatchers.IO) {
        seedMutex.withLock {
            if (!force && purchaseDao.countPurchases() > 0) return@withLock
            if (force) {
                purchaseDao.deleteAllPurchases()
                purchaseDao.deleteAllProducts()
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
                    purchaseDao.insertProducts(
                        seed.products.map { product ->
                            ProductEntity(
                                purchaseId = purchaseId,
                                name = context.getString(product.nameRes),
                                quantity = product.quantity,
                                priceCents = product.priceCents
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
                totalCents = 45230,
                products = listOf(
                    SeedProduct(R.string.product_leche, 2, 3200),
                    SeedProduct(R.string.product_pan, 1, 1800),
                    SeedProduct(R.string.product_queso, 1, 2500)
                )
            ),
            SeedPurchase(
                nameRes = R.string.supermarket_coto,
                dateMillis = now - 3 * day,
                totalCents = 28900,
                products = listOf(
                    SeedProduct(R.string.product_carne, 1, 8500),
                    SeedProduct(R.string.product_verduras, 3, 2100)
                )
            ),
            SeedPurchase(
                nameRes = R.string.supermarket_jumbo,
                dateMillis = now - 5 * day,
                totalCents = 31200,
                products = listOf(
                    SeedProduct(R.string.product_bebidas, 2, 3800),
                    SeedProduct(R.string.product_leche, 3, 3000)
                )
            ),
            // Mes Anterior (Hace 1 mes)
            SeedPurchase(
                nameRes = R.string.supermarket_carrefour,
                dateMillis = now - 1 * month - 2 * day,
                totalCents = 55000,
                products = listOf(
                    SeedProduct(R.string.product_leche, 5, 3000),
                    SeedProduct(R.string.product_carne, 2, 9000)
                )
            ),
            SeedPurchase(
                nameRes = R.string.supermarket_coto,
                dateMillis = now - 1 * month - 10 * day,
                totalCents = 42000,
                products = listOf(SeedProduct(R.string.product_bebidas, 10, 1500))
            ),
            // Hace 2 meses
            SeedPurchase(
                nameRes = R.string.supermarket_jumbo,
                dateMillis = now - 2 * month - 5 * day,
                totalCents = 38000,
                products = listOf(SeedProduct(R.string.product_queso, 4, 2800))
            ),
            SeedPurchase(
                nameRes = R.string.supermarket_carrefour,
                dateMillis = now - 2 * month - 15 * day,
                totalCents = 62000,
                products = listOf(SeedProduct(R.string.product_carne, 3, 9500))
            ),
            // Hace 3 meses
            SeedPurchase(
                nameRes = R.string.supermarket_coto,
                dateMillis = now - 3 * month - 8 * day,
                totalCents = 25000,
                products = listOf(SeedProduct(R.string.product_pan, 5, 1200))
            ),
            SeedPurchase(
                nameRes = R.string.supermarket_jumbo,
                dateMillis = now - 3 * month - 20 * day,
                totalCents = 48000,
                products = listOf(SeedProduct(R.string.product_leche, 8, 3100))
            ),
            // Hace 4 meses
            SeedPurchase(
                nameRes = R.string.supermarket_carrefour,
                dateMillis = now - 4 * month - 12 * day,
                totalCents = 51000,
                products = listOf(SeedProduct(R.string.product_verduras, 10, 2000))
            ),
            // Hace 5 meses
            SeedPurchase(
                nameRes = R.string.supermarket_coto,
                dateMillis = now - 5 * month - 5 * day,
                totalCents = 33000,
                products = listOf(SeedProduct(R.string.product_bebidas, 6, 2200))
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
