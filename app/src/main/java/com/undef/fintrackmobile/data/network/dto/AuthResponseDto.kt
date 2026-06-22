package com.undef.fintrackmobile.data.network.dto

import com.squareup.moshi.Json

data class AuthResponseDto(
    @param:Json(name = "access_token") val accessToken: String,
    @param:Json(name = "user") val user: SupabaseUserDto
)

data class SupabaseUserDto(
    @param:Json(name = "id") val id: String,
    @param:Json(name = "email") val email: String? = null
)
