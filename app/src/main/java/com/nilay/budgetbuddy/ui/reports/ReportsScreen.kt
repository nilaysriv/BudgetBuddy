package com.nilay.budgetbuddy.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nilay.budgetbuddy.ui.components.CategoryTrendChart
import com.nilay.budgetbuddy.ui.components.TrendBarChart
import com.nilay.budgetbuddy.ui.dashboard.SimplePieChart
import com.nilay.budgetbuddy.util.CurrencyFormatter
import com.nilay.budgetbuddy.util.LocalCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: ReportsViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Reports") }) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    PeriodSelector(selected = uiState.period, onSelect = viewModel::onPeriodSelected)
                }

                item { SummaryCard(uiState) }

                if (uiState.categoryBreakdown.isNotEmpty()) {
                    item { CategoryBreakdownCard(uiState) }
                }

                if (uiState.monthlyComparison.isNotEmpty()) {
                    item { MonthlyComparisonCard(uiState) }
                }

                if (uiState.categoryTrends.isNotEmpty()) {
                    item { CategoryTrendCard(uiState) }
                }

                if (uiState.showBudgetVariance) {
                    item { BudgetVarianceCard(uiState) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodSelector(selected: Period, onSelect: (Period) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val options = listOf(
            Period.THIS_MONTH to "This Month",
            Period.LAST_MONTH to "Last Month",
            Period.THIS_YEAR to "This Year"
        )
        options.forEach { (period, label) ->
            FilterChip(
                selected = selected == period,
                onClick = { onSelect(period) },
                label = { Text(label) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SummaryCard(uiState: ReportsUiState) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryStat("Income", uiState.totalIncome)
            SummaryStat("Expense", uiState.totalExpense)
            SummaryStat("Balance", uiState.balance)
        }
    }
}

@Composable
fun SummaryStat(label: String, amount: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            CurrencyFormatter.format(amount, LocalCurrency.current),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CategoryBreakdownCard(uiState: ReportsUiState) {
    val total = uiState.categoryBreakdown.sumOf { it.amount }.coerceAtLeast(0.01)
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Category Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SimplePieChart(categories = uiState.categoryBreakdown, modifier = Modifier.size(100.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    uiState.categoryBreakdown.take(6).forEach { category ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(10.dp),
                                shape = MaterialTheme.shapes.extraSmall,
                                color = try { Color(android.graphics.Color.parseColor(category.colorHex)) } catch (e: Exception) { Color.Gray }
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(category.categoryName, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), maxLines = 1)
                            Text(
                                "${(category.amount / total * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyComparisonCard(uiState: ReportsUiState) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("6-Month Comparison", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            TrendBarChart(uiState.monthlyComparison)
        }
    }
}

@Composable
fun CategoryTrendCard(uiState: ReportsUiState) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Top Category Trends", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            CategoryTrendChart(
                series = uiState.categoryTrends,
                monthLabels = uiState.monthlyComparison.map { it.label }
            )
        }
    }
}

@Composable
fun BudgetVarianceCard(uiState: ReportsUiState) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Budget vs. Actual", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (uiState.budgetVariance.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No budgets set for this month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                uiState.budgetVariance.forEach { item ->
                    val variance = item.actualSpent - item.budgetAmount
                    val isOver = variance > 0
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.categoryName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${CurrencyFormatter.format(item.actualSpent, LocalCurrency.current)} / ${CurrencyFormatter.format(item.budgetAmount, LocalCurrency.current)}",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                (if (isOver) "+" else "") + CurrencyFormatter.format(variance, LocalCurrency.current),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }
    }
}
