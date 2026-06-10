package com.undef.fintrackmobile.data.network.dto

import com.squareup.moshi.Json

/**
 * RemotePurchaseResponseDto: Respuesta del servidor tras una sincronización exitosa.
 */
data class RemotePurchaseResponseDto(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "store_name") val storeName: String,
    @param:Json(name = "total_amount") val totalAmount: Double,
    @param:Json(name = "purchase_date") val purchaseDate: String,
    @param:Json(name = "reason") val reason: String,
    @param:Json(name = "user_id") val userId: Int
)
