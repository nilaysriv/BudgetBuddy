package com.nilay.budgetbuddy.data.remote.api

import com.nilay.budgetbuddy.data.remote.dto.BudgetDto
import retrofit2.http.*

interface BudgetService {
    @GET("budgets")
    suspend fun getBudgets(): List<BudgetDto>

    @POST("budgets")
    suspend fun createBudget(@Body budget: BudgetDto): BudgetDto

    @PUT("budgets/{id}")
    suspend fun updateBudget(@Path("id") id: Long, @Body budget: BudgetDto): BudgetDto

    @DELETE("budgets/{id}")
    suspend fun deleteBudget(@Path("id") id: Long)
}
