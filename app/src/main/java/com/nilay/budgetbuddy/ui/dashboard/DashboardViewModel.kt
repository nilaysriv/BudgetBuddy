package com.nilay.budgetbuddy.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nilay.budgetbuddy.data.local.entity.Budget
import com.nilay.budgetbuddy.data.repository.AuthRepository
import com.nilay.budgetbuddy.domain.model.TransactionType
import com.nilay.budgetbuddy.domain.model.TransactionWithCategory
import com.nilay.budgetbuddy.domain.model.TrendPoint
import com.nilay.budgetbuddy.domain.repository.BudgetRepository
import com.nilay.budgetbuddy.util.spentForCategoryInMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class DashboardUiState(
    val greetingName: String? = null,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val topCategories: List<CategorySpending> = emptyList(),
    val budgetProgress: List<BudgetProgress> = emptyList(),
    val recentTransactions: List<TransactionWithCategory> = emptyList(),
    val weeklyTrend: List<TrendPoint> = emptyList(),
    val isLoading: Boolean = true
)

data class CategorySpending(
    val categoryName: String,
    val amount: Double,
    val colorHex: String
)

data class BudgetProgress(
    val categoryName: String,
    val spent: Double,
    val limit: Double,
    val colorHex: String
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: BudgetRepository,
    authRepository: AuthRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.syncAll()
        }
    }

    private val currentMonthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().time)

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getAllTransactions(),
        repository.getBudgetsByMonth(currentMonthYear),
        repository.getAllCategories(),
        authRepository.userNameFlow
    ) { transactions, budgets, categories, userName ->
        val income = transactions.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
        val expense = transactions.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }

        val categorySpending = transactions
            .filter { it.transaction.type == TransactionType.EXPENSE }
            .groupBy { it.category.id }
            .map { (categoryId, categoryTransactions) ->
                val category = categories.find { it.id == categoryId }
                CategorySpending(
                    categoryName = category?.name ?: "Unknown",
                    amount = categoryTransactions.sumOf { it.transaction.amount },
                    colorHex = category?.colorHex ?: "#CCCCCC"
                )
            }
            .sortedByDescending { it.amount }
            .take(5)

        val progress = budgets.map { budget ->
            val category = categories.find { it.id == budget.categoryId }
            val spent = transactions.spentForCategoryInMonth(budget.categoryId, budget.monthYear)
            BudgetProgress(
                categoryName = category?.name ?: "Unknown",
                spent = spent,
                limit = budget.budgetAmount,
                colorHex = category?.colorHex ?: "#CCCCCC"
            )
        }

        DashboardUiState(
            greetingName = userName?.trim()?.substringBefore(" ")?.takeIf { it.isNotBlank() },
            totalIncome = income,
            totalExpense = expense,
            balance = income - expense,
            topCategories = categorySpending,
            budgetProgress = progress,
            recentTransactions = transactions.take(5),
            weeklyTrend = last7DaysTrend(transactions),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    private fun last7DaysTrend(transactions: List<TransactionWithCategory>): List<TrendPoint> {
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val keyFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        return (6 downTo 0).map { daysAgo ->
            val day = calendar.clone() as Calendar
            day.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val dayKey = keyFormat.format(day.time)

            val dayTransactions = transactions.filter { keyFormat.format(java.util.Date(it.transaction.date)) == dayKey }
            TrendPoint(
                label = dayFormat.format(day.time),
                income = dayTransactions.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount },
                expense = dayTransactions.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.syncAll()
        }
    }
}
