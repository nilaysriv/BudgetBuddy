package com.nilay.budgetbuddy.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.nilay.budgetbuddy.ui.settings.SettingsViewModel
import com.nilay.budgetbuddy.util.LocalCurrency
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.nilay.budgetbuddy.ui.navigation.BudgetBuddyRoute
import com.nilay.budgetbuddy.ui.navigation.rememberNavigationState
import com.nilay.budgetbuddy.ui.navigation.Navigator
import com.nilay.budgetbuddy.ui.navigation.toEntries
import com.nilay.budgetbuddy.ui.theme.BudgetBuddyTheme
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.hilt.navigation.compose.hiltViewModel
import com.nilay.budgetbuddy.ui.budgets.BudgetAddEditScreen
import com.nilay.budgetbuddy.ui.budgets.BudgetListScreen
import com.nilay.budgetbuddy.ui.categories.CategoryAddEditScreen
import com.nilay.budgetbuddy.ui.categories.CategoryListScreen
import com.nilay.budgetbuddy.ui.dashboard.DashboardScreen
import com.nilay.budgetbuddy.ui.reports.ReportsScreen
import com.nilay.budgetbuddy.ui.transactions.TransactionAddEditScreen
import com.nilay.budgetbuddy.ui.transactions.TransactionsScreen

@Composable
fun BudgetBuddyShell() {
    val topLevelRoutes = remember {
        setOf(
            BudgetBuddyRoute.Dashboard,
            BudgetBuddyRoute.Transactions,
            BudgetBuddyRoute.Budgets,
            BudgetBuddyRoute.Reports,
            BudgetBuddyRoute.Settings
        )
    }

    val navigationState = rememberNavigationState(
        startRoute = BudgetBuddyRoute.Dashboard,
        topLevelRoutes = topLevelRoutes
    )

    val navigator = remember { Navigator(navigationState) }

    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val currency by settingsViewModel.currencyFlow.collectAsState()

    val entryProvider = remember {
        entryProvider {
            entry<BudgetBuddyRoute.Dashboard> {
                DashboardScreen(
                    viewModel = hiltViewModel(),
                    onSeeAllTransactions = { navigator.navigate(BudgetBuddyRoute.Transactions) }
                )
            }
            entry<BudgetBuddyRoute.Transactions> {
                TransactionsScreen(
                    viewModel = hiltViewModel(),
                    onAddTransaction = { navigator.navigate(BudgetBuddyRoute.TransactionAddEdit()) },
                    onEditTransaction = { id -> navigator.navigate(BudgetBuddyRoute.TransactionAddEdit(id)) }
                )
            }
            entry<BudgetBuddyRoute.TransactionAddEdit> { route ->
                TransactionAddEditScreen(
                    viewModel = hiltViewModel(),
                    transactionId = route.transactionId,
                    onNavigateBack = { navigator.goBack() }
                )
            }
            entry<BudgetBuddyRoute.Budgets> {
                BudgetListScreen(
                    viewModel = hiltViewModel(),
                    onAddBudget = { navigator.navigate(BudgetBuddyRoute.BudgetAddEdit()) },
                    onEditBudget = { id -> navigator.navigate(BudgetBuddyRoute.BudgetAddEdit(id)) }
                )
            }
            entry<BudgetBuddyRoute.BudgetAddEdit> { route ->
                BudgetAddEditScreen(
                    viewModel = hiltViewModel(),
                    budgetId = route.budgetId,
                    onNavigateBack = { navigator.goBack() }
                )
            }
            entry<BudgetBuddyRoute.Reports> { ReportsScreen(viewModel = hiltViewModel()) }
            entry<BudgetBuddyRoute.Settings> { 
                SettingsScreen(
                    viewModel = hiltViewModel(),
                    onManageCategories = { navigator.navigate(BudgetBuddyRoute.Categories) }
                )
            }
            entry<BudgetBuddyRoute.Categories> {
                CategoryListScreen(
                    viewModel = hiltViewModel(),
                    onNavigateBack = { navigator.goBack() },
                    onAddCategory = { navigator.navigate(BudgetBuddyRoute.CategoryAddEdit()) },
                    onEditCategory = { id -> navigator.navigate(BudgetBuddyRoute.CategoryAddEdit(id)) }
                )
            }
            entry<BudgetBuddyRoute.CategoryAddEdit> { route ->
                CategoryAddEditScreen(
                    viewModel = hiltViewModel(),
                    categoryId = route.categoryId,
                    onNavigateBack = { navigator.goBack() }
                )
            }
        }
    }

    CompositionLocalProvider(LocalCurrency provides currency) {
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            topLevelRoutes.forEach { route ->
                val (label, icon) = when (route) {
                    BudgetBuddyRoute.Dashboard -> "Dashboard" to Icons.Rounded.Dashboard
                    BudgetBuddyRoute.Transactions -> "Transactions" to Icons.AutoMirrored.Rounded.ReceiptLong
                    BudgetBuddyRoute.Budgets -> "Budgets" to Icons.Rounded.AccountBalanceWallet
                    BudgetBuddyRoute.Reports -> "Reports" to Icons.Rounded.PieChart
                    BudgetBuddyRoute.Settings -> "Settings" to Icons.Rounded.Settings
                    else -> "Unknown" to Icons.Rounded.Settings
                }
                item(
                    selected = navigationState.topLevelRoute == route,
                    onClick = { navigator.navigate(route) },
                    icon = { Icon(icon, contentDescription = label) },
                    label = {
                        Text(
                            label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            // Each screen already handles its own top inset (TopAppBar, or statusBarsPadding()
            // for the couple that lack one) — consuming it again here doubled the top gap.
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            NavDisplay(
                modifier = Modifier.padding(innerPadding),
                entries = navigationState.toEntries(entryProvider as (NavKey) -> androidx.navigation3.runtime.NavEntry<NavKey>),
                onBack = { navigator.goBack() }
            )
        }
    }
    }
}

@PreviewScreenSizes
@Preview(showBackground = true)
@Composable
fun BudgetBuddyShellPreview() {
    BudgetBuddyTheme {
        BudgetBuddyShell()
    }
}
