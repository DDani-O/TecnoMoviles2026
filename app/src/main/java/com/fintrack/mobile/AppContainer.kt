package com.fintrack.mobile

import android.content.Context
import androidx.room.Room
import com.fintrack.mobile.data.local.AppDatabase
import com.fintrack.mobile.data.network.NetworkModule
import com.fintrack.mobile.data.preferences.UserPreferencesRepository
import com.fintrack.mobile.data.repository.ExploreRepository
import com.fintrack.mobile.data.repository.PurchaseRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val preferencesRepository = UserPreferencesRepository(appContext)

    private val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "fintrack.db",
    ).fallbackToDestructiveMigration().build()

    val purchaseRepository = PurchaseRepository(database.purchaseDao(), appContext)
    val exploreRepository = ExploreRepository(NetworkModule.apiService)
}
