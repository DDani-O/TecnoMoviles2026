package com.undef.fintrackmobile.data.repository

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import android.util.Log
import com.undef.fintrackmobile.data.network.SupabaseAuthApiService
import com.undef.fintrackmobile.data.network.SupabaseDataApiService
import com.undef.fintrackmobile.data.network.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class SincronizacionRepository(
    private val dataApiService: SupabaseDataApiService,
    private val authApiService: SupabaseAuthApiService,
    private val storageApiService: com.undef.fintrackmobile.data.network.SupabaseStorageApiService
) {
    suspend fun uploadTicketImage(fileName: String, bytes: ByteArray): Result<String> = withContext(Dispatchers.IO) {
        try {
            val mediaType = "image/jpeg".toMediaTypeOrNull()
            val requestBody = bytes.toRequestBody(mediaType)
            storageApiService.uploadFile(fileName, requestBody)
            // Assuming the bucket is public or we use the public URL format
            val publicUrl = "https://ruqmvzmmuscalvihlcva.supabase.co/storage/v1/object/public/tickets/$fileName"
            Result.success(publicUrl)
        } catch (e: Exception) {
            logError("uploadTicketImage", e)
            Result.failure(e)
        }
    }
    suspend fun getRemoteSupermarkets(): Result<List<SupermarketDto>> = withContext(Dispatchers.IO) {
        try {
            val response = dataApiService.getSupermarkets()
            Result.success(response)
        } catch (e: Exception) {
            logError("getRemoteSupermarkets", e)
            Result.failure(e)
        }
    }

    suspend fun getRemoteOffers(): Result<List<OfferDto>> = withContext(Dispatchers.IO) {
        try {
            val response = dataApiService.getOffers()
            Result.success(response)
        } catch (e: Exception) {
            logError("getRemoteOffers", e)
            Result.failure(e)
        }
    }

    suspend fun syncPurchase(dto: RemotePurchaseDto): Result<RemotePurchaseResponseDto> = withContext(Dispatchers.IO) {
        try {
            Log.d("SincronizacionRepo", "Syncing purchase to Supabase: $dto")
            // Supabase returns a list when Prefer: return=representation is used
            val response = dataApiService.syncPurchase(purchase = dto)
            Log.d("SincronizacionRepo", "Sync response: $response")
            if (response.isNotEmpty()) {
                Result.success(response[0])
            } else {
                Result.failure(Exception("No data returned from Supabase"))
            }
        } catch (e: Exception) {
            logError("syncPurchase", e)
            Result.failure(e)
        }
    }

    private fun logError(tag: String, e: Exception) {
        if (e is HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("SincronizacionRepo", "$tag error: HTTP ${e.code()} - $errorBody")
        } else {
            Log.e("SincronizacionRepo", "$tag error: ${e.message}", e)
        }
    }

    suspend fun login(request: AuthRequestDto): Result<AuthResponseDto> = withContext(Dispatchers.IO) {
        try {
            Log.d("SincronizacionRepo", "Starting login for: ${request.email}")
            val response = authApiService.login(request = request)
            Log.d("SincronizacionRepo", "Login success for: ${request.email}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("SincronizacionRepo", "Login error for ${request.email}: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signup(request: AuthRequestDto): Result<AuthResponseDto> = withContext(Dispatchers.IO) {
        try {
            Log.d("SincronizacionRepo", "Starting signup for: ${request.email}")
            val response = authApiService.signup(request)
            Log.d("SincronizacionRepo", "Signup success for: ${request.email}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("SincronizacionRepo", "Signup error for ${request.email}: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun syncProducts(products: List<Map<String, Any>>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dataApiService.syncProducts(prefer = "return=minimal", products = products)
            Result.success(Unit)
        } catch (e: Exception) {
            logError("syncProducts", e)
            Result.failure(e)
        }
    }

    suspend fun updateRemotePurchase(remoteId: Int, dto: RemotePurchaseDto): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dataApiService.updatePurchase("eq.$remoteId", dto)
            Result.success(Unit)
        } catch (e: Exception) {
            logError("updateRemotePurchase", e)
            Result.failure(e)
        }
    }

    suspend fun deleteRemoteProducts(remotePurchaseId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dataApiService.deleteProducts("eq.$remotePurchaseId")
            Result.success(Unit)
        } catch (e: Exception) {
            logError("deleteRemoteProducts", e)
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

    suspend fun deleteRemotePurchase(remoteId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dataApiService.deletePurchase("eq.$remoteId")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
