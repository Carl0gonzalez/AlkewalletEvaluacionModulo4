package com.cjgr.awandroide.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY id DESC")
    suspend fun getTransactionsByUser(userId: Int): List<TransactionEntity>

    @Query("SELECT * FROM transactions ORDER BY id DESC")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getTransactionById(transactionId: Int): TransactionEntity?

    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteTransactionsByUser(userId: Int)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}