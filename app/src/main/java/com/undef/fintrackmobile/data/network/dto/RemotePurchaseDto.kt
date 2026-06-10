package com.undef.fintrackmobile.data.network.dto

import com.squareup.moshi.Json

/**
 * RemotePurchaseDto: DTO profesional para la sincronización de compras.
 * Los nombres están en inglés para seguir los estándares de la industria.
 */
data class RemotePurchaseDto(
    @param:Json(name = "store_name") val storeName: String,
    @param:Json(name = "total_amount") val totalAmount: Double,
    @param:Json(name = "purchase_date") val purchaseDate: String,
    @param:Json(name = "reason") val reason: String,
    @param:Json(name = "user_id") val userId: Int = 1
)
