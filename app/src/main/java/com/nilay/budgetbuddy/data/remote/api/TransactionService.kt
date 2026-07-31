package com.nilay.budgetbuddy.data.remote.api

import com.nilay.budgetbuddy.data.remote.dto.TransactionDto
import retrofit2.http.*

interface TransactionService {
    @GET("transactions")
    suspend fun getTransactions(): List<TransactionDto>

    @POST("transactions")
    suspend fun createTransaction(@Body transaction: TransactionDto): TransactionDto

    @PUT("transactions/{id}")
    suspend fun updateTransaction(@Path("id") id: Long, @Body transaction: TransactionDto): TransactionDto

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: Long)
}
