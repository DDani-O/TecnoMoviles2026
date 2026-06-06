package com.undef.fintrackmobile.data.network

import com.undef.fintrackmobile.data.network.dto.CompraRemotaDto
import com.undef.fintrackmobile.data.network.dto.CompraRemotaRespuestaDto
import com.undef.fintrackmobile.data.network.dto.SupermercadoDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SuperAhorroApiService {
    @GET("posts")
    suspend fun obtenerSupermercados(): List<SupermercadoDto>

    @POST("posts")
    suspend fun sincronizarCompra(@Body compra: CompraRemotaDto): CompraRemotaRespuestaDto
}
