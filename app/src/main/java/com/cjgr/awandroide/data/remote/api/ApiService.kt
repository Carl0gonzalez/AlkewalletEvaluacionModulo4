package com.cjgr.awandroide.data.remote.api

import com.cjgr.awandroide.data.remote.model.TransactionDto
import com.cjgr.awandroide.data.remote.model.TransactionRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("transactions")
    suspend fun getTransactions(): Response<List<TransactionDto>>

    @POST("transactions")
    suspend fun createTransaction(
        @Body request: TransactionRequest
    ): Response<TransactionDto>
}