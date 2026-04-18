package com.cjgr.awandroide.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val correo: String,
    val password: String,
    val saldo: Double = 0.0,
    val fotoPerfil: String? = null,
    val token: String? = null
)