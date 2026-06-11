package com.undef.fintrackmobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userEmail: String = "", // Default para facilitar la creación de objetos transitorios en la UI
    val supermarketName: String,
    val dateMillis: Long,
    val totalCents: Long,
    val reason: String = ""
)
