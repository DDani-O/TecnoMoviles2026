package com.undef.fintrackmobile

import android.content.Context
import com.undef.fintrackmobile.data.local.AppDatabase
import com.undef.fintrackmobile.data.network.NetworkModule
import com.undef.fintrackmobile.data.preferences.UserPreferencesRepository
import com.undef.fintrackmobile.data.repository.ExploreRepository
import com.undef.fintrackmobile.data.repository.PurchaseRepository
import com.undef.fintrackmobile.data.repository.SincronizacionRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val preferencesRepository = UserPreferencesRepository(appContext)

    private val database: AppDatabase = AppDatabase.getDatabase(appContext)

    val purchaseRepository = PurchaseRepository(database.purchaseDao(), appContext)
    val exploreRepository = ExploreRepository(NetworkModule.apiService)
    val sincronizacionRepository = SincronizacionRepository(NetworkModule.superAhorroApiService)
}
