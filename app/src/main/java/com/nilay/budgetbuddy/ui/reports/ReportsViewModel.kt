package com.nilay.budgetbuddy.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nilay.budgetbuddy.data.local.entity.Budget
import com.nilay.budgetbuddy.data.local.entity.Category
import com.nilay.budgetbuddy.domain.model.TransactionType
import com.nilay.budgetbuddy.domain.model.TransactionWithCategory
import com.nilay.budgetbuddy.domain.model.TrendPoint
import com.nilay.budgetbuddy.domain.repository.BudgetRepository
import com.nilay.budgetbuddy.ui.dashboard.CategorySpending
import com.nilay.budgetbuddy.util.monthKeyOf
import com.nilay.budgetbuddy.util.spentForCategoryInMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

enum class Period { THIS_MONTH, LAST_MONTH, THIS_YEAR }

data class CategoryTrendSeries(
    val categoryName: String,
    val colorHex: String,
    val monthlyAmounts: List<Double>
)

data class BudgetVarianceItem(
    val categoryName: String,
    val colorHex: String,
    val budgetAmount: Double,
    val actualSpent: Double
)

data class ReportsUiState(
    val period: Period = Period.THIS_MONTH,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val categoryBreakdown: List<CategorySpending> = emptyList(),
    val monthlyComparison: List<TrendPoint> = emptyList(),
    val categoryTrends: List<CategoryTrendSeries> = emptyList(),
    val budgetVariance: List<BudgetVarianceItem> = emptyList(),
    val showBudgetVariance: Boolean = true,
    val isLoading: Boolean = true
)

private val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
private val monthShortLabelFormat = SimpleDateFormat("MMM", Locale.getDefault())

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val period = MutableStateFlow(Period.THIS_MONTH)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ReportsUiState> = period.flatMapLatest { selectedPeriod ->
        val monthKey = periodMonthKey(selectedPeriod)
        combine(
            repository.getAllTransactions(),
            repository.getAllCategories(),
            if (monthKey != null) repository.getBudgetsByMonth(monthKey) else flowOf(emptyList())
        ) { transactions, categories, budgets ->
            buildUiState(selectedPeriod, transactions, categories, budgets, monthKey)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsUiState())

    fun onPeriodSelected(newPeriod: Period) {
        period.value = newPeriod
    }

    private fun buildUiState(
        selectedPeriod: Period,
        transactions: List<TransactionWithCategory>,
        categories: List<Category>,
        budgets: List<Budget>,
        monthKey: String?
    ): ReportsUiState {
        val (start, end) = periodBounds(selectedPeriod)
        val periodTransactions = transactions.filter { it.transaction.date in start..end }

        val income = periodTransactions.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount }
        val expense = periodTransactions.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }

        val expenseByCategory = periodTransactions
            .filter { it.transaction.type == TransactionType.EXPENSE }
            .groupBy { it.category.id }

        val categoryBreakdown = expenseByCategory.map { (categoryId, txns) ->
            val category = categories.find { it.id == categoryId }
            CategorySpending(
                categoryName = category?.name ?: "Unknown",
                amount = txns.sumOf { it.transaction.amount },
                colorHex = category?.colorHex ?: "#CCCCCC"
            )
        }.sortedByDescending { it.amount }

        val monthKeys = last6MonthKeys()
        val monthlyComparison = monthKeys.map { key ->
            val monthTransactions = transactions.filter { monthKeyOf(it.transaction.date) == key }
            TrendPoint(
                label = monthShortLabelFor(key),
                income = monthTransactions.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount },
                expense = monthTransactions.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount }
            )
        }

        val topCategoryIds = expenseByCategory.entries
            .sortedByDescending { (_, txns) -> txns.sumOf { it.transaction.amount } }
            .take(5)
            .map { it.key }

        val categoryTrends = topCategoryIds.map { categoryId ->
            val category = categories.find { it.id == categoryId }
            CategoryTrendSeries(
                categoryName = category?.name ?: "Unknown",
                colorHex = category?.colorHex ?: "#CCCCCC",
                monthlyAmounts = monthKeys.map { key -> transactions.spentForCategoryInMonth(categoryId, key) }
            )
        }

        val budgetVariance = if (monthKey != null) {
            budgets.map { budget ->
                val category = categories.find { it.id == budget.categoryId }
                BudgetVarianceItem(
                    categoryName = category?.name ?: "Unknown",
                    colorHex = category?.colorHex ?: "#CCCCCC",
                    budgetAmount = budget.budgetAmount,
                    actualSpent = transactions.spentForCategoryInMonth(budget.categoryId, monthKey)
                )
            }
        } else {
            emptyList()
        }

        return ReportsUiState(
            period = selectedPeriod,
            totalIncome = income,
            totalExpense = expense,
            balance = income - expense,
            categoryBreakdown = categoryBreakdown,
            monthlyComparison = monthlyComparison,
            categoryTrends = categoryTrends,
            budgetVariance = budgetVariance,
            showBudgetVariance = monthKey != null,
            isLoading = false
        )
    }

    private fun periodMonthKey(period: Period): String? {
        val calendar = Calendar.getInstance()
        return when (period) {
            Period.THIS_MONTH -> monthKeyFormat.format(calendar.time)
            Period.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                monthKeyFormat.format(calendar.time)
            }
            Period.THIS_YEAR -> null
        }
    }

    private fun periodBounds(period: Period): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return when (period) {
            Period.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.timeInMillis to System.currentTimeMillis()
            }
            Period.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                start to calendar.timeInMillis
            }
            Period.THIS_YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.timeInMillis to System.currentTimeMillis()
            }
        }
    }

    private fun last6MonthKeys(): List<String> {
        val calendar = Calendar.getInstance()
        return (5 downTo 0).map { monthsAgo ->
            val month = calendar.clone() as Calendar
            month.add(Calendar.MONTH, -monthsAgo)
            monthKeyFormat.format(month.time)
        }
    }

    private fun monthShortLabelFor(monthYear: String): String = try {
        monthShortLabelFormat.format(monthKeyFormat.parse(monthYear)!!)
    } catch (e: Exception) {
        monthYear
    }
}
