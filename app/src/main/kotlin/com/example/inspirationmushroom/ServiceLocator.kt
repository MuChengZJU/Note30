package com.example.inspirationmushroom

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

// This is a simplified version. In a real app, you would use Hilt or Koin for dependency injection.
object ServiceLocator {
    private var database: AppDatabase? = null
    private var recordRepository: RecordRepository? = null

    fun provideDatabase(application: Application): AppDatabase {
        return database ?: AppDatabase.getDatabase(application).also { database = it }
    }

    fun provideRecordRepository(application: Application): RecordRepository {
        return recordRepository ?: RecordRepository(provideDatabase(application).recordDao()).also { recordRepository = it }
    }
}