package com.undef.fintrackmobile.data.network

import com.undef.fintrackmobile.data.network.dto.AuthRequestDto
import com.undef.fintrackmobile.data.network.dto.AuthResponseDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseAuthApiService {
    @POST("signup")
    suspend fun signup(@Body request: AuthRequestDto): AuthResponseDto

    @POST("token")
    suspend fun login(
        @Query("grant_type") grantType: String = "password",
        @Body request: AuthRequestDto
    ): AuthResponseDto
}
