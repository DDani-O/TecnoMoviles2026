package com.undef.fintrackmobile.data.network

import com.undef.fintrackmobile.data.network.dto.OfferDto
import com.undef.fintrackmobile.data.network.dto.SupermarketDto
import retrofit2.http.GET

interface SuperAhorroApiService {
    @GET("supermarkets")
    suspend fun getSupermarkets(): List<SupermarketDto>

    @GET("offers")
    suspend fun getOffers(): List<OfferDto>
}
