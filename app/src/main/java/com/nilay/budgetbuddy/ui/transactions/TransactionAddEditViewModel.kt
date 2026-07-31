package com.nilay.budgetbuddy.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nilay.budgetbuddy.data.local.entity.Category
import com.nilay.budgetbuddy.data.local.entity.Transaction
import com.nilay.budgetbuddy.domain.model.TransactionType
import com.nilay.budgetbuddy.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class TransactionAddEditUiState(
    val id: Long = 0,
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedCategory: Category? = null,
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false,
    val isEntryValid: Boolean = false,
    val isNewTransaction: Boolean = true
)

@HiltViewModel
class TransactionAddEditViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionAddEditUiState())
    val uiState: StateFlow<TransactionAddEditUiState> = _uiState.asStateFlow()

    val categories = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadTransaction(id: Long?) {
        if (id == null || id == 0L) {
            _uiState.value = TransactionAddEditUiState(isNewTransaction = true)
            return
        }
        viewModelScope.launch {
            repository.getTransactionById(id)?.let { transaction ->
                val category = repository.getCategoryById(transaction.categoryId)
                _uiState.value = TransactionAddEditUiState(
                    id = transaction.id,
                    amount = transaction.amount.toString(),
                    type = transaction.type,
                    selectedCategory = category,
                    date = transaction.date,
                    note = transaction.note ?: "",
                    isNewTransaction = false
                )
                validateEntry()
            }
        }
    }

    fun onAmountChanged(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
        validateEntry()
    }

    fun onTypeChanged(type: TransactionType) {
        _uiState.value = _uiState.value.copy(type = type, selectedCategory = null)
        validateEntry()
    }

    fun onCategorySelected(category: Category) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        validateEntry()
    }

    fun onDateChanged(date: Long) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun onNoteChanged(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    private fun validateEntry() {
        val amount = _uiState.value.amount.toDoubleOrNull()
        val isValid = amount != null && amount > 0 && _uiState.value.selectedCategory != null
        _uiState.value = _uiState.value.copy(isEntryValid = isValid)
    }

    fun saveTransaction(onSuccess: () -> Unit) {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull() ?: return
        val categoryId = state.selectedCategory?.id ?: return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            val transaction = Transaction(
                id = state.id,
                amount = amount,
                type = state.type,
                categoryId = categoryId,
                date = state.date,
                note = state.note.ifBlank { null }
            )
            if (state.isNewTransaction) {
                repository.insertTransaction(transaction)
            } else {
                repository.updateTransaction(transaction)
            }
            onSuccess()
        }
    }
}
