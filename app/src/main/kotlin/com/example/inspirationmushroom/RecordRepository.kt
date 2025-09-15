package com.example.inspirationmushroom

import com.example.inspirationmushroom.ai.AIService
import com.example.inspirationmushroom.ai.ChatRequest
import com.example.inspirationmushroom.ai.Message
import com.example.inspirationmushroom.ai.Prompts
import com.example.inspirationmushroom.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RecordRepository(
    private val recordDao: RecordDao,
    private val settingsRepository: SettingsRepository
) {

    fun getAllRecords() = recordDao.getAllRecords()
    fun getPendingAnalysisRecords(): Flow<List<Record>> = recordDao.getPendingAnalysisRecords()

    fun getRecordsByDateRange(startTime: Long, endTime: Long) = recordDao.getRecordsByDateRange(startTime, endTime)

    suspend fun insert(record: Record): Long {
        return recordDao.insert(record)
    }

    suspend fun update(record: Record) {
        recordDao.update(record)
    }

    suspend fun delete(record: Record) {
        recordDao.delete(record)
    }

    private suspend fun triggerAnalysis(recordId: Long) {
        val config = settingsRepository.aiConfigFlow.first()
        val recordToAnalyze = recordDao.getRecordById(recordId)

        if (recordToAnalyze == null || recordToAnalyze.status != RecordStatus.PENDING_ANALYSIS) {
            return // Record deleted or already analyzed/failed
        }

        if (config.apiUrl.isBlank() || config.apiKey.isBlank() || config.model.isNullOrBlank()) {
            val updatedRecord = recordToAnalyze.copy(status = RecordStatus.ANALYSIS_FAILED)
            recordDao.update(updatedRecord)
            return
        }

        try {
            val api = AIService.createApi(config.apiUrl)
            val chatRequest = ChatRequest(
                model = config.model,
                messages = listOf(
                    Message(role = "system", content = Prompts.SYSTEM_PROMPT),
                    Message(role = "user", content = Prompts.createUserMessage(recordToAnalyze.content))
                )
            )

            val response = api.getChatCompletions("Bearer ${config.apiKey}", chatRequest)

            if (response.isSuccessful) {
                val analysisJson = response.body()?.choices?.first()?.message?.content
                if (analysisJson != null) {
                    val updatedRecord = recordToAnalyze.copy(
                        aiAnalysis = analysisJson,
                        status = RecordStatus.ANALYZED
                    )
                    recordDao.update(updatedRecord)
                } else {
                     val updatedRecord = recordToAnalyze.copy(status = RecordStatus.ANALYSIS_FAILED)
                    recordDao.update(updatedRecord)
                }
            } else {
                val updatedRecord = recordToAnalyze.copy(status = RecordStatus.ANALYSIS_FAILED)
                recordDao.update(updatedRecord)
            }
        } catch (e: Exception) {
            val updatedRecord = recordToAnalyze.copy(status = RecordStatus.ANALYSIS_FAILED)
            recordDao.update(updatedRecord)
        }
    }

    suspend fun saveRecordAndTriggerAnalysis(record: Record) {
        val recordId = recordDao.insert(record)
        CoroutineScope(Dispatchers.IO).launch {
            triggerAnalysis(recordId)
        }
    }

    suspend fun retryAnalysis(record: Record) {
        // Only retry if it's not already analyzed
        if (record.status != RecordStatus.ANALYZED) {
            val recordToRetry = record.copy(status = RecordStatus.PENDING_ANALYSIS, aiAnalysis = null)
            recordDao.update(recordToRetry)
            triggerAnalysis(record.id)
        }
    }
}