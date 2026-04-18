package com.cjgr.awandroide.data.repository

import com.cjgr.awandroide.data.local.TransactionDao
import com.cjgr.awandroide.data.local.TransactionEntity
import com.cjgr.awandroide.data.remote.api.ApiService
import com.cjgr.awandroide.data.remote.model.TransactionRequest

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val apiService: ApiService
) {

    suspend fun obtenerTransaccionesPorUsuario(userId: Int): List<TransactionEntity> {
        return try {
            val response = apiService.getTransactions()
            if (response.isSuccessful) {
                val remoteTransactions = response.body().orEmpty()

                val filtradas = remoteTransactions
                    .filter { it.userId == userId }
                    .map {
                        TransactionEntity(
                            id = it.id ?: 0,
                            userId = it.userId,
                            fecha = it.fecha,
                            monto = it.monto,
                            descripcion = it.descripcion,
                            tipo = it.tipo
                        )
                    }

                transactionDao.deleteTransactionsByUser(userId)
                transactionDao.insertTransactions(filtradas)

                filtradas
            } else {
                transactionDao.getTransactionsByUser(userId)
            }
        } catch (e: Exception) {
            transactionDao.getTransactionsByUser(userId)
        }
    }

    suspend fun enviarTransaccion(transaction: TransactionEntity): Result<TransactionEntity> {
        return try {
            val request = TransactionRequest(
                userId = transaction.userId,
                fecha = transaction.fecha,
                monto = transaction.monto,
                descripcion = transaction.descripcion,
                tipo = transaction.tipo
            )

            val response = apiService.createTransaction(request)

            if (response.isSuccessful) {
                val body = response.body()

                val savedTransaction = TransactionEntity(
                    id = body?.id ?: transaction.id,
                    userId = body?.userId ?: transaction.userId,
                    fecha = body?.fecha ?: transaction.fecha,
                    monto = body?.monto ?: transaction.monto,
                    descripcion = body?.descripcion ?: transaction.descripcion,
                    tipo = body?.tipo ?: transaction.tipo
                )

                transactionDao.insertTransaction(savedTransaction)
                Result.success(savedTransaction)
            } else {
                Result.failure(Exception("Error al enviar la transacción"))
            }
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