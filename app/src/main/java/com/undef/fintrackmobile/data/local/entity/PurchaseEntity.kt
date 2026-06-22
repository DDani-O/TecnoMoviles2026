package com.undef.fintrackmobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userEmail: String = "", 
    val supermarketName: String,
    val dateMillis: Long,
    val totalCents: Long,
    val reason: String = "",
    val remoteId: Int? = null,
    val isSynced: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
