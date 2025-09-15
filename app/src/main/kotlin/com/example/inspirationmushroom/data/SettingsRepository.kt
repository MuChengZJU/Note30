package com.example.inspirationmushroom.data

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AiConfig(
    val apiUrl: String,
    val apiKey: String,
    val model: String?
)

class SettingsRepository(context: Context) {
    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val API_URL_KEY = stringPreferencesKey("api_url")
        val API_KEY_KEY = stringPreferencesKey("api_key")
        val SELECTED_MODEL_KEY = stringPreferencesKey("selected_model")
        val REMINDER_INTERVAL_MIN_KEY = intPreferencesKey("reminder_interval_min")
    }

    val aiConfigFlow: Flow<AiConfig> = dataStore.data
        .map { preferences ->
            AiConfig(
                apiUrl = preferences[PreferencesKeys.API_URL_KEY] ?: "",
                apiKey = preferences[PreferencesKeys.API_KEY_KEY] ?: "",
                model = preferences[PreferencesKeys.SELECTED_MODEL_KEY]
            )
        }
}
