package com.example.note30

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Date

class RecordViewModel(private val repository: RecordRepository) : ViewModel() {

    fun saveRecord(content: String, efficiency: Int, mood: String?) {
        viewModelScope.launch {
            val record = Record(
                timestamp = Date(),
                efficiency = efficiency,
                mood = mood,
                content = content
            )
            repository.insert(record)
        }
    }
}