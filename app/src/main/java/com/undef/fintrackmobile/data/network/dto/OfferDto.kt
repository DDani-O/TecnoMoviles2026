package com.undef.fintrackmobile.data.network.dto

import com.squareup.moshi.Json

/**
 * OfferDto: Objeto de transferencia de datos para las ofertas desde MockAPI.
 */
data class OfferDto(
    @param:Json(name = "id") val id: Int,
    @param:Json(name = "title") val title: String,
    @param:Json(name = "description") val description: String?,
    @param:Json(name = "store") val store: String?,
    @param:Json(name = "category") val category: String? = null
)
