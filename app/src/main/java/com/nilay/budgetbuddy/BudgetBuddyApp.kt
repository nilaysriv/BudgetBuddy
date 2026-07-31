package com.nilay.budgetbuddy

import android.app.Application
import com.nilay.budgetbuddy.data.local.SettingsDataStore
import com.nilay.budgetbuddy.data.local.TokenHolder
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class BudgetBuddyApp : Application() {

    @Inject lateinit var settingsDataStore: SettingsDataStore
    @Inject lateinit var tokenHolder: TokenHolder

    override fun onCreate() {
        super.onCreate()
        // Mirror the persisted token into the in-memory holder OkHttp's interceptor reads synchronously.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            settingsDataStore.tokenFlow.collect { token -> tokenHolder.token = token }
        }
    }
}
