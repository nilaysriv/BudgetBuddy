package com.nilay.budgetbuddy.ui.budgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nilay.budgetbuddy.domain.model.TransactionType
import com.nilay.budgetbuddy.util.groupedByParent
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetAddEditScreen(
    viewModel: BudgetAddEditViewModel,
    budgetId: Long?,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories by viewModel.categories.collectAsState()

    LaunchedEffect(budgetId) {
        viewModel.loadBudget(budgetId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isNewBudget) "Add Budget" else "Edit Budget") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        IconButton(
                            onClick = { viewModel.saveBudget(onNavigateBack) },
                            enabled = uiState.isEntryValid
                        ) {
                            Icon(Icons.Default.Done, contentDescription = "Save")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category
            var showCategoryMenu by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    value = uiState.selectedCategory?.name ?: "Select Category",
                    onValueChange = {},
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showCategoryMenu = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                )
                DropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    val expenseCategories = categories.filter { it.type == TransactionType.EXPENSE }.groupedByParent()
                    if (expenseCategories.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No expense categories") },
                            onClick = { showCategoryMenu = false }
                        )
                    } else {
                        expenseCategories.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Text(if (category.parentCategoryId != null) "    ${category.name}" else category.name)
                                },
                                onClick = {
                                    viewModel.onCategorySelected(category)
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Month
            Text("Month", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::previousMonth) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous month")
                }
                Text(monthLabelFor(uiState.monthYear), style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next month")
                }
            }

            // Amount
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = viewModel::onAmountChanged,
                label = { Text("Budget Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }
    }
}

private fun monthLabelFor(monthYear: String): String = try {
    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(monthYear)!!
    )
} catch (e: Exception) {
    monthYear
}
