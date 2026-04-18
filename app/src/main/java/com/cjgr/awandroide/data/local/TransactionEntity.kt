package com.cjgr.awandroide.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val fecha: String,
    val monto: Double,
    val descripcion: String,
    val tipo: String = "transferencia"
)