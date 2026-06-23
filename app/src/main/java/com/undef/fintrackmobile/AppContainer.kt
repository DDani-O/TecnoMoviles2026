package com.undef.fintrackmobile

import android.app.Application
import android.content.Context
import com.undef.fintrackmobile.data.local.AppDatabase
import com.undef.fintrackmobile.data.network.NetworkModule
import com.undef.fintrackmobile.data.preferences.UserPreferencesRepository
import com.undef.fintrackmobile.data.repository.GroqRepository
import com.undef.fintrackmobile.data.repository.PurchaseRepository
import com.undef.fintrackmobile.data.repository.SincronizacionRepository

class AppContainer(context: Context) {
    val appContext = context.applicationContext as Application

    val preferencesRepository = UserPreferencesRepository(appContext)

    private val database: AppDatabase = AppDatabase.getDatabase(appContext)

    val sincronizacionRepository = SincronizacionRepository(
        NetworkModule.supabaseDataApiService,
        NetworkModule.supabaseAuthApiService,
        NetworkModule.supabaseStorageApiService
    )

    val groqRepository = GroqRepository(
        NetworkModule.groqApiService,
        com.squareup.moshi.Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()
    )

    val purchaseRepository = PurchaseRepository(
        purchaseDao = database.purchaseDao(),
        productDao = database.productDao(),
        userPreferencesRepository = preferencesRepository,
        sincronizacionRepository = sincronizacionRepository
    )
}
