package com.cjgr.awandroide.data.remote.model

data class TransactionRequest(
    val userId: Int,
    val fecha: String,
    val monto: Double,
    val descripcion: String,
    val tipo: String = "transferencia"
)