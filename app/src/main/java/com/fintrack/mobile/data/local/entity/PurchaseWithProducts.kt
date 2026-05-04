package com.fintrack.mobile.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PurchaseWithProducts(
    @Embedded val purchase: PurchaseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "purchaseId"
    )
    val products: List<ProductEntity>
)
