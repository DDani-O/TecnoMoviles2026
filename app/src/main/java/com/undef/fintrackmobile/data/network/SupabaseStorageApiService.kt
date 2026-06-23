package com.undef.fintrackmobile.data.network

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface SupabaseStorageApiService {
    @POST("object/tickets/{path}")
    suspend fun uploadFile(
        @Path("path") path: String,
        @Body file: RequestBody
    )
}
