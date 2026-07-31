package com.nilay.budgetbuddy.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nilay.budgetbuddy.data.local.entity.Category
import com.nilay.budgetbuddy.domain.model.TransactionType
import com.nilay.budgetbuddy.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryAddEditUiState(
    val id: Long = 0,
    val name: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val iconKey: String = "category",
    val colorHex: String = "#9C27B0",
    val parentCategoryId: Long? = null,
    val isSaving: Boolean = false,
    val isEntryValid: Boolean = false,
    val isNewCategory: Boolean = true
)

@HiltViewModel
class CategoryAddEditViewModel @Inject constructor(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryAddEditUiState())
    val uiState: StateFlow<CategoryAddEditUiState> = _uiState.asStateFlow()

    val allCategories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Only top-level categories of the current type can be a parent — keeps nesting to one level. */
    val availableParents: List<Category>
        get() = allCategories.value.filter {
            it.parentCategoryId == null && it.type == _uiState.value.type && it.id != _uiState.value.id
        }

    fun loadCategory(id: Long?) {
        if (id == null || id == 0L) {
            _uiState.value = CategoryAddEditUiState(isNewCategory = true)
            return
        }
        viewModelScope.launch {
            repository.getCategoryById(id)?.let { category ->
                _uiState.value = CategoryAddEditUiState(
                    id = category.id,
                    name = category.name,
                    type = category.type,
                    iconKey = category.iconKey,
                    colorHex = category.colorHex,
                    parentCategoryId = category.parentCategoryId,
                    isNewCategory = false
                )
                validateEntry()
            }
        }
    }

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
        validateEntry()
    }

    fun onTypeChanged(type: TransactionType) {
        // parent must match type, so switching type drops an incompatible parent selection
        _uiState.value = _uiState.value.copy(type = type, parentCategoryId = null)
    }

    fun onIconSelected(iconKey: String) {
        _uiState.value = _uiState.value.copy(iconKey = iconKey)
    }

    fun onColorSelected(colorHex: String) {
        _uiState.value = _uiState.value.copy(colorHex = colorHex)
    }

    fun onParentCategorySelected(parentCategoryId: Long?) {
        _uiState.value = _uiState.value.copy(parentCategoryId = parentCategoryId)
    }

    private fun validateEntry() {
        val isValid = _uiState.value.name.isNotBlank()
        _uiState.value = _uiState.value.copy(isEntryValid = isValid)
    }

    fun saveCategory(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.name.isBlank()) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            val category = Category(
                id = state.id,
                name = state.name,
                type = state.type,
                iconKey = state.iconKey,
                colorHex = state.colorHex,
                parentCategoryId = state.parentCategoryId
            )
            if (state.isNewCategory) {
                repository.insertCategory(category)
            } else {
                repository.updateCategory(category)
            }
            onSuccess()
        }
    }
}
