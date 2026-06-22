package com.undef.fintrackmobile.data.network

import com.undef.fintrackmobile.data.network.dto.OfferDto
import com.undef.fintrackmobile.data.network.dto.RemotePurchaseDto
import com.undef.fintrackmobile.data.network.dto.RemotePurchaseResponseDto
import com.undef.fintrackmobile.data.network.dto.SupermarketDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Query

interface SupabaseDataApiService {
    @GET("supermarkets")
    suspend fun getSupermarkets(): List<SupermarketDto>

    @GET("offers")
    suspend fun getOffers(): List<OfferDto>

    @POST("remote_purchases")
    suspend fun syncPurchase(
        @Header("Prefer") prefer: String = "return=representation",
        @Body purchase: RemotePurchaseDto
    ): List<RemotePurchaseResponseDto>

    @POST("remote_products")
    suspend fun syncProducts(
        @Body products: List<Map<String, Any>>
    )

    @PATCH("users")
    suspend fun updateUser(
        @Query("id") id: String,
        @Body profile: Map<String, String>
    )
}
