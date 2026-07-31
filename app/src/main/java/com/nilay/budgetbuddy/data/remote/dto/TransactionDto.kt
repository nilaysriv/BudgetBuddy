package com.nilay.budgetbuddy.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    val id: Long? = null,
    val amount: Double,
    val type: String,
    val categoryId: Long,
    val date: String,
    val note: String? = null
)
