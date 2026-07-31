package com.nilay.budgetbuddy.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nilay.budgetbuddy.domain.model.TransactionType
import com.nilay.budgetbuddy.domain.model.TransactionWithCategory
import com.nilay.budgetbuddy.data.local.entity.Transaction
import com.nilay.budgetbuddy.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionsUiState(
    val transactions: List<TransactionWithCategory> = emptyList(),
    val filterType: TransactionType? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val selectedTransaction: TransactionWithCategory? = null,
    val error: String? = null
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.syncAll()
        }
    }

    private val _filterType = MutableStateFlow<TransactionType?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedTransactionId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<TransactionsUiState> = combine(
        repository.getAllTransactions(),
        _filterType,
        _searchQuery,
        _selectedTransactionId
    ) { transactions, type, query, selectedId ->
        val filtered = transactions.filter {
            (type == null || it.transaction.type == type) &&
            (query.isEmpty() || it.category.name.contains(query, ignoreCase = true) || 
             it.transaction.note?.contains(query, ignoreCase = true) == true)
        }
        TransactionsUiState(
            transactions = filtered,
            filterType = type,
            searchQuery = query,
            isLoading = false,
            selectedTransaction = transactions.find { it.transaction.id == selectedId }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionsUiState()
    )

    fun onFilterTypeSelected(type: TransactionType?) {
        _filterType.value = type
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onTransactionSelected(id: Long?) {
        _selectedTransactionId.value = id
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            if (_selectedTransactionId.value == transaction.id) {
                _selectedTransactionId.value = null
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                repository.syncAll()
            } catch (e: Exception) {
                // We could update the state with an error here if we wanted to show a snackbar
            }
        }
    }
}
