package com.undef.fintrackmobile.data.network

import com.undef.fintrackmobile.data.network.dto.GroqChatResponse
import com.undef.fintrackmobile.data.network.dto.GroqRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface GroqApiService {
    @POST("chat/completions")
    suspend fun getChatCompletion(
        @Body request: GroqRequest
    ): GroqChatResponse
}
