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

    @GET("remote_purchases")
    suspend fun getPurchases(
        @Query("user_id") userIdFilter: String
    ): List<RemotePurchaseResponseDto>

    @GET("remote_products")
    suspend fun getProducts(
        @Query("purchase_id") purchaseIdFilter: String
    ): List<com.undef.fintrackmobile.data.network.dto.RemoteProductResponseDto>

    @POST("remote_purchases")
    suspend fun syncPurchase(
        @Header("Prefer") prefer: String = "return=representation",
        @Body purchase: RemotePurchaseDto
    ): List<RemotePurchaseResponseDto>

    @PATCH("remote_purchases")
    suspend fun updatePurchase(
        @Query("id") filter: String,
        @Body purchase: RemotePurchaseDto
    )

    @POST("remote_products")
    suspend fun syncProducts(
        @Header("Prefer") prefer: String = "return=minimal",
        @Body products: @JvmSuppressWildcards List<Map<String, Any>>
    )

    @retrofit2.http.DELETE("remote_products")
    suspend fun deleteProducts(
        @Query("purchase_id") filter: String
    )

    @retrofit2.http.DELETE("remote_purchases")
    suspend fun deletePurchase(
        @Query("id") filter: String // Ej: "eq.123"
    )

    @PATCH("users")
    suspend fun updateUser(
        @Query("id") id: String,
        @Body profile: Map<String, String>
    )
}
