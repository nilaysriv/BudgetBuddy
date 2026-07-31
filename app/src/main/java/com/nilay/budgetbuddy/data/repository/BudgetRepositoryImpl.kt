package com.nilay.budgetbuddy.data.repository

import com.nilay.budgetbuddy.data.local.dao.BudgetDao
import com.nilay.budgetbuddy.data.local.dao.CategoryDao
import com.nilay.budgetbuddy.data.local.dao.TransactionDao
import com.nilay.budgetbuddy.data.local.entity.Budget
import com.nilay.budgetbuddy.data.local.entity.Category
import com.nilay.budgetbuddy.data.local.entity.Transaction
import com.nilay.budgetbuddy.data.mapper.toDto
import com.nilay.budgetbuddy.data.mapper.toEntity
import com.nilay.budgetbuddy.data.remote.api.BudgetService
import com.nilay.budgetbuddy.data.remote.api.CategoryService
import com.nilay.budgetbuddy.data.remote.api.TransactionService
import com.nilay.budgetbuddy.domain.model.TransactionType
import com.nilay.budgetbuddy.domain.model.TransactionWithCategory
import com.nilay.budgetbuddy.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val transactionService: TransactionService,
    private val categoryService: CategoryService,
    private val budgetService: BudgetService
) : BudgetRepository {

    private val TAG = "BudgetRepositoryImpl"

    override fun getAllTransactions(): Flow<List<TransactionWithCategory>> = transactionDao.getAllTransactions()

    override suspend fun getTransactionById(id: Long): Transaction? = transactionDao.getTransactionById(id)

    override suspend fun insertTransaction(transaction: Transaction) {
        val id = transactionDao.insertTransaction(transaction)
        try {
            transactionService.createTransaction(transaction.copy(id = id).toDto())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync transaction to remote", e)
        }
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
        try {
            transactionService.updateTransaction(transaction.id, transaction.toDto())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update transaction on remote", e)
        }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
        try {
            transactionService.deleteTransaction(transaction.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete transaction from remote", e)
        }
    }

    override fun getTransactionsByCategory(categoryId: Long): Flow<List<TransactionWithCategory>> = 
        transactionDao.getTransactionsByCategory(categoryId)

    override fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    override suspend fun getCategoryById(id: Long): Category? = categoryDao.getCategoryById(id)

    override suspend fun insertCategory(category: Category) {
        val id = categoryDao.insertCategory(category)
        try {
            categoryService.createCategory(category.copy(id = id).toDto())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync category to remote", e)
        }
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
        try {
            categoryService.updateCategory(category.id, category.toDto())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update category on remote", e)
        }
    }

    override suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
        try {
            categoryService.deleteCategory(category.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete category from remote", e)
        }
    }

    override fun getCategoriesByType(type: TransactionType): Flow<List<Category>> = 
        categoryDao.getCategoriesByType(type)

    override fun getBudgetsByMonth(monthYear: String): Flow<List<Budget>> = budgetDao.getBudgetsByMonth(monthYear)

    override suspend fun getBudgetById(id: Long): Budget? = budgetDao.getBudgetById(id)

    override suspend fun insertBudget(budget: Budget) {
        val id = budgetDao.insertBudget(budget)
        try {
            budgetService.createBudget(budget.copy(id = id).toDto())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync budget to remote", e)
        }
    }

    override suspend fun updateBudget(budget: Budget) {
        budgetDao.updateBudget(budget)
        try {
            budgetService.updateBudget(budget.id, budget.toDto())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update budget on remote", e)
        }
    }

    override suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
        try {
            budgetService.deleteBudget(budget.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete budget from remote", e)
        }
    }

    override suspend fun getBudgetForCategory(categoryId: Long, monthYear: String): Budget? = 
        budgetDao.getBudgetForCategory(categoryId, monthYear)

    override suspend fun syncAll() {
        try {
            // Sync Categories first because of foreign key constraints
            val remoteCategories = categoryService.getCategories()
            remoteCategories.forEach { categoryDao.insertCategory(it.toEntity()) }

            // Sync Budgets
            val remoteBudgets = budgetService.getBudgets()
            remoteBudgets.forEach { budgetDao.insertBudget(it.toEntity()) }

            // Sync Transactions
            val remoteTransactions = transactionService.getTransactions()
            remoteTransactions.forEach { transactionDao.insertTransaction(it.toEntity()) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync all data", e)
        }
    }
}
