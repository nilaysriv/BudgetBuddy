package com.nilay.budgetbuddy.ui.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nilay.budgetbuddy.data.local.entity.Category
import com.nilay.budgetbuddy.domain.model.TransactionType
import com.nilay.budgetbuddy.util.CategoryIcons
import com.nilay.budgetbuddy.util.groupedByParent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    viewModel: CategoriesViewModel,
    onNavigateBack: () -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (Long) -> Unit
) {
    val categories by viewModel.categories.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCategory) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { padding ->
        if (categories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No categories found. Add some to get started!")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val expenseCategories = categories.filter { it.type == TransactionType.EXPENSE }.groupedByParent()
                val incomeCategories = categories.filter { it.type == TransactionType.INCOME }.groupedByParent()

                if (expenseCategories.isNotEmpty()) {
                    item {
                        Text("Expense Categories", style = MaterialTheme.typography.titleMedium)
                    }
                    items(expenseCategories) { category ->
                        CategoryItem(
                            category = category,
                            isSubcategory = category.parentCategoryId != null,
                            onEdit = { onEditCategory(category.id) },
                            onDelete = { viewModel.deleteCategory(category) }
                        )
                    }
                }

                if (incomeCategories.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Income Categories", style = MaterialTheme.typography.titleMedium)
                    }
                    items(incomeCategories) { category ->
                        CategoryItem(
                            category = category,
                            isSubcategory = category.parentCategoryId != null,
                            onEdit = { onEditCategory(category.id) },
                            onDelete = { viewModel.deleteCategory(category) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    isSubcategory: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (isSubcategory) 24.dp else 0.dp)
    ) {
        ListItem(
            headlineContent = { Text(category.name) },
            leadingContent = {
                val color = try {
                    Color(android.graphics.Color.parseColor(category.colorHex))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = MaterialTheme.shapes.small,
                    color = color
                ) {
                    Icon(
                        imageVector = CategoryIcons.iconFor(category.iconKey),
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = Color.White
                    )
                }
            },
            trailingContent = {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        )
    }
}
