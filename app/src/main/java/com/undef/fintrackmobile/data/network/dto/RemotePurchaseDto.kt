package com.undef.fintrackmobile.data.network.dto

import com.squareup.moshi.Json

/**
 * RemotePurchaseDto: DTO profesional para la sincronización de compras con Supabase.
 */
data class RemotePurchaseDto(
    @param:Json(name = "store_name") val storeName: String,
    @param:Json(name = "total_amount") val totalAmount: Double,
    @param:Json(name = "purchase_date") val purchaseDate: String,
    @param:Json(name = "reason") val reason: String?,
    @param:Json(name = "ticket_image_url") val ticketImageUrl: String?,
    @param:Json(name = "user_id") val userId: String
)
