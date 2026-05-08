package com.undef.fintrackmobile.data.local.entity

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
