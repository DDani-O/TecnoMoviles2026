package com.undef.fintrackmobile.data.network

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Path

/*
 * 7️⃣ NETWORKING - Retrofit
 * Definimos la interfaz del servicio web. Retrofit genera la implementación.
 */
interface ExploreApiService {
    /**
     * @GET define el endpoint relativo.
     * @Path sustituye dinámicamente partes de la URL.
     * suspend garantiza que la llamada HTTP no bloquee la UI.
     */
    @GET("latest/{base}")
    suspend fun getRates(@Path("base") base: String): ExchangeRateResponse
}

/**
 * Data Class para mapear la respuesta JSON.
 * Moshi se encarga de la serialización/deserialización.
 */
data class ExchangeRateResponse(
    @param:Json(name = "result") val result: String?,
    @param:Json(name = "base_code") val baseCode: String?,
    @param:Json(name = "rates") val rates: Map<String, Double>?,
)
