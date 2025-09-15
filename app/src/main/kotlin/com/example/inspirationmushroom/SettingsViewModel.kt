package com.example.inspirationmushroom

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.inspirationmushroom.ai.AIService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_settings")

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore

    // Preferences keys
    private val API_URL_KEY = stringPreferencesKey("api_url")
    private val API_KEY_KEY = stringPreferencesKey("api_key")
    private val SELECTED_MODEL_KEY = stringPreferencesKey("selected_model")

    // State flows
    private val _apiUrl = MutableStateFlow("")
    val apiUrl: StateFlow<String> = _apiUrl.asStateFlow()

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _selectedModel = MutableStateFlow<String?>(null)
    val selectedModel: StateFlow<String?> = _selectedModel.asStateFlow()

    private val _availableModels = MutableStateFlow<List<String>>(emptyList())
    val availableModels: StateFlow<List<String>> = _availableModels.asStateFlow()

    private val _isLoadingModels = MutableStateFlow(false)
    val isLoadingModels: StateFlow<Boolean> = _isLoadingModels.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            dataStore.data.firstOrNull()?.let { preferences ->
                _apiUrl.value = preferences[API_URL_KEY] ?: ""
                _apiKey.value = preferences[API_KEY_KEY] ?: ""
                _selectedModel.value = preferences[SELECTED_MODEL_KEY]
            }
        }
    }

    fun updateApiUrl(url: String) {
        _apiUrl.value = url
    }

    fun updateApiKey(key: String) {
        _apiKey.value = key
    }

    fun updateSelectedModel(model: String) {
        _selectedModel.value = model
    }

    suspend fun saveConfiguration(): Boolean {
        return try {
            dataStore.edit { preferences ->
                preferences[API_URL_KEY] = _apiUrl.value
                preferences[API_KEY_KEY] = _apiKey.value
                _selectedModel.value?.let { preferences[SELECTED_MODEL_KEY] = it }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun loadAvailableModels() {
        if (_apiUrl.value.isBlank() || _apiKey.value.isBlank()) return

        _isLoadingModels.value = true
        try {
            val api = AIService.createApi(_apiUrl.value)
            val response = api.getModels("Bearer ${_apiKey.value}")

            if (response.isSuccessful) {
                val models = response.body()?.data?.map { it.id } ?: emptyList()
                _availableModels.value = models
            } else {
                _availableModels.value = emptyList()
            }
        } catch (e: Exception) {
            _availableModels.value = emptyList()
        } finally {
            _isLoadingModels.value = false
        }
    }
}
