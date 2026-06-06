package com.undef.fintrackmobile.data.network.dto

import com.squareup.moshi.Json

data class CompraRemotaRespuestaDto(
    @Json(name = "id") val id: Int,
    @Json(name = "titulo") val titulo: String,
    @Json(name = "detalle") val detalle: String
)
