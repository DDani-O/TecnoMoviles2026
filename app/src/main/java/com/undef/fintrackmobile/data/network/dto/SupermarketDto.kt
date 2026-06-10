package com.undef.fintrackmobile.data.network.dto

import com.squareup.moshi.Json

/**
 * SupermarketDto: DTO profesional para la consulta de supermercados.
 */
data class SupermarketDto(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "address") val address: String,
    @param:Json(name = "schedule") val schedule: String,
    @param:Json(name = "rating") val rating: Float
)
