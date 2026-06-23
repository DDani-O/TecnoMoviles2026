package com.undef.fintrackmobile.data.network.dto

import com.squareup.moshi.Json

/**
 * RemoteProductResponseDto: DTO para recibir productos desde Supabase.
 */
data class RemoteProductResponseDto(
    @param:Json(name = "id") val id: Int,
    @param:Json(name = "purchase_id") val purchaseId: Int,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "code") val code: String?,
    @param:Json(name = "description") val description: String?,
    @param:Json(name = "quantity") val quantity: Int,
    @param:Json(name = "price_cents") val priceCents: Long,
    @param:Json(name = "discount_cents") val discountCents: Long?
)
