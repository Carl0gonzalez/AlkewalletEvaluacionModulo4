package com.cjgr.awandroide.data.repository

import com.cjgr.awandroide.data.local.TransactionDao
import com.cjgr.awandroide.data.local.TransactionEntity
import com.cjgr.awandroide.network.AlkeApiService
import com.cjgr.awandroide.network.ApiTransaction
import kotlin.math.abs

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val apiService: AlkeApiService
) {

    suspend fun obtenerTransaccionesPorUsuario(userId: Int): List<TransactionEntity> {
        return try {
            val remoteTransactions = apiService.getTransactions(userId.toString())
            val mapeadas = remoteTransactions.map {
                TransactionEntity(
                    id = it.id.toIntOrNull() ?: 0,
                    userId = it.userId.toIntOrNull() ?: userId,
                    fecha = it.date,
                    monto = if (it.type == "ENVIO") -it.amount else it.amount,
                    descripcion = it.description,
                    tipo = it.type
                )
            }
            transactionDao.deleteTransactionsByUser(userId)
            transactionDao.insertTransactions(mapeadas)
            mapeadas
        } catch (e: Exception) {
            transactionDao.getTransactionsByUser(userId)
        }
    }

    suspend fun enviarTransaccion(transaction: TransactionEntity): Result<TransactionEntity> {
        return try {
            val apiTx = ApiTransaction(
                id = "",
                userId = transaction.userId.toString(),
                type = if (transaction.monto < 0) "ENVIO" else "INGRESO",
                amount = abs(transaction.monto),
                description = transaction.descripcion,
                date = transaction.fecha
            )
            val resultado = apiService.createTransaction(apiTx)
            val saved = transaction.copy(id = resultado.id.toIntOrNull() ?: transaction.id)
            transactionDao.insertTransaction(saved)
            Result.success(saved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun guardarTransaccionLocal(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun obtenerTodasLasTransaccionesLocales(): List<TransactionEntity> {
        return transactionDao.getAllTransactions()
    }

    suspend fun obtenerTransaccionPorId(transactionId: Int): TransactionEntity? {
        return transactionDao.getTransactionById(transactionId)
    }

    suspend fun actualizarTransaccion(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun eliminarTransaccionesDeUsuario(userId: Int) {
        transactionDao.deleteTransactionsByUser(userId)
    }

    suspend fun limpiarTransacciones() {
        transactionDao.deleteAllTransactions()
    }
}
