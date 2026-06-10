package com.undef.fintrackmobile.data.network

import com.undef.fintrackmobile.data.network.dto.RemotePurchaseDto
import com.undef.fintrackmobile.data.network.dto.RemotePurchaseResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * SyncApiService: Interfaz para la sincronización de compras en el segundo proyecto de MockAPI.
 */
interface SyncApiService {
    @POST("purchases")
    suspend fun syncPurchase(@Body purchase: RemotePurchaseDto): RemotePurchaseResponseDto
}
