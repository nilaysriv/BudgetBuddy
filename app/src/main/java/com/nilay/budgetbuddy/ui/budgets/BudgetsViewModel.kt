package com.nilay.budgetbuddy.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nilay.budgetbuddy.data.local.entity.Budget
import com.nilay.budgetbuddy.domain.repository.BudgetRepository
import com.nilay.budgetbuddy.util.spentForCategoryInMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class BudgetListItem(
    val budget: Budget,
    val categoryName: String,
    val colorHex: String,
    val spent: Double
)

data class BudgetsUiState(
    val monthYear: String = "",
    val monthLabel: String = "",
    val items: List<BudgetListItem> = emptyList(),
    val isLoading: Boolean = true
)

private val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
private val monthLabelFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val selectedMonth = MutableStateFlow(monthYearFormat.format(Calendar.getInstance().time))

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<BudgetsUiState> = selectedMonth.flatMapLatest { month ->
        combine(
            repository.getBudgetsByMonth(month),
            repository.getAllCategories(),
            repository.getAllTransactions()
        ) { budgets, categories, transactions ->
            val items = budgets.map { budget ->
                val category = categories.find { it.id == budget.categoryId }
                BudgetListItem(
                    budget = budget,
                    categoryName = category?.name ?: "Unknown",
                    colorHex = category?.colorHex ?: "#CCCCCC",
                    spent = transactions.spentForCategoryInMonth(budget.categoryId, month)
                )
            }
            BudgetsUiState(
                monthYear = month,
                monthLabel = monthLabelFor(month),
                items = items,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetsUiState(monthYear = selectedMonth.value, monthLabel = monthLabelFor(selectedMonth.value))
    )

    private fun monthLabelFor(monthYear: String): String = try {
        monthLabelFormat.format(monthYearFormat.parse(monthYear)!!)
    } catch (e: Exception) {
        monthYear
    }

    fun previousMonth() = shiftMonth(-1)
    fun nextMonth() = shiftMonth(1)

    private fun shiftMonth(delta: Int) {
        val calendar = Calendar.getInstance()
        monthYearFormat.parse(selectedMonth.value)?.let { calendar.time = it }
        calendar.add(Calendar.MONTH, delta)
        selectedMonth.value = monthYearFormat.format(calendar.time)
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }
}
