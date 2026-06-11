package com.undef.fintrackmobile

import android.app.Application
import android.content.Context
import com.undef.fintrackmobile.data.local.AppDatabase
import com.undef.fintrackmobile.data.network.NetworkModule
import com.undef.fintrackmobile.data.preferences.UserPreferencesRepository
import com.undef.fintrackmobile.data.repository.PurchaseRepository
import com.undef.fintrackmobile.data.repository.SincronizacionRepository

class AppContainer(context: Context) {
    val appContext = context.applicationContext as Application

    val preferencesRepository = UserPreferencesRepository(appContext)

    private val database: AppDatabase = AppDatabase.getDatabase(appContext)

    val purchaseRepository = PurchaseRepository(
        purchaseDao = database.purchaseDao(),
        productDao = database.productDao(),
        userPreferencesRepository = preferencesRepository
    )
    val sincronizacionRepository = SincronizacionRepository(
        NetworkModule.superAhorroApiService,
        NetworkModule.syncApiService
    )
}
