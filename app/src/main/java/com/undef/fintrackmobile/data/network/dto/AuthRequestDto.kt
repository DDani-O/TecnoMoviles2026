package com.undef.fintrackmobile.data.network.dto

import com.squareup.moshi.Json

data class AuthRequestDto(
    @param:Json(name = "email") val email: String,
    @param:Json(name = "password") val password: String,
    @param:Json(name = "data") val data: Map<String, Any>? = null
)
