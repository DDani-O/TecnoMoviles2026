package com.undef.fintrackmobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
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
}
