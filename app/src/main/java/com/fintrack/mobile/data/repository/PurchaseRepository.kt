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

    suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        seedMutex.withLock {
            if (purchaseDao.countPurchases() > 0) return@withLock
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
        return listOf(
            SeedPurchase(
                nameRes = R.string.supermarket_carrefour,
                dateMillis = now - 86_400_000L,
                totalCents = 45230,
                products = listOf(
                    SeedProduct(R.string.product_leche, 2, 3200),
                    SeedProduct(R.string.product_pan, 1, 1800),
                    SeedProduct(R.string.product_queso, 1, 2500),
                    SeedProduct(R.string.product_verduras, 2, 1200),
                    SeedProduct(R.string.product_bebidas, 3, 800)
                )
            ),
            SeedPurchase(
                nameRes = R.string.supermarket_coto,
                dateMillis = now - 172_800_000L,
                totalCents = 28900,
                products = listOf(
                    SeedProduct(R.string.product_queso, 1, 4200),
                    SeedProduct(R.string.product_verduras, 3, 2100),
                    SeedProduct(R.string.product_carne, 1, 8500),
                    SeedProduct(R.string.product_pan, 2, 1500),
                    SeedProduct(R.string.product_leche, 1, 3200),
                    SeedProduct(R.string.product_bebidas, 2, 2000)
                )
            ),
            SeedPurchase(
                nameRes = R.string.supermarket_jumbo,
                dateMillis = now - 259_200_000L,
                totalCents = 31200,
                products = listOf(
                    SeedProduct(R.string.product_carne, 1, 12500),
                    SeedProduct(R.string.product_bebidas, 2, 3800),
                    SeedProduct(R.string.product_verduras, 2, 1800),
                    SeedProduct(R.string.product_leche, 3, 3000),
                    SeedProduct(R.string.product_pan, 1, 1800),
                    SeedProduct(R.string.product_queso, 1, 2500)
                )
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
