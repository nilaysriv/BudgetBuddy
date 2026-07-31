package com.nilay.budgetbuddy.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class BudgetDto(
    val id: Long? = null,
    val categoryId: Long,
    val monthYear: String,
    val budgetAmount: Double
)
