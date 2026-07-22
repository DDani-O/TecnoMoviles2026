package com.undef.fintrackmobile.data.network

import com.undef.fintrackmobile.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {
    private const val SUPABASE_URL = "https://ruqmvzmmuscalvihlcva.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_mwpYPau6mCXolCSs0ny1Mw_UXM8PKzv"
    private const val GROQ_URL = "https://api.groq.com/openai/v1/"
    private const val GROQ_API_KEY = BuildConfig.GROQ_API_KEY

    private var authToken: String? = null

    /**
     * Permite actualizar el token de autenticación dinámicamente.
     */
    fun setAuthToken(token: String?) {
        authToken = token
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val requestBuilder = chain.request().newBuilder()
            
            // Siempre enviamos la apikey de Supabase
            requestBuilder.addHeader("apikey", SUPABASE_ANON_KEY)
            
            // Solo enviamos Authorization si tenemos un token de usuario real
            // Esto evita que peticiones públicas fallen con 401 si el token expiró
            if (!authToken.isNullOrBlank()) {
                android.util.Log.d("NetworkModule", "Interceptor: Adding Authorization header with User Token")
                requestBuilder.addHeader("Authorization", "Bearer $authToken")
            } else {
                android.util.Log.d("NetworkModule", "Interceptor: No user token, sending only apikey")
            }

            val request = requestBuilder.build()
            val response = chain.proceed(request)
            
            if (response.code == 401 && !authToken.isNullOrBlank()) {
                android.util.Log.e("NetworkModule", "401 Unauthorized detected! Token might be expired.")
            }
            
            response
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

    private val groqOkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $GROQ_API_KEY")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            },
        )
        .build()

    val groqApiService: GroqApiService = Retrofit.Builder()
        .baseUrl(GROQ_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(groqOkHttpClient)
        .build()
        .create(GroqApiService::class.java)

    val supabaseStorageApiService: SupabaseStorageApiService = Retrofit.Builder()
        .baseUrl("$SUPABASE_URL/storage/v1/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()
        .create(SupabaseStorageApiService::class.java)

    // Eliminamos los servicios antiguos de MockAPI una vez migrados
}
