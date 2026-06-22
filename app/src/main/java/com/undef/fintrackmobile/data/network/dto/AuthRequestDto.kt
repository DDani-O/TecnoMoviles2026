package com.undef.fintrackmobile.data.network.dto

import com.squareup.moshi.Json

data class AuthRequestDto(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "data") val data: Map<String, String>? = null
)
