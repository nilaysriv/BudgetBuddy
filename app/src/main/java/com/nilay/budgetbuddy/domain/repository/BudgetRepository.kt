package com.nilay.budgetbuddy.domain.repository

import com.nilay.budgetbuddy.data.local.entity.Budget
import com.nilay.budgetbuddy.data.local.entity.Category
import com.nilay.budgetbuddy.data.local.entity.Transaction
import com.nilay.budgetbuddy.domain.model.TransactionType
import com.nilay.budgetbuddy.domain.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    // Transactions
    fun getAllTransactions(): Flow<List<TransactionWithCategory>>
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
    fun getTransactionsByCategory(categoryId: Long): Flow<List<TransactionWithCategory>>

    // Categories
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun insertCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(category: Category)
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>>

    // Budgets
    fun getBudgetsByMonth(monthYear: String): Flow<List<Budget>>
    suspend fun getBudgetById(id: Long): Budget?
    suspend fun insertBudget(budget: Budget)
    suspend fun updateBudget(budget: Budget)
    suspend fun deleteBudget(budget: Budget)
    suspend fun getBudgetForCategory(categoryId: Long, monthYear: String): Budget?

    suspend fun syncAll()
}
