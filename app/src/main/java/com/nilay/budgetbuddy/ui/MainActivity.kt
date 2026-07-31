package com.nilay.budgetbuddy.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.nilay.budgetbuddy.ui.auth.AuthScreen
import com.nilay.budgetbuddy.ui.auth.SessionViewModel
import com.nilay.budgetbuddy.ui.settings.SettingsViewModel
import com.nilay.budgetbuddy.ui.theme.AppColorScheme
import com.nilay.budgetbuddy.ui.theme.BudgetBuddyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val sessionViewModel: SessionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkModeSetting by settingsViewModel.darkModeFlow.collectAsState()
            val useDarkTheme = darkModeSetting ?: isSystemInDarkTheme()
            val isLoggedIn by sessionViewModel.isLoggedIn.collectAsState()
            val colorSchemeSetting by settingsViewModel.colorSchemeFlow.collectAsState()
            val appColorScheme = try {
                AppColorScheme.valueOf(colorSchemeSetting)
            } catch (e: IllegalArgumentException) {
                AppColorScheme.DYNAMIC
            }

            // enableEdgeToEdge() only sets status/nav bar icon contrast once, at the system's
            // dark mode at launch — it doesn't react to this app's own dark-mode override, so
            // set it explicitly here whenever the resolved theme changes.
            val view = LocalView.current
            SideEffect {
                val window = (view.context as android.app.Activity).window
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !useDarkTheme
                controller.isAppearanceLightNavigationBars = !useDarkTheme
            }

            BudgetBuddyTheme(darkTheme = useDarkTheme, colorScheme = appColorScheme) {
                when (isLoggedIn) {
                    null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    true -> BudgetBuddyShell()
                    false -> AuthScreen(viewModel = hiltViewModel())
                }
            }
        }
    }
}
