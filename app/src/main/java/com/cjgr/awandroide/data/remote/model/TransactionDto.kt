package com.cjgr.awandroide.data.remote.model

data class TransactionDto(
    val id: Int? = null,
    val userId: Int,
    val fecha: String,
    val monto: Double,
    val descripcion: String,
    val tipo: String = "transferencia"
)