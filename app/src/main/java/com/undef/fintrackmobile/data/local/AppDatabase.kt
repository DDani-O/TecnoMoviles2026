package com.undef.fintrackmobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.undef.fintrackmobile.data.local.dao.PurchaseDao
import com.undef.fintrackmobile.data.local.entity.ProductEntity
import com.undef.fintrackmobile.data.local.entity.PurchaseEntity

@Database(
    entities = [PurchaseEntity::class, ProductEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun purchaseDao(): PurchaseDao
}
