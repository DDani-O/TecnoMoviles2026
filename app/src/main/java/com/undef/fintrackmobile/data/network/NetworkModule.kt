package com.undef.fintrackmobile.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {
    private const val SUPABASE_URL = "https://ruqmvzmmuscalvihlcva.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_mwpYPau6mCXolCSs0ny1Mw_UXM8PKzv"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer $SUPABASE_ANON_KEY")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            },
        )
        .build()

    /**
     * supabaseAuthApiService: Cliente para Supabase Auth API.
     */
    val supabaseAuthApiService: SupabaseAuthApiService = Retrofit.Builder()
        .baseUrl("$SUPABASE_URL/auth/v1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()
        .create(SupabaseAuthApiService::class.java)

    /**
     * supabaseDataApiService: Cliente para Supabase PostgREST API (Tablas).
     */
    val supabaseDataApiService: SupabaseDataApiService = Retrofit.Builder()
        .baseUrl("$SUPABASE_URL/rest/v1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()
        .create(SupabaseDataApiService::class.java)

    // Eliminamos los servicios antiguos de MockAPI una vez migrados
}
