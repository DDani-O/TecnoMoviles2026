package com.undef.fintrackmobile.data.network.dto

import com.squareup.moshi.Json

data class CompraRemotaDto(
    @Json(name = "titulo") val titulo: String,
    @Json(name = "detalle") val detalle: String,
    @Json(name = "usuarioId") val usuarioId: Int = 1
)
