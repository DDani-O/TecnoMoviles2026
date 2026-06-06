package com.undef.fintrackmobile.data.repository

import com.undef.fintrackmobile.data.network.SuperAhorroApiService
import com.undef.fintrackmobile.data.network.dto.CompraRemotaDto
import com.undef.fintrackmobile.data.network.dto.CompraRemotaRespuestaDto
import com.undef.fintrackmobile.data.network.dto.SupermercadoDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SincronizacionRepository(
    private val apiService: SuperAhorroApiService
) {
    suspend fun obtenerSupermercadosRemotos(): Result<List<SupermercadoDto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.obtenerSupermercados()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sincronizarCompra(dto: CompraRemotaDto): Result<CompraRemotaRespuestaDto> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.sincronizarCompra(dto)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
