package com.undef.fintrackmobile.data.network.dto

import com.squareup.moshi.Json

data class SupermercadoDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "body") val body: String
)
