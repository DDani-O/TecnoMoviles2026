package com.undef.fintrackmobile.data.repository

import com.undef.fintrackmobile.data.network.ExploreApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class RateResult {
    data class Success(val rate: Double) : RateResult()
    data class Fallback(val rate: Double) : RateResult()
}

class ExploreRepository(private val apiService: ExploreApiService) {
    /**
     * 8️⃣ CORRUTINAS - Cambio de Dispatcher
     * Usamos 'withContext(Dispatchers.IO)' para asegurar que el networking se ejecute
     * en hilos dedicados a entrada/salida, manteniendo la UI fluida.
     */
    suspend fun getRate(base: String, target: String): RateResult = withContext(Dispatchers.IO) {
        return@withContext try {
            // Llamada a la API externa vía Retrofit
            val response = apiService.getRates(base)
            val rate = response.rates?.get(target)
            if (rate != null) {
                RateResult.Success(rate)
            } else {
                // 7️⃣ NETWORKING - Fallback local si el dato no viene de la API
                RateResult.Fallback(LocalRates.DEFAULT_RATE)
            }
        } catch (_: Throwable) {
            // 7️⃣ NETWORKING - Fallback ante errores de red (offline, timeout)
            RateResult.Fallback(LocalRates.DEFAULT_RATE)
        }
    }
}

private object LocalRates {
    const val DEFAULT_RATE = 900.0
}
