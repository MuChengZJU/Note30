package com.example.inspirationmushroom

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.example.inspirationmushroom.data.SettingsRepository

// This is a simplified version. In a real app, you would use Hilt or Koin for dependency injection.
object ServiceLocator {
    private var database: AppDatabase? = null
    private var recordRepository: RecordRepository? = null
    private var settingsRepository: SettingsRepository? = null

    fun provideDatabase(application: Application): AppDatabase {
        return database ?: AppDatabase.getDatabase(application).also { database = it }
    }

    fun provideSettingsRepository(application: Application): SettingsRepository {
        return settingsRepository ?: SettingsRepository(application).also { settingsRepository = it }
    }

    fun provideRecordRepository(application: Application): RecordRepository {
        return recordRepository ?: RecordRepository(
            provideDatabase(application).recordDao(),
            provideSettingsRepository(application)
        ).also { recordRepository = it }
    }
}