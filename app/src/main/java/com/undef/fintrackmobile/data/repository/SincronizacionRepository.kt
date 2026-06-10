package com.undef.fintrackmobile.data.repository

import com.undef.fintrackmobile.data.network.SyncApiService
import com.undef.fintrackmobile.data.network.SuperAhorroApiService
import com.undef.fintrackmobile.data.network.dto.RemotePurchaseDto
import com.undef.fintrackmobile.data.network.dto.RemotePurchaseResponseDto
import com.undef.fintrackmobile.data.network.dto.OfferDto
import com.undef.fintrackmobile.data.network.dto.SupermarketDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SincronizacionRepository(
    private val apiService: SuperAhorroApiService,
    private val syncApiService: SyncApiService
) {
    suspend fun getRemoteSupermarkets(): Result<List<SupermarketDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getSupermarkets()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRemoteOffers(): Result<List<OfferDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getOffers()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncPurchase(dto: RemotePurchaseDto): Result<RemotePurchaseResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = syncApiService.syncPurchase(dto)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
