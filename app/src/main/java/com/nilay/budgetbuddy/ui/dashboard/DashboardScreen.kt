package com.nilay.budgetbuddy.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nilay.budgetbuddy.domain.model.TransactionWithCategory
import com.nilay.budgetbuddy.domain.model.TrendPoint
import com.nilay.budgetbuddy.ui.components.LegendDot
import com.nilay.budgetbuddy.ui.components.SkeletonBox
import com.nilay.budgetbuddy.ui.components.TrendBarChart
import com.nilay.budgetbuddy.ui.theme.ExpenseColor
import com.nilay.budgetbuddy.ui.theme.IncomeColor
import com.nilay.budgetbuddy.ui.transactions.TransactionItem
import com.nilay.budgetbuddy.util.CurrencyFormatter
import com.nilay.budgetbuddy.util.LocalCurrency
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onSeeAllTransactions: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .widthIn(max = 640.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GreetingHeader(name = uiState.greetingName, onRefresh = viewModel::refresh)
            }

            if (uiState.isLoading) {
                item { DashboardSkeleton() }
            } else {
                item {
                    SummarySection(uiState)
                }

                if (uiState.weeklyTrend.isNotEmpty()) {
                    item { WeeklyTrendCard(uiState.weeklyTrend) }
                }

                if (uiState.topCategories.isNotEmpty()) {
                    item {
                        SpendingChartSection(uiState.topCategories)
                    }
                }

                if (uiState.budgetProgress.isNotEmpty()) {
                    item {
                        Text(
                            "Budget Progress",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(uiState.budgetProgress) { progress ->
                        Card(shape = MaterialTheme.shapes.medium) {
                            Box(modifier = Modifier.padding(16.dp).animateContentSize()) {
                                BudgetProgressItem(progress)
                            }
                        }
                    }
                }

                item {
                    RecentTransactionsSection(
                        transactions = uiState.recentTransactions,
                        onSeeAll = onSeeAllTransactions
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun GreetingHeader(name: String?, onRefresh: () -> Unit) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(greeting, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = name?.let { "$it 👋" } ?: "Welcome",
                style = MaterialTheme.typography.headlineLarge
            )
        }
        IconButton(onClick = onRefresh) {
            Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
        }
    }
}

@Composable
fun DashboardSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SkeletonBox(height = 140.dp, shape = MaterialTheme.shapes.large)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SkeletonBox(modifier = Modifier.weight(1f), height = 80.dp, shape = MaterialTheme.shapes.large)
            SkeletonBox(modifier = Modifier.weight(1f), height = 80.dp, shape = MaterialTheme.shapes.large)
        }
        SkeletonBox(height = 160.dp, shape = MaterialTheme.shapes.large)
        SkeletonBox(height = 100.dp, shape = MaterialTheme.shapes.large)
    }
}

@Composable
fun SummarySection(uiState: DashboardUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BalanceCard(uiState.balance)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IncomeExpenseCard(
                title = "Income",
                amount = uiState.totalIncome,
                icon = Icons.Rounded.ArrowUpward,
                color = IncomeColor,
                modifier = Modifier.weight(1.1f)
            )
            IncomeExpenseCard(
                title = "Expense",
                amount = uiState.totalExpense,
                icon = Icons.Rounded.ArrowDownward,
                color = ExpenseColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BalanceCard(balance: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.Wallet, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(
                "Total Balance",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                CurrencyFormatter.format(balance, LocalCurrency.current),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun IncomeExpenseCard(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier, shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(title, style = MaterialTheme.typography.labelMedium)
            }
            Text(
                CurrencyFormatter.format(amount, LocalCurrency.current),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun WeeklyTrendCard(data: List<TrendPoint>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Last 7 Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                LegendDot(color = IncomeColor, label = "Income")
                Spacer(modifier = Modifier.width(12.dp))
                LegendDot(color = ExpenseColor, label = "Expense")
            }
            Spacer(modifier = Modifier.height(16.dp))
            TrendBarChart(data)
        }
    }
}

@Composable
fun SpendingChartSection(categories: List<CategorySpending>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Top Spending Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                SimplePieChart(
                    categories = categories,
                    modifier = Modifier.size(120.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { category ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(12.dp),
                                shape = MaterialTheme.shapes.extraSmall,
                                color = Color(android.graphics.Color.parseColor(category.colorHex))
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                category.categoryName,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimplePieChart(categories: List<CategorySpending>, modifier: Modifier = Modifier) {
    val total = categories.sumOf { it.amount }
    val animationProgress by animateFloatAsState(targetValue = 1f, animationSpec = tween(600), label = "pie")
    Canvas(modifier = modifier) {
        var startAngle = -90f
        categories.forEach { category ->
            val sweepAngle = (category.amount / total).toFloat() * 360f * animationProgress
            drawArc(
                color = Color(android.graphics.Color.parseColor(category.colorHex)),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun RecentTransactionsSection(transactions: List<TransactionWithCategory>, onSeeAll: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Transactions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            TextButton(onClick = onSeeAll) { Text("See all") }
        }
        if (transactions.isEmpty()) {
            Text(
                "No transactions yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Column {
                transactions.forEach { transaction ->
                    TransactionItem(transaction = transaction, onClick = {})
                }
            }
        }
    }
}

@Composable
fun BudgetProgressItem(progress: BudgetProgress) {
    val percentage = if (progress.limit > 0) (progress.spent / progress.limit).toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = percentage.coerceIn(0f, 1f), label = "budgetProgress")

    val color = Color(android.graphics.Color.parseColor(progress.colorHex))

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(progress.categoryName, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${CurrencyFormatter.format(progress.spent, LocalCurrency.current)} / ${CurrencyFormatter.format(progress.limit, LocalCurrency.current)}",
                style = MaterialTheme.typography.labelSmall
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = if (percentage > 1f) MaterialTheme.colorScheme.error else color,
            trackColor = color.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}
