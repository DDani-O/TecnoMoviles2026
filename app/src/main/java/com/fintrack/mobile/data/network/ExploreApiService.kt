package com.fintrack.mobile.data.network

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Path

interface ExploreApiService {
    @GET("latest/{base}")
    suspend fun getRates(@Path("base") base: String): ExchangeRateResponse
}

data class ExchangeRateResponse(
    @param:Json(name = "result") val result: String?,
    @param:Json(name = "base_code") val baseCode: String?,
    @param:Json(name = "rates") val rates: Map<String, Double>?,
)
