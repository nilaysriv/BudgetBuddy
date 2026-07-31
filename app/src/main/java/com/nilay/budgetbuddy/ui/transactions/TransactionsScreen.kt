package com.nilay.budgetbuddy.ui.transactions

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nilay.budgetbuddy.domain.model.TransactionType
import com.nilay.budgetbuddy.domain.model.TransactionWithCategory
import com.nilay.budgetbuddy.ui.components.SkeletonBox
import com.nilay.budgetbuddy.util.CategoryIcons
import com.nilay.budgetbuddy.util.CurrencyFormatter
import com.nilay.budgetbuddy.util.LocalCurrency
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel,
    onAddTransaction: () -> Unit,
    onEditTransaction: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigator = rememberListDetailPaneScaffoldNavigator<Long>()
    val scope = rememberCoroutineScope()

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
        listPane = {
            TransactionListPane(
                uiState = uiState,
                onAddTransaction = onAddTransaction,
                onTransactionClick = { transactionId ->
                    viewModel.onTransactionSelected(transactionId)
                    scope.launch {
                        navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, transactionId)
                    }
                },
                onSearchQueryChanged = viewModel::onSearchQueryChanged,
                onFilterTypeSelected = viewModel::onFilterTypeSelected,
                onRefresh = viewModel::refresh
            )
        },
        detailPane = {
            TransactionDetailPane(
                transaction = uiState.selectedTransaction,
                onEdit = { id -> onEditTransaction(id) },
                onDelete = { transaction -> viewModel.deleteTransaction(transaction.transaction) },
                onClose = {
                    scope.launch {
                        navigator.navigateBack()
                    }
                }
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListPane(
    uiState: TransactionsUiState,
    onAddTransaction: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onFilterTypeSelected: (TransactionType?) -> Unit,
    onRefresh: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { /* Show filter dialog */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search transactions...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            TransactionList(
                transactions = uiState.transactions,
                onTransactionClick = onTransactionClick,
                isLoading = uiState.isLoading
            )
        }
    }
}

@Composable
fun TransactionList(
    transactions: List<TransactionWithCategory>,
    onTransactionClick: (Long) -> Unit,
    isLoading: Boolean
) {
    Crossfade(targetState = isLoading, animationSpec = tween(250), label = "transactionList") { loading ->
        when {
            loading -> TransactionListSkeleton()
            transactions.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions found.")
            }
            else -> {
                val groupedTransactions = transactions.groupBy {
                    SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(it.transaction.date))
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    groupedTransactions.forEach { (date, transactionsInDate) ->
                        item {
                            Text(
                                text = date,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(transactionsInDate) { item ->
                            TransactionItem(
                                transaction = item,
                                onClick = { onTransactionClick(item.transaction.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionListSkeleton() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(6) {
            SkeletonBox(height = 64.dp, shape = MaterialTheme.shapes.medium)
        }
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionWithCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        ListItem(
            headlineContent = { 
                Text(
                    transaction.category.name,
                    fontWeight = FontWeight.SemiBold
                ) 
            },
            supportingContent = { 
                transaction.transaction.note?.let { 
                    Text(
                        it,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall
                    ) 
                } 
            },
            trailingContent = {
                val color = if (transaction.transaction.type == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFF44336)
                val prefix = if (transaction.transaction.type == TransactionType.INCOME) "+" else "-"
                Text(
                    text = "$prefix${CurrencyFormatter.format(transaction.transaction.amount, LocalCurrency.current)}",
                    color = color,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            leadingContent = {
                val color = try {
                    Color(android.graphics.Color.parseColor(transaction.category.colorHex))
                } catch (e: Exception) {
                    MaterialTheme.colorScheme.primary
                }
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = color.copy(alpha = 0.2f)
                ) {
                    Icon(
                        imageVector = CategoryIcons.iconFor(transaction.category.iconKey),
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp),
                        tint = color
                    )
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
fun TransactionDetailPane(
    transaction: TransactionWithCategory?,
    onEdit: (Long) -> Unit,
    onDelete: (TransactionWithCategory) -> Unit,
    onClose: () -> Unit
) {
    if (transaction == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Select a transaction to see details")
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val color = try {
                Color(android.graphics.Color.parseColor(transaction.category.colorHex))
            } catch (e: Exception) {
                MaterialTheme.colorScheme.primary
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close")
                }
            }

            Surface(
                modifier = Modifier.size(80.dp),
                shape = MaterialTheme.shapes.large,
                color = color.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = CategoryIcons.iconFor(transaction.category.iconKey),
                    contentDescription = null,
                    modifier = Modifier.padding(20.dp),
                    tint = color
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(transaction.category.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                if (transaction.transaction.type == TransactionType.INCOME) "Income" else "Expense",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(32.dp))
            val amountColor = if (transaction.transaction.type == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFF44336)
            Text(
                CurrencyFormatter.format(transaction.transaction.amount, LocalCurrency.current),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = amountColor
            )

            Spacer(modifier = Modifier.height(32.dp))
            DetailRow("Date", SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(transaction.transaction.date)))
            if (!transaction.transaction.note.isNullOrBlank()) {
                DetailRow("Note", transaction.transaction.note)
            }

            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { onDelete(transaction) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete")
                }
                Button(
                    onClick = { onEdit(transaction.transaction.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
