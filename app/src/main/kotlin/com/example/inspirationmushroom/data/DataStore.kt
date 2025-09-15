package com.example.inspirationmushroom.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Define DataStore as a singleton at the top level
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_settings")
