package com.undef.fintrackmobile.data.repository

import com.undef.fintrackmobile.data.network.SupabaseAuthApiService
import com.undef.fintrackmobile.data.network.SupabaseDataApiService
import com.undef.fintrackmobile.data.network.dto.RemotePurchaseDto
import com.undef.fintrackmobile.data.network.dto.RemotePurchaseResponseDto
import com.undef.fintrackmobile.data.network.dto.OfferDto
import com.undef.fintrackmobile.data.network.dto.SupermarketDto
import com.undef.fintrackmobile.data.network.dto.AuthRequestDto
import com.undef.fintrackmobile.data.network.dto.AuthResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SincronizacionRepository(
    private val dataApiService: SupabaseDataApiService,
    private val authApiService: SupabaseAuthApiService
) {
    suspend fun getRemoteSupermarkets(): Result<List<SupermarketDto>> = withContext(Dispatchers.IO) {
        try {
            val response = dataApiService.getSupermarkets()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRemoteOffers(): Result<List<OfferDto>> = withContext(Dispatchers.IO) {
        try {
            val response = dataApiService.getOffers()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncPurchase(dto: RemotePurchaseDto): Result<RemotePurchaseResponseDto> = withContext(Dispatchers.IO) {
        try {
            // Supabase returns a list when Prefer: return=representation is used
            val response = dataApiService.syncPurchase(purchase = dto)
            if (response.isNotEmpty()) {
                Result.success(response[0])
            } else {
                Result.failure(Exception("No data returned from Supabase"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(request: AuthRequestDto): Result<AuthResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = authApiService.login(request = request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signup(request: AuthRequestDto): Result<AuthResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = authApiService.signup(request)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncProducts(products: List<Map<String, Any>>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dataApiService.syncProducts(products)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRemoteProfile(userId: String, profile: Map<String, String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dataApiService.updateUser("eq.$userId", profile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
