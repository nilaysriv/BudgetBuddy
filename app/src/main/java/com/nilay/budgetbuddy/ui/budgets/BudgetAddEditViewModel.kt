package com.nilay.budgetbuddy.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nilay.budgetbuddy.data.local.entity.Budget
import com.nilay.budgetbuddy.data.local.entity.Category
import com.nilay.budgetbuddy.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

private val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

data class BudgetAddEditUiState(
    val id: Long = 0,
    val selectedCategory: Category? = null,
    val monthYear: String = monthYearFormat.format(Calendar.getInstance().time),
    val amount: String = "",
    val isSaving: Boolean = false,
    val isEntryValid: Boolean = false,
    val isNewBudget: Boolean = true
)

@HiltViewModel
class BudgetAddEditViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetAddEditUiState())
    val uiState: StateFlow<BudgetAddEditUiState> = _uiState.asStateFlow()

    val categories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadBudget(id: Long?) {
        if (id == null || id == 0L) {
            _uiState.value = BudgetAddEditUiState(isNewBudget = true)
            return
        }
        viewModelScope.launch {
            repository.getBudgetById(id)?.let { budget ->
                val category = repository.getCategoryById(budget.categoryId)
                _uiState.value = BudgetAddEditUiState(
                    id = budget.id,
                    selectedCategory = category,
                    monthYear = budget.monthYear,
                    amount = budget.budgetAmount.toString(),
                    isNewBudget = false
                )
                validateEntry()
            }
        }
    }

    fun onCategorySelected(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        validateEntry()
    }

    fun previousMonth() = shiftMonth(-1)
    fun nextMonth() = shiftMonth(1)

    private fun shiftMonth(delta: Int) {
        val calendar = Calendar.getInstance()
        monthYearFormat.parse(_uiState.value.monthYear)?.let { calendar.time = it }
        calendar.add(Calendar.MONTH, delta)
        _uiState.value = _uiState.value.copy(monthYear = monthYearFormat.format(calendar.time))
    }

    fun onAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
        validateEntry()
    }

    private fun validateEntry() {
        val amount = _uiState.value.amount.toDoubleOrNull()
        val isValid = amount != null && amount > 0 && _uiState.value.selectedCategory != null
        _uiState.value = _uiState.value.copy(isEntryValid = isValid)
    }

    fun saveBudget(onSuccess: () -> Unit) {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull() ?: return
        val categoryId = state.selectedCategory?.id ?: return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            val budget = Budget(
                id = state.id,
                categoryId = categoryId,
                monthYear = state.monthYear,
                budgetAmount = amount
            )
            if (state.isNewBudget) {
                repository.insertBudget(budget)
            } else {
                repository.updateBudget(budget)
            }
            onSuccess()
        }
    }
}
