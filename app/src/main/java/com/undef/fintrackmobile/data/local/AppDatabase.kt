package com.undef.fintrackmobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.undef.fintrackmobile.data.local.dao.ProductDao
import com.undef.fintrackmobile.data.local.dao.PurchaseDao
import com.undef.fintrackmobile.data.local.entity.ProductEntity
import com.undef.fintrackmobile.data.local.entity.PurchaseEntity

/*
 * 6️⃣ BASE DE DATOS LOCAL - Room
 * Room es la capa de abstracción sobre SQLite. Proporciona persistencia local robusta.
 * Definimos las entidades (tablas) y la versión de la base de datos.
 */
@Database(
    entities = [PurchaseEntity::class, ProductEntity::class],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    // El DAO es el contrato de acceso a los datos
    abstract fun purchaseDao(): PurchaseDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fintrack.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
