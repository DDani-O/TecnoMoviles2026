package com.undef.fintrackmobile.data.repository

import com.undef.fintrackmobile.data.network.ExploreApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class RateResult {
    data class Success(val rate: Double) : RateResult()
    data class Fallback(val rate: Double) : RateResult()
}

class ExploreRepository(private val apiService: ExploreApiService) {
    suspend fun getRate(base: String, target: String): RateResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val response = apiService.getRates(base)
            val rate = response.rates?.get(target)
            if (rate != null) {
                RateResult.Success(rate)
            } else {
                RateResult.Fallback(LocalRates.DEFAULT_RATE)
            }
        } catch (_: Throwable) {
            RateResult.Fallback(LocalRates.DEFAULT_RATE)
        }
    }
}

private object LocalRates {
    const val DEFAULT_RATE = 900.0
}
