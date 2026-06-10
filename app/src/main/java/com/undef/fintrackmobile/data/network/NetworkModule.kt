package com.undef.fintrackmobile.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            },
        )
        .build()

    /**
     * superAhorroApiService: Cliente para MockAPI Proyecto 1 (Supermercados y Ofertas).
     */
    val superAhorroApiService: SuperAhorroApiService = Retrofit.Builder()
        .baseUrl("https://6a2878d04e1e783349a58dab.mockapi.io/api/v1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()
        .create(SuperAhorroApiService::class.java)

    /**
     * syncApiService: Cliente para MockAPI Proyecto 2 (Sincronización de Compras).
     */
    val syncApiService: SyncApiService = Retrofit.Builder()
        .baseUrl("https://6a288bf14e1e783349a59edf.mockapi.io/api/v1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()
        .create(SyncApiService::class.java)
}
