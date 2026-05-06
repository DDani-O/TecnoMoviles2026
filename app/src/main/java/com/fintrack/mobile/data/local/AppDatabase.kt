package com.fintrack.mobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fintrack.mobile.data.local.dao.PurchaseDao
import com.fintrack.mobile.data.local.entity.ProductEntity
import com.fintrack.mobile.data.local.entity.PurchaseEntity

@Database(
    entities = [PurchaseEntity::class, ProductEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun purchaseDao(): PurchaseDao
}
