package com.nilay.budgetbuddy.data.local

import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory mirror of the persisted auth token, kept in sync from [SettingsDataStore.tokenFlow]
 * by BudgetBuddyApp at startup. OkHttp interceptors run off the main thread but need a
 * synchronous read on every request, which a suspending DataStore read can't give directly.
 */
@Singleton
class TokenHolder @Inject constructor() {
    @Volatile
    var token: String? = null
}
