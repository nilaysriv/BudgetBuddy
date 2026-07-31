package com.nilay.budgetbuddy.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val TOKEN = stringPreferencesKey("auth_token")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val CURRENCY = stringPreferencesKey("currency")
        val COLOR_SCHEME = stringPreferencesKey("color_scheme")
        val PROFILE_PICTURE_PATH = stringPreferencesKey("profile_picture_path")
    }

    private val safeData: Flow<Preferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    val darkModeFlow: Flow<Boolean?> = safeData.map { it[PreferencesKeys.DARK_MODE] }
    val tokenFlow: Flow<String?> = safeData.map { it[PreferencesKeys.TOKEN] }
    val userNameFlow: Flow<String?> = safeData.map { it[PreferencesKeys.USER_NAME] }
    val userEmailFlow: Flow<String?> = safeData.map { it[PreferencesKeys.USER_EMAIL] }
    val currencyFlow: Flow<String> = safeData.map { it[PreferencesKeys.CURRENCY] ?: "INR" }
    val colorSchemeFlow: Flow<String> = safeData.map { it[PreferencesKeys.COLOR_SCHEME] ?: "DYNAMIC" }
    val profilePicturePathFlow: Flow<String?> = safeData.map { it[PreferencesKeys.PROFILE_PICTURE_PATH] }

    suspend fun updateColorScheme(colorScheme: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.COLOR_SCHEME] = colorScheme }
    }

    suspend fun updateProfilePicturePath(path: String?) {
        dataStore.edit { preferences ->
            if (path != null) preferences[PreferencesKeys.PROFILE_PICTURE_PATH] = path
            else preferences.remove(PreferencesKeys.PROFILE_PICTURE_PATH)
        }
    }

    suspend fun updateDarkMode(isDarkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = isDarkMode
        }
    }

    suspend fun saveSession(token: String, userName: String, userEmail: String, currency: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TOKEN] = token
            preferences[PreferencesKeys.USER_NAME] = userName
            preferences[PreferencesKeys.USER_EMAIL] = userEmail
            preferences[PreferencesKeys.CURRENCY] = currency
        }
    }

    suspend fun updateCurrency(currency: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.CURRENCY] = currency }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.TOKEN)
            preferences.remove(PreferencesKeys.USER_NAME)
            preferences.remove(PreferencesKeys.USER_EMAIL)
            preferences.remove(PreferencesKeys.CURRENCY)
            preferences.remove(PreferencesKeys.PROFILE_PICTURE_PATH)
        }
    }
}
